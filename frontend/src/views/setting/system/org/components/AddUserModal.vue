<script setup lang="ts">
import {useI18n} from "/@/hooks/use-i18n.ts";
import {computed, ref, watchEffect} from "vue";
import type {FormInst, FormRules} from "naive-ui";
import {useForm, useRequest} from "alova/client";
import {UserRequestTypeEnum} from "/@/components/user-select/utils.ts";
import {UserTableItem} from "/@/api/interface/setting/user-group.ts";
import {addUserToOrgOrProject, getUserByOrganizationOrProject} from "/@/api/modules/setting/organizationAndProject.ts";
import {AddUserToOrgOrProjectParams} from "/@/api/interface/setting/org-project.ts";

const {t} = useI18n();
const props = defineProps<{
  visible: boolean;
  organizationId?: string;
  projectId?: string;
}>();

const emit = defineEmits<{
  (e: 'cancel'): void;
  (e: 'submit'): void;
}>();

const currentVisible = ref(props.visible);
const formRef = ref<FormInst | null>(null)
const userSelectorOptions = ref<Array<UserTableItem>>([])
const rules: FormRules = {
  name: [{
    required: true, message() {
      return t('system.organization.addMemberRequired')
    },
  }]
}
const {
  loading: submiting,
  form,
  send: submit,
} = useForm(
    formData => {
      // 可以在此转换表单数据并提交
      return addUserToOrgOrProject(formData as AddUserToOrgOrProjectParams);
    },
    {
      // 初始化表单数据
      initialForm: {
        userIds: [],
        organizationId: '',
        projectId: '',
      }
    }
);
const userSelectorProps = computed(() => {
  return {
    type: UserRequestTypeEnum.SYSTEM_ORGANIZATION,
    loadOptionParams: {
      sourceId: props.organizationId || props.projectId
    },
  };
});
const handleCancel = () => {
  form.value.userIds = [];
  emit('cancel');
};
const renderLabel = (option: UserTableItem) => {
  return `${option.name}(${option.email})`
}
const {send: loadUserOption} = useRequest(() => getUserByOrganizationOrProject(userSelectorProps.value.loadOptionParams.sourceId as string, ""), {immediate: false});
const handleAddMember = () => {
}
watchEffect(() => {
  currentVisible.value = props.visible;
  if (userSelectorProps.value.loadOptionParams.sourceId) {
    loadUserOption().then(res => userSelectorOptions.value = res)
  }
});
</script>

<template>
  <n-modal v-model:show="currentVisible" preset="dialog" :mask-closable="false"
           :title="t('system.organization.addMember')"
           @close="handleCancel">
    <n-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="rounded-[4px]"
    >
      <n-form-item :label="t('system.organization.member')" path="name">
        <n-select v-model:value="form.userIds" multiple :options="userSelectorOptions"
                  :render-label="renderLabel"
                  value-field="id"/>
      </n-form-item>
    </n-form>
    <template #action>
      <n-button secondary :loading="submiting" @click="handleCancel">{{ t('common.cancel') }}</n-button>
      <n-button type="primary" :loading="submiting" :disabled="form.userIds.length === 0" @click="handleAddMember">
        {{ t('common.add') }}
      </n-button>
    </template>
  </n-modal>
</template>

<style scoped>

</style>