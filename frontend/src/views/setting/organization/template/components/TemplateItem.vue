<script setup lang="ts">
import {useI18n} from "/@/hooks/use-i18n.ts";
import {useAppStore} from "/@/store";
import useTemplateStore from "/@/store/modules/setting/template.ts";
import {computed, ref} from "vue";
import {ActionsItem} from "/@/components/more-action/types.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";
import MoreAction from '/@/components/more-action/index.vue'

const {t} = useI18n();
const appStore = useAppStore();
const templateStore = useTemplateStore();
// const licenseStore = useLicenseStore();
const currentOrgId = computed(() => appStore.state.currentOrgId);

const props = defineProps<{
  cardItem: Record<string, any>;
  mode: 'organization' | 'project';
}>();
const emit = defineEmits<{
  (e: 'fieldSetting', key: string): void;
  (e: 'templateManagement', key: string): void;
  (e: 'workflowSetup', key: string): void;
  (e: 'updateState'): void;
}>();
const isEnableProject = computed(() => {
  return templateStore.projectStatus[props.cardItem.key];
});
const moreActions = ref<ActionsItem[]>([
  {
    label: t('system.orgTemplate.enable'),
    eventTag: 'enable',
    danger: true,
    permission: ['ORGANIZATION_TEMPLATE:READ+ENABLE'],
  },
]);
const showEnableVisible = ref<boolean>(false);
const orgName = computed(() => {
  return appStore.state.ordList.find((item: any) => item.id === appStore.state.currentOrgId)?.name || '默认组织';
});
const fieldSetting = () => {
  emit('fieldSetting', props.cardItem.key);
};
const templateManagement = () => {
  emit('templateManagement', props.cardItem.key);
};
const hasEnablePermission = computed(() => hasAnyPermission(['ORGANIZATION_TEMPLATE:READ+ENABLE']));
const isShow = computed(() => {
  if (props.cardItem.key === 'BUG') {
    return true;
  }
  return !hasEnablePermission.value ? false : !isEnableProject.value;
});
const handleMoreActionSelect = () => {
  enableHandler();
};
const enableHandler = async () => {
  try {
    showEnableVisible.value = true;
  } catch (error) {
    console.log(error);
  }
};
</script>

<template>
  <div class="outerWrapper p-[3px]">
    <div class="innerWrapper">
      <div class="content">
        <div class="logo-img h-[48px] w-[48px]">
        </div>
        <div class="template-operation">
          <div class="flex items-center">
            <span class="font-medium">{{ props.cardItem.name }}</span>
            <span v-if="isEnableProject" class="enable">{{ t('system.orgTemplate.enabledTemplates') }}</span>
          </div>
          <div class="flex min-w-[300px] flex-nowrap items-center">
          <span class="operation hover:text-blue">
              <span @click="fieldSetting">{{ t('system.orgTemplate.fieldSetting') }}</span>
              <n-divider direction="vertical"/>
            </span>
            <!-- 模板列表 -->
            <span class="operation hover:text-blue">
              <span @click="templateManagement">{{ t('system.orgTemplate.TemplateManagementList') }}</span>
              <n-divider v-if="isShow" direction="vertical"/>
            </span>
            <!-- 启用项目模板 只有组织可以启用 -->
            <span
                v-if="hasEnablePermission && props.mode === 'organization' && !isEnableProject"
                class="rounded p-[2px] hover:bg-blueGray"
            >
              <more-action :list="moreActions" @select="handleMoreActionSelect"/>
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.outerWrapper {
  box-shadow: 0 6px 15px rgba(120 56 135/ 5%);
  @apply rounded bg-white;

  .innerWrapper {
    @apply rounded p-6;

    .content {
      @apply flex;

      .logo-img {
        @apply mr-3 flex items-center justify-center bg-white;
      }

      .template-operation {
        .operation {
          cursor: pointer;
        }

        .enable {
          height: 20px;
          font-size: 12px;
          line-height: 14px;
          @apply ml-4 rounded p-1;
        }

        @apply flex flex-col justify-between;
      }
    }
  }
}
</style>