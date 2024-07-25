<script setup lang="ts">
import {useAppStore} from "/@/store";
import {listenerRouteChange} from "/@/utils/route-listener.ts";
import {ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {BreadcrumbItem} from "/@/router/routes/types.ts";

const appStore = useAppStore();
const router = useRouter();
const route = useRoute();
const isEdit = ref(true);


listenerRouteChange((newRouter) => {
  const {name, meta} = newRouter;
  isEdit.value = false;
  if (name === appStore.state.currentTopMenu.name) {
    appStore.setBreadcrumbList(appStore.state.currentTopMenu?.meta?.breadcrumbs);
  } else if ((name as string).includes(appStore.state.currentTopMenu.name as string)) {
    // 顶部菜单内下钻的父子路由命名是包含关系，子路由会携带完整的父路由名称
    const currentBreads = meta.breadcrumbs;
    appStore.setBreadcrumbList(currentBreads);
    // 下钻的三级路由一般都会区分编辑添加场景，根据场景展示不同的国际化路由信息
    const editTag = currentBreads && currentBreads[currentBreads.length - 1].editTag;
    setTimeout(() => {
      // 路由异步挂载，这里使用同步或者nextTick都取不到变化后的路由参数，所以使用定时器
      isEdit.value = editTag && route.query[editTag];
    }, 100);
  } else {
    appStore.setBreadcrumbList([]);
  }
}, true)
const jumpTo = (crumb: BreadcrumbItem, index: number) => {
  if (index === appStore.state.breadcrumbList.length - 1) {
    return;
  }
  if (crumb.isBack && window.history.state.back) {
    router.back();
  } else {
    const query: Record<string, any> = {};
    if (crumb.query) {
      crumb.query.forEach((key) => {
        query[key] = route.query[key];
      });
    }
    router.replace({name: crumb.name, query});
  }
}
</script>

<template>
  <n-breadcrumb v-if="appStore.state.breadcrumbList.length > 0" style="margin-top: 8px" class="z-10">
    <n-breadcrumb-item v-for="(crumb, index) of appStore.state.breadcrumbList"
                       :key="crumb.name" @click="jumpTo(crumb, index)"
    >
      {{ isEdit ? $t(crumb.editLocale || crumb.locale) : $t(crumb.locale) }}
    </n-breadcrumb-item>
    <!--    <n-breadcrumb-item>Home</n-breadcrumb-item>-->
  </n-breadcrumb>
</template>

<style scoped>

</style>