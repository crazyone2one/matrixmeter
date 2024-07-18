<script setup lang="ts">
import {NDrawer, NDrawerContent} from 'naive-ui'
import {useI18n} from "/@/hooks/use-i18n.ts";
import {ref, watch} from "vue";
import {UseFullScreen} from "/@/hooks/use-full-screen.ts";
import {Description} from "/@/components/base-description/index.vue";

interface DrawerProps {
  visible: boolean;
  title?: string | undefined;
  titleTag?: string;
  titleTagColor?: string;
  descriptions?: Description[];
  footer?: boolean;
  mask?: boolean; // 是否显示遮罩
  showSkeleton?: boolean; // 是否显示骨架屏
  okLoading?: boolean;
  okDisabled?: boolean;
  okPermission?: string[]; // 确认按钮权限
  okText?: string;
  cancelText?: string;
  saveContinueText?: string;
  showContinue?: boolean;
  width: string | number; // 抽屉宽度，为数值时才可拖拽改变宽度
  noContentPadding?: boolean; // 是否没有内容内边距
  popupContainer?: string;
  disabledWidthDrag?: boolean; // 是否禁止拖拽宽度
  closable?: boolean; // 是否显示右上角的关闭按钮
  noTitle?: boolean; // 是否不显示标题栏
  drawerStyle?: Record<string, string>; // 抽屉样式
  showFullScreen?: boolean; // 是否显示全屏按钮
  maskClosable?: boolean; // 点击遮罩是否关闭
  handleBeforeCancel?: () => boolean;
}

const props = withDefaults(defineProps<DrawerProps>(), {
  footer: true,
  mask: true,
  closable: true,
  showSkeleton: false,
  showContinue: false,
  popupContainer: 'body',
  disabledWidthDrag: false,
  showFullScreen: false,
  maskClosable: true,
  okPermission: () => [], // 确认按钮权限
});
const emit = defineEmits(['update:visible', 'confirm', 'cancel', 'continue', 'close']);

const {t} = useI18n();

const active = ref(props.visible);
const fullScreen = ref<UseFullScreen>();
const resizing = ref(false); // 是否正在拖拽
const drawerWidth = ref(props.width); // 抽屉初始宽度
const handleCancel=()=>{
  fullScreen.value?.exitFullscreen();
  active.value = false;
  emit('update:visible', false);
  emit('cancel');
}
watch(
    () => props.visible,
    (val) => {
      active.value = val;
    }
);
</script>

<template>
  <n-drawer v-model:show="active" v-bind="props" :width="fullScreen?.isFullScreen ? '100%' : drawerWidth"
            :mask-closable="props.maskClosable">
    <n-drawer-content :native-scrollbar="false">
      <template #header>
        <div class="flex items-center justify-between gap-[4px]">
          <slot name="title">
            <div class="flex flex-1 items-center justify-between">
              <div class="flex items-center">
                <n-tooltip :disabled="!props.title">
                  <template #trigger>
                    <span class="one-line-text max-w-[300px]"> {{ props.title }}</span>
                  </template>

                  {{ props.title }}
                </n-tooltip>

                <slot name="headerLeft"></slot>
                <n-tag v-if="titleTag" :color="props.titleTagColor" class="ml-[8px] mr-auto">
                  {{ props.titleTag }}
                </n-tag>
              </div>
              <slot name="tbutton"></slot>
            </div>
          </slot>
          <div class="right-operation-button-icon">
            <n-button v-if="props.showFullScreen" secondary text>
              {{ fullScreen?.isFullScreen ? t('common.offFullScreen') : t('common.fullScreen') }}
            </n-button>
          </div>
        </div>
      </template>
      <div class="ms-drawer-body">
        <slot>
        </slot>
      </div>
      <template #footer>
        <slot name="footer">
          <div class="flex items-center justify-between">
            <slot name="footerLeft"></slot>
            <div class="ml-auto flex gap-[12px]">
              <n-button :disabled="props.okLoading" @click="handleCancel">{{ t(props.cancelText || 'ms.drawer.cancel') }}</n-button>
              <n-button v-if="showContinue" v-permission="props.okPermission || []" secondary :loading="props.okLoading"
                        :disabled="okDisabled">
                {{ t(props.saveContinueText || 'ms.drawer.saveContinue') }}
              </n-button>
              <n-button v-permission="props.okPermission || []" type="primary" :disabled="okDisabled"
                        :loading="props.okLoading">
                {{ t(props.okText || 'ms.drawer.ok') }}
              </n-button>
            </div>
          </div>
        </slot>

      </template>
    </n-drawer-content>
  </n-drawer>
</template>

<style scoped>

</style>