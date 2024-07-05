import {
  RouteLocationNormalized,
  RouteRecordNormalized,
  RouteRecordRaw,
} from "vue-router";
import appRoutes from "/@/router/routes";
import { useUserStore } from "/@/store";
import {
  SystemScopeType,
  UserRole,
  UserRoleRelation,
} from "/@/store/modules/user/types";

export const composePermissions = (
  userRoleRelations: UserRoleRelation[],
  type: SystemScopeType,
  id: string
) => {
  if (type === "SYSTEM") {
    return userRoleRelations
      .filter((ur) => ur.userRole && ur.userRole.type === "SYSTEM")
      .flatMap((role) => role.userRolePermissions)
      .map((g) => g.permissionId);
  }
  let func: (role: UserRole) => boolean;
  switch (type) {
    case "PROJECT":
      func = (role) => role && role.type === "PROJECT";
      break;
    case "ORGANIZATION":
      func = (role) => role && role.type === "ORGANIZATION";
      break;
    default:
      func = (role) => role && role.type === "SYSTEM";
      break;
  }

  return userRoleRelations
    .filter((ur) => func(ur.userRole))
    .filter((ur) => ur.sourceId === id)
    .flatMap((role) => role.userRolePermissions)
    .map((g) => g.permissionId);
};

export const hasPermission = (permission: string, typeList: string[]) => {
  const userStore = useUserStore();
  if (userStore.isAdmin) {
    return true;
  }
  const { projectPermissions, orgPermissions, systemPermissions } =
    userStore.currentRole;

  if (
    projectPermissions.length === 0 &&
    orgPermissions.length === 0 &&
    systemPermissions.length === 0
  ) {
    return false;
  }

  if (typeList.includes("PROJECT") && projectPermissions.includes(permission)) {
    return true;
  }
  if (
    typeList.includes("ORGANIZATION") &&
    orgPermissions.includes(permission)
  ) {
    return true;
  }
  if (typeList.includes("SYSTEM") && systemPermissions.includes(permission)) {
    return true;
  }
  return false;
};

export const hasAnyPermission = (
  permissions: string[],
  typeList = ["PROJECT", "ORGANIZATION", "SYSTEM"]
) => {
  if (!permissions || permissions.length === 0) {
    return true;
  }
  return permissions.some((permission) => hasPermission(permission, typeList));
};

export const hasAllPermission = (
  permissions: string[],
  typeList = ["PROJECT", "ORGANIZATION", "SYSTEM"]
) => {
  if (!permissions || permissions.length === 0) {
    return true;
  }
  return permissions.every((permission) => hasPermission(permission, typeList));
};

export const topLevelMenuHasPermission = (
  route: RouteLocationNormalized | RouteRecordRaw
) => {
  const userStore = useUserStore();
  if (
    userStore.lastProjectId === "no_such_project" ||
    userStore.lastProjectId === ""
  ) {
    // 项目不存在, 不显示任何项目级别菜单, 展示无资源页面
    return false;
  }

  if (userStore.isAdmin) {
    // 如果是系统管理员, 包含项目, 组织, 系统层级所有菜单权限
    return true;
  }
  return hasAnyPermission(route.meta?.roles || []);
};

export const getFirstRouteNameByPermission = (
  routerList: RouteRecordNormalized[]
) => {
  const currentRoute = routerList
    .filter((item) => hasAnyPermission(item.meta.roles || [])) // 排除没有权限的路由
    .sort((a, b) => {
      // 如果 a 和 b 都有 order，按照 order 的值进行升序排序
      if (a.meta.order !== undefined && b.meta.order !== undefined) {
        return a.meta.order - b.meta.order;
      }
      // 如果 a 有 order 但是 b 没有 order，a 排前面
      if (a.meta.order !== undefined && b.meta.order === undefined) {
        return -1;
      }
      // 如果 a 没有 order 但是 b 有 order，b 排前面
      if (a.meta.order === undefined && b.meta.order !== undefined) {
        return 1;
      }
      // 如果 a 和 b 都没有 order，它们的位置不变
      return 0;
    })[0];
  return currentRoute?.name || "index";
};

export const routerNameHasPermission = (
  routerName: string,
  routerList: RouteRecordNormalized[]
) => {
  const currentRoute = routerList.find((item) => item.name === routerName);
  return currentRoute
    ? hasAnyPermission(currentRoute.meta?.roles || [])
    : false;
};
export function findRouteByName(name: string) {
  const queue: RouteRecordNormalized[] = [...appRoutes];
  while (queue.length > 0) {
    const currentRoute = queue.shift();
    if (!currentRoute) {
      return;
    }
    if (currentRoute.name === name) {
      return currentRoute;
    }
    if (currentRoute.children) {
      queue.push(...(currentRoute.children as RouteRecordNormalized[]));
    }
  }
  return null;
}
export function getFirstRouterNameByCurrentRoute(parentName: string) {
  const currentRoute = findRouteByName(parentName);
  if (currentRoute) {
    const hasAuthChildrenRouter = currentRoute.children.find((item) =>
      hasAnyPermission(item.meta?.roles || [])
    );
    return hasAuthChildrenRouter ? hasAuthChildrenRouter.name : parentName;
  }
  return parentName;
}
