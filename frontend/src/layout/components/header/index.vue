<script setup lang="ts">
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
    label: "加入群 17 个",
    key: "stmt3",
  },
];
const handleSelect = () => {};
</script>
<template>
  <n-layout-header bordered>
    <n-breadcrumb>
      <n-breadcrumb-item>Dashboard</n-breadcrumb-item>
      <n-breadcrumb-item>Home</n-breadcrumb-item>
    </n-breadcrumb>
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
