<script setup lang="ts">
withDefaults(defineProps<{
  type: string;
  size?: string | number;
  rotate?: number;
  custom?: boolean
}>(), {
  size: 16,
  rotate: 0,
  custom: false
})
</script>

<template>
  <n-icon :size="size">
    <div v-if="custom" :class="'i-matrix:' + type"/>
    <div v-else :class="type"/>
  </n-icon>
</template>

<style scoped></style>
