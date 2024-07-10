<script setup lang="ts">
defineProps<{
  type: string;
  size?: string | number;
  rotate?: number;
}>();
</script>

<template>
  <n-icon :size="size">
    <div :class="'i-matrix:' + type" />
  </n-icon>
</template>

<style scoped></style>
