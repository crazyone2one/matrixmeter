<script setup lang="ts">
import {useI18n} from "/@/hooks/use-i18n.ts";
import useTemplateStore from "/@/store/modules/setting/template.ts";
import {useRoute} from "vue-router";
import {computed, ref} from "vue";
import {SceneType} from "/@/api/interface/setting/template.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";

const props = defineProps<{
  mode: 'organization' | 'project';
  deletePermission: string[];
  createPermission: string[];
  updatePermission: string[];
}>();
const {t} = useI18n()
const templateStore = useTemplateStore()
const route = useRoute();
const scene = ref<SceneType>(route.query.type);
const isEnabledTemplate = computed(() => {
  return props.mode === 'organization'
      ? templateStore.projectStatus[scene.value as string]
      : !templateStore.projectStatus[scene.value as string];
});
</script>

<template>
  <div class="mb-4 flex items-center justify-between">
    <span v-if="isEnabledTemplate" class="font-medium">{{ t('system.orgTemplate.fieldList') }}</span>
    <n-button type="primary" v-if="!isEnabledTemplate && hasAnyPermission(props.createPermission)">
      {{ t('system.orgTemplate.addField') }}
    </n-button>
  </div>
</template>

<style scoped>

</style>