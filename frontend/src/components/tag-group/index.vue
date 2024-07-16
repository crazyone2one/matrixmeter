<script setup lang="ts">
import BaseTag,{ Size } from "/@/components/tag-group/BaseTag.vue";
import {computed, useAttrs} from "vue";
const props = withDefaults(
    defineProps<{
      tagList: Array<any>;
      showNum?: number;
      nameKey?: string;
      isStringTag?: boolean; // 是否是字符串数组的标签
      size?: Size;
      tagPosition?:
          | 'top'
          | 'tl'
          | 'tr'
          | 'bottom'
          | 'bl'
          | 'br'
          | 'left'
          | 'lt'
          | 'lb'
          | 'right'
          | 'rt'
          | 'rb'
          | undefined; // 提示位置防止窗口抖动
    }>(),
    {
      showNum: 2,
      nameKey: 'name',
      size: 'medium',
      tagPosition: 'top',
    }
);
const emit = defineEmits<{
  (e: 'click'): void;
}>();
const attrs = useAttrs();

const filterTagList = computed(() => {
  return (props.tagList || []).filter((item: any) => item) || [];
});

const showTagList = computed(() => {
  return filterTagList.value.slice(0, props.showNum);
});
const getTagWidth = (tag: { [x: string]: any }) => {
  const tagStr = props.isStringTag ? tag : tag[props.nameKey];

  const tagWidth = tagStr ? tagStr.length : 0;
  // 16个中文字符
  return tagWidth < 16 ? tagWidth : 16;
};
</script>

<template>
  <div class="flex max-w-[440px] flex-row" @click="emit('click')">
    <base-tag v-for="tag of showTagList" :key="tag.id" :width="getTagWidth(tag)" :size="props.size" v-bind="attrs">
      {{ props.isStringTag ? tag : tag[props.nameKey] }}
    </base-tag>
  </div>
</template>

<style scoped>

</style>