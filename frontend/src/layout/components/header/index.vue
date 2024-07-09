<script setup lang="ts">
import {useAppStore} from "/@/store";
import TopMenu from "/@/layout/components/header/TopMenu.vue";
import PersonalMenu from "/@/layout/components/header/PersonalMenu.vue";

const appStore = useAppStore();

</script>
<template>
  <n-layout-header bordered>
    <n-select :options="appStore.state.projectList" style="width: 200px"/>
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
