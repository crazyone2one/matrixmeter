<script setup lang="ts">
import {NTag} from "naive-ui";
import {useAttrs} from "vue";

export type Size = 'small' | 'medium' | 'large';
export type TagType = 'default' | 'primary' | 'info' | 'success' | 'warning' | 'error'

const attrs = useAttrs();
const props = withDefaults(
    defineProps<{
      type?: TagType; // tag类型
      size?: Size; // tag尺寸
      selfStyle?: Record<string, any>; // 自定义样式
      width?: number; // tag宽度,不传入时绑定max-width
      maxWidth?: string;
      bordered?: boolean;
      round?: boolean;
      closable?: boolean; // 是否可关闭
    }>(),
    {
      type: 'default',
      theme: 'dark',
      size: 'medium',
      bordered: false,
    }
);
const emit = defineEmits<{
  (e: 'close'): void;
}>();

</script>

<template>
  <n-tag :bordered="bordered" v-bind="attrs"
         :type="props.type" :size="props.size" :round="round" :closable="closable" @close="emit('close')">
    <slot></slot>
    <template #icon>
      <slot name="icon"></slot>
    </template>
  </n-tag>
</template>

<style scoped>

</style>