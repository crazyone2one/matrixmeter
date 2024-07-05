<script setup lang="ts">
import {useI18n} from "/@/hooks/use-i18n.ts";
import {useAppStore, useUserStore} from "/@/store";
import {useRouter} from "vue-router";
import TopMenu from "/@/layout/components/header/TopMenu.vue";

const userStore = useUserStore();
const appStore = useAppStore();
const router = useRouter();
const {t} = useI18n()
const options = [
  {
    label: "处理群消息 342 条",
    key: "stmt1",
  },
  {
    label: "被 @ 58 次",
    key: "stmt2",
  },
  {
    label: () => t('personal.exit'),
    key: "stmt3",
  },
];
const handleSelect = async (key: string) => {
  switch (key) {
    case "stmt3":
      await userStore.logout();
      const currentRoute = router.currentRoute.value;
      window.$message.success(t('message.logoutSuccess'))
      router.push({
        name: 'login',
        query: {
          ...router.currentRoute.value.query,
          redirect: currentRoute.name as string,
        }
      });
      break;
  }
};
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
      <n-dropdown trigger="hover" :options="options" @select="handleSelect">
        <n-button>2021年 第36周</n-button>
      </n-dropdown>
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
