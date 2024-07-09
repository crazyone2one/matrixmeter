<script setup lang="ts">
import BaseCard from '/@/components/base-card/index.vue'
import {nextTick, ref} from "vue";
import {useI18n} from "/@/hooks/use-i18n.ts";
import SystemProject from '/@/views/setting/system/org/components/SystemProject.vue'
import SystemOrganization from '/@/views/setting/system/org/components/SystemOrganization.vue'

const {t} = useI18n();
const currentTable = ref('organization');
const organizationCount = ref(0);
const projectCount = ref(0);
const orgTableRef = ref();
const projectTableRef = ref();
const keyword = ref('');
const loading = ref(false)

const handleSearch = () => {
  tableSearch()
}
const tableSearch = () => {
  if (currentTable.value === 'organization') {
    if (orgTableRef.value) {
      orgTableRef.value.fetchData();
      loading.value = orgTableRef.value.loading
    } else {
      nextTick(() => {
        orgTableRef.value?.fetchData();
        loading.value = orgTableRef.value.loading
      });
    }
  } else if (projectTableRef.value) {
    projectTableRef.value.fetchData();
  } else {
    nextTick(() => {
      projectTableRef.value?.fetchData();
    });
  }
}
</script>

<template>
  <base-card simple :loading="loading">
    <div class="mb-4 flex items-center justify-between">
      <div>
        <n-button v-if="currentTable !== 'organization'"
                  v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+ADD']"
                  type="primary">
          {{
            currentTable === 'organization'
                ? t('system.organization.createOrganization')
                : t('system.organization.createProject')
          }}
        </n-button>
      </div>
      <div class="flex items-center">
        <n-input v-model:value="keyword"
                 :placeholder="t('system.organization.searchIndexPlaceholder')"
                 class="w-[280px]"
                 @keyup.enter.prevent="handleSearch"/>
        <n-radio-group v-model:value="currentTable" name="radiogroup" class="ml-[14px] w-full">
          <n-flex>
            <n-radio value="organization">
              {{ t('system.organization.organizationCount', {count: organizationCount}) }}
            </n-radio>
            <n-radio value="project">
              {{ t('system.organization.projectCount', {count: projectCount}) }}
            </n-radio>
          </n-flex>
        </n-radio-group>
      </div>
    </div>
    <div>
      <system-organization v-if="currentTable === 'organization'" ref="orgTableRef" v-model:keyword="keyword"/>
      <system-project v-if="currentTable === 'project'" ref="projectTableRef"/>
    </div>
  </base-card>

</template>

<style scoped>

</style>