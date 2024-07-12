<script setup lang="ts">
import {NPopconfirm} from 'naive-ui'
import {useI18n} from "/@/hooks/use-i18n.ts";
import {ref} from "vue";

const {t} = useI18n();
const props = withDefaults(
    defineProps<{
      title: string;
      loading?: boolean;
      removeText?: string;
      okText?: string;
      cancelText?: string;
      disabled?: boolean;
    }>(),
    {
      removeText: 'common.remove',
      cancelText: 'common.cancel',
      disabled: false,
    }
);
const emit = defineEmits<{
  (e: 'ok'): void;
}>();
const currentVisible = ref(false);

const handlePositiveClick = () => {
  emit('ok');
}
const handleNegativeClick = () => {
  currentVisible.value = false;
}
const showPopover = () => {
  currentVisible.value = true;
};
</script>

<template>
  <n-popconfirm
      :show="currentVisible"
      @positive-click="handlePositiveClick"
      @negative-click="handleNegativeClick"
      :positive-text="t(props.removeText)"
      :negative-text="t(props.cancelText)"
  >
    <template #trigger>
      <n-button text :disabled="props.disabled" @click="showPopover">{{ t(props.removeText) }}</n-button>
    </template>
    {{props.title}}
  </n-popconfirm>
</template>

<style scoped>

</style>