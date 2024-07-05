<script setup lang="ts">
import type {MenuOption} from 'naive-ui'
import {useAppStore} from "/@/store";
import {cloneDeep} from "lodash-es";
import appClientMenus from "/@/router/app-menus";
import {RouteRecordName, RouteRecordRaw, useRouter} from "vue-router";
import {useI18n} from "/@/hooks/use-i18n.ts";
import usePermission from "/@/hooks/use-permission.ts";
import {Ref, ref, watch, watchEffect} from "vue";
import {RouteEnum} from "/@/enums/routeEnum.ts";
import {listenerRouteChange} from "/@/utils/route-listener.ts";
import {renderRouterLink} from "/@/utils/render.ts";

const copyRouters = cloneDeep(appClientMenus) as RouteRecordRaw[];
const permission = usePermission();
const appStore = useAppStore();
const router = useRouter();
const {t} = useI18n();
const activeMenus: Ref<RouteRecordName[]> = ref([]);
const checkAuthMenu = () => {
  const topMenus = appStore.getTopMenus(appStore.state);
  if (appStore.state.packageType === 'community') {
    appStore.setTopMenus(topMenus.filter((item) => item.name !== RouteEnum.SETTING_SYSTEM_AUTHORIZED_MANAGEMENT));
  } else {
    appStore.setTopMenus(topMenus);
  }
}
const setCurrentTopMenu = (key: string) => {
  const secParentFullSame = appStore.state.topMenus.find((route: RouteRecordRaw) => {
    return key === route?.name;
  });
  // 非全等的情况下，一定是父子路由包含关系
  const secParentLike = appStore.state.topMenus.find((route: RouteRecordRaw) => {
    return key.includes(route?.name as string);
  });
  if (secParentFullSame) {
    appStore.setCurrentTopMenu(secParentFullSame);
  } else if (secParentLike) {
    appStore.setCurrentTopMenu(secParentLike);
  }
}
listenerRouteChange((newRoute) => {
  const {name} = newRoute;
  for (let i = 0; i < copyRouters.length; i++) {
    const firstRoute = copyRouters[i];
    // 权限校验通过
    if (permission.accessRouter(firstRoute)) {
      if (name && firstRoute?.name && (name as string).includes(firstRoute.name as string)) {
        // 先判断二级菜单是否顶部菜单
        let currentParent = firstRoute?.children?.some((item) => item.meta?.isTopMenu)
            ? (firstRoute as RouteRecordRaw)
            : undefined;
        if (!currentParent) {
          // 二级菜单非顶部菜单，则判断三级菜单是否有顶部菜单
          currentParent = firstRoute?.children?.find(
              (item) => name && item?.name && (name as string).includes(item.name as string)
          );
        }
        const filterMenuTopRouter =
            currentParent?.children?.filter((item: any) => permission.accessRouter(item) && item.meta?.isTopMenu) || [];
        appStore.setTopMenus(filterMenuTopRouter);
        setCurrentTopMenu(name as string);
        return;
      }
    }
  }
  // 切换到没有顶部菜单的路由时，清空顶部菜单
  appStore.setTopMenus([]);
  setCurrentTopMenu('');
}, true);
const menuOptions = ref<MenuOption[]>([])
watch(
    () => appStore.getCurrentTopMenu(appStore.state)?.name,
    (val) => {
      checkAuthMenu();
      activeMenus.value = [val || ''];
    },
    {
      immediate: true,
    }
);
watch(
    () => appStore.state.packageType,
    (val) => {
      checkAuthMenu();
    }
);
watchEffect(() => {
  const menus = appStore.state.topMenus;
  menuOptions.value = menus.map(menu => ({
    key: menu.name,
    label: () => renderRouterLink(String(menu.name), menu.meta?.locale as string)
  }));
})
</script>

<template>
  <n-menu
      v-model:value="activeMenus[0]"
      mode="horizontal"
      :options="menuOptions"
      responsive

  />
</template>

<style scoped>

</style>