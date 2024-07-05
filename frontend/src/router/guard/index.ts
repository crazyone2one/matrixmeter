import type { Router } from "vue-router";
import setupPermissionGuard from "./permission";
import setupUserLoginInfoGuard from "./user-login-info";
import { MENU_LEVEL, PathMapRoute } from "/@/config/pathMap";
import usePathMap from "/@/hooks/use-path-map";
import { useAppStore } from "/@/store";
import { setRouteEmitter } from "/@/utils/route-listener";

const setupPageGuard = (router: Router) => {
  const { getRouteLevelByKey } = usePathMap();
  router.beforeEach((to, _from, next) => {
    // 监听路由变化
    setRouteEmitter(to);
    const appStore = useAppStore();
    const urlOrgId = to.query.orgId;
    const urlProjectId = to.query.pId;
    if (urlOrgId) {
      appStore.setCurrentOrgId(urlOrgId as string);
    }
    if (urlProjectId) {
      appStore.setCurrentProjectId(urlProjectId as string);
    }
    switch (getRouteLevelByKey(to.name as PathMapRoute)) {
      case MENU_LEVEL[1]: // 组织级别的页面，需要给页面携带上组织 ID
        if (urlOrgId === undefined) {
          to.query = {
            ...to.query,
            orgId: appStore.state.currentOrgId,
          };
          next(to);
          return;
        }
        break;
      case MENU_LEVEL[2]: // 项目级别的页面，需要给页面携带上组织 ID和项目 ID
        if (urlOrgId === undefined && urlProjectId === undefined) {
          to.query = {
            ...to.query,
            orgId: appStore.state.currentOrgId,
            pId: appStore.state.currentProjectId,
          };

          next(to);
          return;
        }
        break;
      case MENU_LEVEL[0]: // 系统级别的页面，无需携带组织ID和项目ID
      default:
        break;
    }
    next();
  });
};

export default function createRouteGuard(router: Router) {
  // 设置路由监听守卫
  setupPageGuard(router);
  // 设置用户登录校验守卫
  setupUserLoginInfoGuard(router);
  // 设置菜单权限守卫
  setupPermissionGuard(router);
}
