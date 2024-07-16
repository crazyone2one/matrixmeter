<script setup lang="ts">
import {ref, unref, watchEffect} from "vue";
import type {FormInst} from "naive-ui";
import {NScrollbar, NSwitch} from "naive-ui";
import {useI18n} from "/@/hooks/use-i18n.ts";
import {FormItemModel, FormMode} from "/@/components/batch-form/types.ts";
import {VueDraggable} from "vue-draggable-plus";
import MmIcon from "/@/components/icon/index.vue";

const props = withDefaults(
    defineProps<{
      models: FormItemModel[];
      formMode: FormMode;
      addText: string;
      maxHeight?: string;
      defaultVals?: any[]; // 当外层是编辑状态时，可传入已填充的数据
      isShowDrag?: boolean; // 是否可以拖拽
      formWidth?: string; // 自定义表单区域宽度
      showEnable?: boolean; // 是否显示启用禁用switch状态
      hideAdd?: boolean; // 是否隐藏添加按钮
      addToolTip?: string;
    }>(),
    {
      maxHeight: '30vh',
      isShowDrag: false,
      hideAdd: false,
    }
);
const emit = defineEmits(['change']);

const {t} = useI18n();
const formRef = ref<FormInst | null>(null)
const defaultForm = {
  list: [] as Record<string, any>[],
};
const form = ref<Record<string, any>>({list: [...defaultForm.list]});
const formItem: Record<string, any> = {};
const getFormResult = () => unref<Record<string, any>[]>(form.value.list);
const formValidate = (cb: (res?: Record<string, any>[]) => void, isSubmit = true) => {
  formRef.value?.validate(errors => {
    if (errors) {
      return
    }
    if (typeof cb === 'function') {
      if (isSubmit) {
        cb(getFormResult());
        return;
      }
      cb();
    }
  })
}
watchEffect(() => {
  props.models.forEach((e) => {
    // 默认填充表单项
    let value: string | number | boolean | string[] | number[] | undefined;
    if (e.type === 'inputNumber') {
      value = undefined;
    } else if (e.type === 'tagInput') {
      value = [];
    } else {
      value = e.defaultValue;
    }
    formItem[e.field] = value;
    if (props.showEnable) {
      // 如果有开启关闭状态，将默认禁用
      formItem.enable = false;
    }
    // 默认填充表单项的子项
    e.children?.forEach((child) => {
      formItem[child.field] = child.type === 'inputNumber' ? null : child.defaultValue;
    });
  });
  form.value.list = [{...formItem}];
  if (props.defaultVals?.length) {
    // 取出defaultVals的表单 field
    form.value.list = props.defaultVals.map((e) => e);
  }
});

function addField() {
  const item = [{...formItem}];
  item[0].type = [];
  formValidate(() => {
    form.value.list.push(item[0]); // 序号自增，不会因为删除而重复
  }, false);
}

function removeField(i: number) {
  form.value.list.splice(i, 1);
}

const resetForm = () => formRef.value?.restoreValidation()
defineExpose({
  formValidate,
  getFormResult,
  resetForm,
  // setFields,
});
</script>

<template>
  <n-form
      ref="formRef"
      :model="form"
      :label-width="80"

      size="small"
  >
    <div
        class="mb-[16px] overflow-y-auto rounded-[4px] border border-b-blue p-[12px]"
        :style="{ width: props.formWidth || '100%' }"
    >
      <n-scrollbar class="overflow-y-auto" :style="{ 'max-height': props.maxHeight }">
        <VueDraggable v-model="form.list"
                      ghost-class="ghost"
                      drag-class="dragChosenClass"
                      :disabled="!props.isShowDrag"
                      :force-fallback="true"
                      :animation="150"
                      handle=".dragIcon">
          <div v-for="(element, index) in form.list"
               :key="`${element.field}${index}`"
               class="draggableElement gap-[8px] py-[6px] pr-[8px]"
               :class="[props.isShowDrag ? 'cursor-move' : '']">
            <div v-if="props.isShowDrag" class="dragIcon ml-[8px] mr-[8px] pt-[8px]">
              <mm-icon type="i-mdi-drag-variant"/>
            </div>
            <n-form-item v-for="model of props.models" :class="index > 0 ? 'hidden-item' : 'mb-0 flex-1'"
                         :label="t(model.label || '')">
              <n-input v-if="model.type==='input'" v-model:value="element[model.field]" class="flex-1"
                       :placeholder="t(model.placeholder || '')"
                       :max-length="model.maxLength || 255" clearable/>
              <n-select v-else-if="model.type==='select'" :placeholder="t(model.placeholder || '')"
                        :options="model.options"/>
            </n-form-item>
            <div v-if="showEnable">
              <n-switch v-model:value="element.enable" class="mt-[8px]"
                        :style="{ 'margin-top': index === 0 && !props.isShowDrag ? '36px' : '' }"/>
            </div>
            <div v-if="!props.hideAdd"
                 v-show="form.list.length > 1"
                 class="minus"
                 :class="[
                'flex',
                'h-full',
                'w-[32px]',
                'cursor-pointer',
                'items-center',
                'justify-center',
                'text-[var(--color-text-4)]',
                'mt-[8px]',
              ]"
                 :style="{ 'margin-top': index === 0 && !props.isShowDrag ? '36px' : '' }"
                 @click="removeField(index)"
            >
              <mm-icon type="i-mdi:minus-circle"/>
            </div>
          </div>
        </VueDraggable>
      </n-scrollbar>
      <div v-if="props.formMode === 'create' && !props.hideAdd" class="w-full">
        <n-tooltip trigger="hover">
          <template #trigger>
            <n-button text class="px-0" @click="addField">
              <template #icon>
                <mm-icon type="i-mdi-plus-circle"/>
              </template>
            </n-button>
          </template>
          {{ t(props.addText) }}
        </n-tooltip>
      </div>
    </div>

  </n-form>
</template>

<style scoped>

</style>