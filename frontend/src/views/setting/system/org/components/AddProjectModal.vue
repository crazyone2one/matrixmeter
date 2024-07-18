<script setup lang="ts">
import {useAppStore, useUserStore} from "/@/store";
import {useI18n} from "/@/hooks/use-i18n.ts";
import {CreateOrUpdateSystemProjectParams} from "/@/api/interface/setting/system/org-project.ts";
import {computed, h, ref, watchEffect} from "vue";
import {FormInst, FormRules, NButton} from "naive-ui";
import {useForm, useRequest} from "alova/client";
import {
  createOrUpdateProject,
  getAdminByOrganizationOrProject,
  getSystemOrgOption
} from "/@/api/modules/setting/organizationAndProject.ts";
import {OrgProjectTableItem, SystemOrgOption} from "/@/api/interface/setting/org-project.ts";
import Icon from '/@/components/icon/index.vue'
import {UserTableItem} from "/@/api/interface/setting/user-group.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";
import {showUpdateOrCreateMessage} from "/@/views/setting/utils.ts";

const appStore = useAppStore();
const userStore = useUserStore();
const {t} = useI18n();
const props = defineProps<{
  currentProject?: CreateOrUpdateSystemProjectParams;
}>();

defineOptions({
  name: 'SystemAddProjectModal',
});
const currentVisible = defineModel<boolean>('visible', {
  default: false,
});
const emit = defineEmits<{
  (e: 'cancel', shouldSearch: boolean): void;
}>();
const formRef = ref<FormInst | null>(null)
const isEdit = computed(() => !!props.currentProject?.id);
const allModuleIds = ['bugManagement', 'caseManagement', 'apiTest', 'testPlan'];
const showPoolModuleIds = ['apiTest', 'testPlan'];
const affiliatedOrgOption = ref<SystemOrgOption[]>([]);
const affiliatedUserOption = ref<UserTableItem[]>([]);
const moduleOption = [
  // { label: 'menu.workbench', value: 'workstation' },
  {label: 'menu.testPlan', value: 'testPlan'},
  {label: 'menu.bugManagement', value: 'bugManagement'},
  {label: 'menu.caseManagement', value: 'caseManagement'},
  {label: 'menu.apiTest', value: 'apiTest'},
  // { label: 'menu.uiTest', value: 'uiTest' },
  // { label: 'menu.performanceTest', value: 'loadTest' },
];
const {
  loading: submiting,
  form,
  send: submit,
} = useForm(
    formData => {
      // 可以在此转换表单数据并提交
      return createOrUpdateProject({id: isEdit.value ? props.currentProject?.id : '', ...formData} as Partial<OrgProjectTableItem>);
    },
    {
      // 初始化表单数据
      initialForm: {
        userIds: userStore.id ? [userStore.id] : [],
        organizationId: '',
        name: '',
        description: '',
        enable: true,
        moduleIds: allModuleIds,
        resourcePoolIds: [],
      }
    }
);
const rules: FormRules = {
  name: [{required: true, message: t('system.project.projectNameRequired')},
    {max: 255, message: t('common.nameIsTooLang')}
  ],
  organizationId: [{required: true, message: t('system.project.affiliatedOrgRequired')}],
  userIds: [{required: true, message: t('system.project.projectAdminIsNotNull')}]
}
const showPool = computed(() => showPoolModuleIds.some((item) => form.value.moduleIds?.includes(item)));
const handleCancel = (shouldSearch: boolean) => {
  formRef.value?.restoreValidation();
  emit('cancel', shouldSearch);
};
const {
  send: initAffiliatedOrgOption,
  onSuccess: onSuccessInitAffiliatedOrgOption
} = useRequest(() => getSystemOrgOption(), {immediate: false})
const {
  send: initAffiliatedUserOption,
  onSuccess: onSuccessInitAffiliatedUserOption
} = useRequest((keyword) => getAdminByOrganizationOrProject(keyword), {immediate: false})
/**
 * 提交保存数据
 */
const handleBeforeOk = () => {
  formRef.value?.validate(errors => {
    if (errors) {
      return;
    }
    submit().then(res => {
      console.log(res)
      showUpdateOrCreateMessage(isEdit.value, res.id, res.organizationId);
      handleCancel(true);
    })
  })
}
onSuccessInitAffiliatedOrgOption((res) => {
  affiliatedOrgOption.value = res.data;
  form.value.organizationId = affiliatedOrgOption.value[0].id;
})
onSuccessInitAffiliatedUserOption((res) => {
  affiliatedUserOption.value = res.data;
})

watchEffect(() => {
  initAffiliatedOrgOption();
  initAffiliatedUserOption("")
  if (props.currentProject?.id) {
    if (props.currentProject) {
      form.value.id = props.currentProject.id;
      form.value.name = props.currentProject.name;
      form.value.description = props.currentProject.description;
      form.value.enable = props.currentProject.enable;
      form.value.userIds = props.currentProject.userIds;
      form.value.organizationId = props.currentProject.organizationId;
      form.value.moduleIds = props.currentProject.moduleIds;
      form.value.resourcePoolIds = props.currentProject.resourcePoolIds;
    }
  }
})
</script>

<template>
  <n-modal v-model:show="currentVisible" preset="dialog" :mask-closable="false"
           :title="isEdit?t('system.project.updateProject'):t('system.project.createProject')"
           @close="handleCancel">
    <n-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="rounded-[4px]"
        label-placement="left"
        label-width="auto"
        require-mark-placement="right-hanging"
    >
      <n-form-item :label="t('system.project.name')" path="name">
        <n-input v-model:value="form.name" :placeholder="t('system.project.projectNamePlaceholder')"/>
      </n-form-item>
      <n-form-item :label="t('system.project.affiliatedOrg')" path="organizationId">
        <n-select v-model:value="form.organizationId" :options="affiliatedOrgOption"
                  :placeholder="t('system.project.affiliatedOrgPlaceholder')"
                  label-field="name" value-field="id"/>
      </n-form-item>
      <n-form-item :label="t('system.project.projectAdmin')" path="userIds">
        <n-select v-model:value="form.userIds" :options="affiliatedUserOption" multiple
                  :placeholder="t('system.project.pleaseSelectAdmin')" label-field="name" value-field="id"/>
      </n-form-item>
      <n-form-item :label="t('system.project.moduleSetting')" path="module">
        <n-checkbox-group v-model:value="form.moduleIds">
          <n-flex>
            <n-checkbox v-for="(item,index) in moduleOption" :key="index" :label="t(item.label)" :value="item.value"/>
          </n-flex>
        </n-checkbox-group>
      </n-form-item>
      <n-form-item :label="t('common.desc')" path="description">
        <n-input v-model:value="form.description" type="textarea"
                 :placeholder="t('system.project.descriptionPlaceholder')"
                 :maxlength="1000" clearable/>
      </n-form-item>
    </n-form>
    <template #action>
      <div class="flex flex-row justify-between">
        <div class="flex flex-row items-center gap-[4px] mr-3">
          <n-switch v-model:value="form.enable"/>
          <span>{{ t('system.organization.status') }}</span>
          <n-tooltip trigger="hover">
            <template #trigger>
              <icon type="i-mdi-chat-question"/>
            </template>
            {{ t('system.project.createTip') }}
          </n-tooltip>
        </div>
        <div class="flex flex-row gap-[14px]">
          <n-button secondary :loading="submiting" @click="handleCancel(false)">{{ t('common.cancel') }}</n-button>
          <n-button type="primary" :loading="submiting" :disabled="form.userIds.length === 0" @click="handleBeforeOk">
            {{ isEdit ? t('common.update') : t('common.create') }}
          </n-button>
        </div>
      </div>

    </template>
  </n-modal>
</template>

<style scoped>

</style>