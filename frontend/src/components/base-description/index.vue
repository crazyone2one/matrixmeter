<script setup lang="ts">
import {TagType} from "/@/components/tag-group/BaseTag.vue";
import {NSkeleton} from 'naive-ui'

export interface Description {
  label: string;
  value: (string | number) | (string | number)[];
  key?: string;
  isTag?: boolean; // 是否标签
  tagClass?: string; // 标签自定义类名
  tagType?: TagType; // 标签类型
  // tagTheme?: Theme; // 标签主题
  tagMaxWidth?: string; // 标签最大宽度
  closable?: boolean; // 标签是否可关闭
  showTagAdd?: boolean; // 是否显示添加标签
  isButton?: boolean;
  showCopy?: boolean;
  copyTimer?: any | null;
  onClick?: () => void;
}

const props = withDefaults(
    defineProps<{
      showSkeleton?: boolean;
      skeletonLine?: number;
      column?: number;
      descriptions: Description[];
      labelWidth?: string;
      oneLineValue?: boolean;
      addTagFunc?: (val: string, item: Description) => Promise<void>;
    }>(),
    {
      column: 1,
    }
);
const emit = defineEmits<{
  (e: 'addTag', val: string): void;
  (e: 'tagClose', tag: string | number, item: Description): void;
}>();
</script>

<template>
  <n-skeleton v-if="props.showSkeleton" text :repeat="props.skeletonLine"
              class="w-[28%]" size="large"/>
  <div v-else class="ms-description">
    <slot name="title"></slot>
    <div
        v-for="(item, index) of props.descriptions"
        :key="item.label"
        class="ms-description-item"
        :style="{ marginBottom: props.descriptions.length - index <= props.column ? '' : '16px' }"
    >
      <n-tooltip>
        <template #trigger>
          <div :class="`ms-description-item-label one-line-text max-w-[${props.labelWidth || '120px'}]`">
            <slot name="item-label">{{ item.label }}</slot>
          </div>
        </template>
        {{ item.label }}
      </n-tooltip>
    </div>
  </div>
</template>

<style scoped>

</style>