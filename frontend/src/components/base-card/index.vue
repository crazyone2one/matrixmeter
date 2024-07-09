<script setup lang="ts">
import { NSpin } from "naive-ui";

const props = withDefaults(
  defineProps<
    Partial<{
      simple: boolean; // 简单模式，没有标题和底部栏
      title: string; // 卡片标题
      subTitle: string; // 卡片副标题
      hideContinue: boolean; // 隐藏保存并继续创建按钮
      hideFooter: boolean; // 隐藏底部栏
      loading: boolean; // 卡片 loading 状态
      isEdit: boolean; // 是否编辑状态
      hideBack: boolean; // 隐藏返回按钮
      hasBreadcrumb: boolean; // 是否有面包屑，如果有面包屑，高度需要减去面包屑的高度
      isFullscreen?: boolean; // 是否全屏
      hideDivider?: boolean; // 是否隐藏分割线
      handleBack: () => void; // 自定义返回按钮触发事件
      showFullScreen: boolean; // 是否显示全屏按钮
      saveText?: string; // 保存按钮文案
      saveAndContinueText?: string; // 保存并继续按钮文案
    }>
  >(),
  {
    simple: false,
    hideContinue: false,
    hideFooter: false,
    isEdit: false,
    hideBack: false,
    hasBreadcrumb: false,
    loading: false,
  }
);
</script>

<template>
  <n-spin size="large" :show="loading">
    <n-card
      embedded
      :segmented="{
        content: true,
        footer: 'soft',
      }"
    >
      <template #header v-if="!props.simple">
        <div class="card-header">
          <div
            v-if="!props.hideBack"
            class="back-btn flex cursor-pointer items-center rounded-full"
          >
            todo
          </div>
          <slot name="headerLeft">
            <div class="font-medium">{{ props.title }}</div>
            <div>{{ props.subTitle }}</div>
          </slot>
        </div>
      </template>
      <template #header-extra> #header-extra </template>
      <slot></slot>
      <!--    <template #footer >-->
      <!--      #footer-->
      <!--    </template>-->
      <template
        #action
        v-if="!props.hideFooter && !props.simple"
        class="card-footer fixed flex justify-between bg-white"
      >
        <div class="ml-0 mr-auto">
          <slot name="footerLeft"></slot>
        </div>
        <slot name="footerRight">
          <div class="flex justify-end gap-[16px]">
            <n-button secondary>
              {{ $t("mscard.defaultCancelText") }}
            </n-button>
            <n-button v-if="!props.hideContinue && !props.isEdit" secondary>
              {{
                props.saveAndContinueText ||
                $t("mscard.defaultSaveAndContinueText")
              }}
            </n-button>
            <n-button type="primary">
              {{
                props.saveText ||
                $t(
                  props.isEdit
                    ? "mscard.defaultUpdate"
                    : "mscard.defaultConfirm"
                )
              }}
            </n-button>
          </div>
        </slot>
      </template>
    </n-card>
  </n-spin>
</template>

<style scoped>
.card-header {
  padding-bottom: 16px;

  .back-btn {
    margin-right: 8px;
    width: 30px;
    height: 30px;
    border: 1px solid #ffffff;
    box-shadow: 0 0 7px rgb(15 0 78 / 9%);
  }
}

.card-footer {
  right: 16px;
  bottom: 0;
  z-index: 100;
  padding: 24px;
  border-bottom: 0;
}
</style>
