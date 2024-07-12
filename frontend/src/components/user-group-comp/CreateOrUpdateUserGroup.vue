<script setup lang="ts">
import {inject, reactive, ref, watchEffect} from "vue";
import {AuthScopeEnum} from "/@/enums/commonEnum.ts";
import {UserGroupItem} from "/@/api/interface/setting/user-group.ts";
import type {FormInst, FormItemRule, FormRules} from 'naive-ui'
import {useI18n} from "/@/hooks/use-i18n.ts";
import {useAppStore} from "/@/store";
import {updateOrAddOrgUserGroup, updateOrAddUserGroup} from "/@/api/modules/setting/user-group.ts";

const {t} = useI18n()
const systemType = inject<AuthScopeEnum>('systemType');
const props = defineProps<{
  id?: string;
  list: UserGroupItem[];
  visible: boolean;
  defaultName?: string;
  // 权限范围
  authScope: AuthScopeEnum;
}>();
const emit = defineEmits<{
  (e: 'cancel', value: boolean): void;
  (e: 'submit', currentId: string): void;
}>();
const appStore = useAppStore();
const formRef = ref<FormInst | null>(null)
const form = reactive({
  name: '',
});
const rules: FormRules = {
  name: [
    {
      required: true,
      validator(_rule: FormItemRule, value: string) {
        if (value === undefined || value === '') {
          return new Error(t('system.userGroup.userGroupNameIsNotNone'))
        } else {
          if (value === props.defaultName) {
            return true;
          } else {
            const isExist = props.list.some((item) => item.name === value);
            if (isExist) {

              return new Error(t('system.userGroup.userGroupNameIsExist', {name: value}))
            }
          }
          if (value.length > 255) {
            return new Error(t('common.nameIsTooLang'))
          }
          return true
        }
      },
      trigger: ['input', 'blur']
    }
  ]
}
const loading = ref(false);
const currentVisible = ref(props.visible);
watchEffect(() => {
  currentVisible.value = props.visible;
  form.name = props.defaultName || '';
});
const handleCancel = () => {
  form.name = '';
  // loading.value = false;
  emit('cancel', false);
};

const handleOutsideClick = () => {
  if (currentVisible.value) {
    handleCancel();
  }
};
const handleBeforeOk = () => {
  formRef.value?.validate(async (errors) => {
    if (errors) {
      return false
    }
    try {
      loading.value = true;
      let res: UserGroupItem | undefined;
      if (systemType === AuthScopeEnum.SYSTEM) {
        res = await updateOrAddUserGroup({id: props.id, name: form.name, type: props.authScope});
      } else if (systemType === AuthScopeEnum.ORGANIZATION) {
        // 组织用户组
        res = await updateOrAddOrgUserGroup({
          id: props.id,
          name: form.name,
          type: props.authScope,
          scopeId: appStore.state.currentOrgId,
        });
      } else {
        res = {name: form.name}
      }
      if (res) {
        window.$message.success(
            props.id ? t('system.userGroup.updateUserGroupSuccess') : t('system.userGroup.addUserGroupSuccess')
        );
        emit('submit', res.id);
        handleCancel();
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    } finally {
      loading.value = false;
    }
  })
}
</script>

<template>
  <n-popover :show="currentVisible" trigger="click" class="w-[350px]" :content-class="props.id ? 'move-left' : ''">
    <template #trigger>
      <slot></slot>
    </template>
    <div v-outer="handleOutsideClick">
      <div class="form">
        <n-form
            ref="formRef"
            :model="form"
            :rules="rules"
        >
          <div class="mb-[8px] text-[14px] font-medium">
            {{ props.id ? $t('system.userGroup.rename') : $t('system.userGroup.createUserGroup') }}
          </div>
          <n-form-item path="name">
            <n-input v-model:value="form.name" :maxlength="255" :placeholder="$t('system.userGroup.searchHolder')"/>
            <span v-if="!props.id" class="mt-[8px] text-[13px] font-medium">
                {{ t('system.userGroup.createUserGroupTip') }}
              </span>
          </n-form-item>
        </n-form>
      </div>
      <div class="flex flex-row flex-nowrap justify-end gap-2">
        <n-button secondary size="tiny" :disabled="loading" @click="handleCancel">
          {{ t('common.cancel') }}
        </n-button>
        <n-button type="primary" size="tiny" :loading="loading" :disabled="form.name.length === 0"
                  @click="handleBeforeOk">
          {{ props.id ? t('common.confirm') : t('common.create') }}
        </n-button>
      </div>
    </div>

  </n-popover>
</template>

<style scoped>
.move-left {
  position: relative;
  right: 22px;
}
</style>