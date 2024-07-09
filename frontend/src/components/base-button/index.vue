<script setup lang="ts">
const props = withDefaults(
    defineProps<{
      type?: 'text' | 'icon' | 'button';
      status?: 'default' | 'tertiary' | 'primary' | 'success' | 'info' | 'warning' | 'error'
      disabled?: boolean;
      loading?: boolean;
      size?: 'tiny' | 'small' | 'medium' | 'large'
    }>(),
    {
      type: 'text',
      status: 'primary',
      size: 'medium',
    }
);

const emit = defineEmits(['click']);
const clickHandler = () => {
  if (!props.disabled) {
    emit('click');
  }
}
</script>

<template>
  <n-button class="mm-button" :text="type" :type="status" @click.stop="clickHandler">
    <slot></slot>
  </n-button>
</template>

<style scoped>
.mm-button {

  &:not(:last-child) {
    @apply mr-4;
  }

  padding: 0 4px;
  font-size: 14px;
}
</style>