<script setup lang="ts">
import {useAppStore, useUserStore} from "/@/store";
import TopMenu from "/@/layout/components/header/TopMenu.vue";
import PersonalMenu from "/@/layout/components/header/PersonalMenu.vue";
import {getFirstRouteNameByPermission, hasAnyPermission} from "/@/utils/permission.ts";
import Icon from '/@/components/icon/index.vue'
import {useRequest} from "alova/client";
import {switchProject} from "/@/api/modules/project-manage/project.ts";
import router from "/@/router";
import AddProjectModal from '/@/views/setting/system/org/components/AddProjectModal.vue'
import {ref} from "vue";

const appStore = useAppStore();
const userStore = useUserStore()
const projectVisible = ref(false);
const {send} = useRequest((param) => switchProject(param), {immediate: false})
const handleUpdateValue = (value: string) => {
  send({projectId: value, userId: userStore.$state.id || ''})
      .finally(() => {
        router.replace({
          name: getFirstRouteNameByPermission(router.getRoutes()),
          query: {
            orgId: appStore.state.currentOrgId,
            pId: value as string,
          },
        });
      })
}
</script>
<template>
  <n-layout-header bordered>
    <n-select v-model:value="appStore.state.currentProjectId" :options="appStore.state.projectList"
              style="width: 230px"
              label-field="name" value-field="id"
              filterable
              @update:value="handleUpdateValue">
      <template v-if="hasAnyPermission(['ORGANIZATION_PROJECT:READ+ADD'])" #action>
        <n-button text @click="projectVisible = true">
          <template #icon>
            <icon type="i-mdi-plus"/>
          </template>
          {{ $t('settings.navbar.createProject') }}
        </n-button>
      </template>
    </n-select>
    <n-split default-size="0.1" :resize-trigger-size="0.1" style="margin-left: 20px"
             :pane-1-style="{'margin-right': '20px'}">
      <template #1>
        <n-breadcrumb style="margin-top: 8px">
          <n-breadcrumb-item>Dashboard</n-breadcrumb-item>
          <n-breadcrumb-item>Home</n-breadcrumb-item>
        </n-breadcrumb>
      </template>
      <template #2>
        <top-menu/>
      </template>
    </n-split>

    <n-flex :size="20" align="center" style="line-height: 1">
      <personal-menu/>
    </n-flex>
  </n-layout-header>
  <add-project-modal :visible="projectVisible" @cancel="projectVisible=false"/>
</template>
<style scoped>
.n-layout-header {
  position: sticky;
  top: 0;
  z-index: 1;
  display: flex;
  align-items: center;
  padding: 9px 18px;
}

.n-button {
  margin-right: 15px;
}

.n-flex {
  margin-left: auto;
}
</style>
