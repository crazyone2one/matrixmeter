<script setup lang="ts">
import {useI18n} from "/@/hooks/use-i18n.ts";
import {hasAllPermission, hasAnyPermission} from "/@/utils/permission.ts";
import Icon from '/@/components/icon/index.vue'
import {computed, ref} from "vue";
import useFeatureCaseStore from "/@/store/modules/case/feature-case.ts";

const {t} = useI18n()
const featureCaseStore = useFeatureCaseStore();
const isExpandAll = ref(false);
const modulesCount = computed(() => {
  return featureCaseStore.modulesCount;
});
</script>

<template>
  <n-card>
    <n-split direction="horizontal" style="height: 200px" :max="0.75" :min="0.25">
      <template #1>
        <div class="p-[16px] pb-0">
          <div class="feature-case h-[100%]">
            <div class="mb-[16px] flex justify-between">
              <n-input :placeholder="t('caseManagement.caseReview.folderSearchPlaceholder')" :maxlength="255"/>
              <n-dropdown v-if="hasAllPermission(['FUNCTIONAL_CASE:READ+IMPORT', 'FUNCTIONAL_CASE:READ+ADD'])"
                          trigger="hover">
                <n-button class="ml-2">{{ t('caseManagement.featureCase.importExcel') }}</n-button>
              </n-dropdown>
              <n-button v-else-if="
                  !hasAnyPermission(['FUNCTIONAL_CASE:READ+ADD']) && hasAnyPermission(['FUNCTIONAL_CASE:READ+IMPORT'])
                " type="primary" class="ml-2">
                {{ t('caseManagement.featureCase.importExcel') }}
              </n-button>
              <n-button v-else
                        v-permission="['FUNCTIONAL_CASE:READ+ADD']" type="primary" class="ml-2">
                {{ t('common.newCreate') }}
              </n-button>
            </div>
            <div class="case h-[38px]">
              <div class="flex items-center">
                <icon type="i-mdi-folder"/>
                <div class="folder-name mx-[4px]">{{ t('caseManagement.featureCase.allCase') }}</div>
                <div class="folder-count">({{ modulesCount.all || 0 }})</div>
              </div>
              <div class="ml-auto flex items-center">
                <n-tooltip trigger="hover">
                  <template #trigger>
                    <n-button text secondary class="!mr-0 p-[4px]">
                      <template #icon>
                        <icon type="i-mdi-folder"/>
                      </template>
                    </n-button>
                  </template>
                  {{isExpandAll ? t('common.expandAllSubModule') : t('common.collapseAllSubModule')}}
                </n-tooltip>
              </div>
            </div>
            <n-divider class="my-[8px]"/>
            Pane 1
          </div>
        </div>

      </template>
      <template #2>
        Pane 2
      </template>
    </n-split>
  </n-card>
</template>

<style scoped>

</style>