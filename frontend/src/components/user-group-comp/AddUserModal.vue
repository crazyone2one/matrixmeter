<script setup lang="ts">
import {useI18n} from "/@/hooks/use-i18n.ts";
import {computed, inject, reactive, ref, watchEffect} from "vue";
import {AuthScopeEnum} from "/@/enums/commonEnum.ts";
import {prettyLog} from "/@/utils/log.ts";
import type {FormInst, FormRules} from "naive-ui";
import {UserRequestTypeEnum} from "/@/components/user-select/utils.ts";
import {useAppStore} from "/@/store";
import {useRequest} from "alova/client";
import {
  addOrgUserToUserGroup,
  addUserToUserGroup,
  getOrgUserGroupOption,
  getSystemUserGroupOption
} from "/@/api/modules/setting/user-group.ts";
import {UserTableItem} from "/@/api/interface/setting/user-group.ts";

const {t} = useI18n();
const systemType = inject<AuthScopeEnum>('systemType');
const log = prettyLog()
const props = defineProps<{
  visible: boolean;
  currentId: string;
}>();
const appStore = useAppStore();
const currentOrgId = computed(() => appStore.state.currentOrgId);
const emit = defineEmits<{
  (e: 'cancel', shouldSearch: boolean): void;
}>();
const form = reactive({
  name: [],
});
const formRef = ref<FormInst | null>(null)
const rules: FormRules = {
  name: [{
    required: true, message() {
      return t('system.userGroup.userRequired')
    },
  }]
}
const currentVisible = ref(props.visible);
const userSelectorProps = computed(() => {
  if (systemType === AuthScopeEnum.SYSTEM) {
    return {
      type: UserRequestTypeEnum.SYSTEM_USER_GROUP,
      loadOptionParams: {
        roleId: props.currentId,
      },
      disabledKey: 'exclude',
    };
  }
  return {
    type: UserRequestTypeEnum.ORGANIZATION_USER_GROUP,
    loadOptionParams: {
      roleId: props.currentId,
      organizationId: currentOrgId.value,
    },
    disabledKey: 'checkRoleFlag',
  };
});
const userSelectorOptions = ref<Array<UserTableItem>>([])
const {send: loadSug} = useRequest((params) => getSystemUserGroupOption(params),
    {
      immediate: false,
      force: true
    })
const {send: loadOug} = useRequest((params) => getOrgUserGroupOption(params.organizationId, params.roleId, params.keyword),
    {
      immediate: false,
      force: true
    })
const handleCancel = (shouldSearch = false) => {
  form.name = [];
  emit('cancel', shouldSearch);
}
const {
  loading,
  send: submitUserToUserGroup,
  onSuccess: submitUserToUserGroupSuccess
} = useRequest(
    formData => addUserToUserGroup(formData),
    {
      immediate: false
    }
);
const {
  loading: load4ou,
  send: submitOrgUserToUserGroup,
} = useRequest(
    formData => addOrgUserToUserGroup(formData),
    {
      immediate: false
    }
);
const globalLoading = computed(() => {
  return loading.value || load4ou.value
})
const handleBeforeOk = (e: MouseEvent) => {
  e.preventDefault()
  formRef.value?.validate((errors) => {
    if (!errors) {
      if (systemType === AuthScopeEnum.SYSTEM) {
        const param = {roleId: props.currentId, userIds: form.name};
        submitUserToUserGroup(param)
      }
      if (systemType === AuthScopeEnum.ORGANIZATION) {
        const param = {
          userRoleId: props.currentId,
          userIds: form.name,
          organizationId: currentOrgId.value,
        };
        submitOrgUserToUserGroup(param)
      }
    } else {
      console.log(errors)
      window.$message.error('Invalid')
    }
  })
}
submitUserToUserGroupSuccess(() => {
  handleCancel(true);
  window.$message.success(t('common.addSuccess'))
})
watchEffect(() => {
  currentVisible.value = props.visible;
  if (userSelectorProps.value.loadOptionParams.roleId) {
    if (userSelectorProps.value.type === UserRequestTypeEnum.SYSTEM_USER_GROUP) {
      loadSug(userSelectorProps.value.loadOptionParams).then(res => userSelectorOptions.value = res);
    } else if (userSelectorProps.value.type === UserRequestTypeEnum.ORGANIZATION_USER_GROUP) {
      loadOug(userSelectorProps.value.loadOptionParams).then(res => userSelectorOptions.value = res);
    }
  }
});
const renderLabel = (option: UserTableItem) => {
  return `${option.name}(${option.email})`
}
</script>

<template>
  <n-modal v-model:show="currentVisible" preset="dialog" :mask-closable="false"
           :title="t('system.userGroup.addUser')"
           @close="handleCancel(false)">
    <n-form
        ref="formRef"
        :model="form"
        :rules="rules"
    >
      <n-form-item :label="t('system.userGroup.user')" path="name">
        <n-select v-model:value="form.name" multiple :options="userSelectorOptions"
                  :render-label="renderLabel"
                  value-field="id"/>
      </n-form-item>
    </n-form>
    <template #action>
      <n-button secondary :loading="globalLoading" @click="handleCancel(false)">{{ t('common.cancel') }}</n-button>
      <n-button type="primary" :loading="globalLoading" :disabled="form.name.length === 0" @click="handleBeforeOk">
        {{ t('common.add') }}
      </n-button>
    </template>
  </n-modal>
</template>

<style scoped>

</style>