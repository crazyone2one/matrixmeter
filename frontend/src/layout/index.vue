<script setup lang="ts">
import MHeader from "/@/layout/components/header/index.vue";
import sidebar from "/@/layout/components/sidebar/index.vue";
</script>
<template>
  <n-layout position="absolute" has-sider>
    <sidebar />
    <n-layout>
      <m-header />
      <n-layout-content
        content-style="padding: 20px 40px 10px; min-height: 85vh"
      >
        <router-view />
      </n-layout-content>
      <n-layout-footer>
        <n-p>&copy; {{ new Date().getFullYear() }} . All rights reserved.</n-p>
      </n-layout-footer>
    </n-layout>
  </n-layout>
</template>

<style scoped>
.n-layout-footer {
  opacity: 0.6;
  padding: 20px;
  text-align: center;
}
.n-a {
  text-decoration: none;
}
</style>
