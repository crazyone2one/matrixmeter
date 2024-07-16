<script setup lang="ts">
import {ActionsItem} from "/@/components/more-action/types.ts";
import {computed} from "vue";
import type {DropdownOption} from "naive-ui";
import {useI18n} from "/@/hooks/use-i18n.ts";
import MmIcon from "/@/components/icon/index.vue";

const props = defineProps<{
  list: ActionsItem[];
}>();
const emit = defineEmits(['select', 'close', 'open']);
const {t} = useI18n()
const options = computed(() => {
  const tmp: DropdownOption[] = []
  props.list.map(item => {
    tmp.push(item.isDivider ? {type: 'divider'} : {
      label: t(item.label as string),
      key: item.eventTag,
      disabled: item.disabled
    })
  })
  return tmp
})
const selectHandler = (key: string | number) => {
  const item = props.list.find((e: ActionsItem) => e.eventTag === key);
  emit('select', item);
}
</script>

<template>
  <n-dropdown trigger="click" :options="options" @select="selectHandler">
    <mm-icon type="i-mdi-dots-horizontal-circle-outline"/>
  </n-dropdown>
</template>

<style scoped>
.more-icon-btn {
  padding: 2px;
}
</style>