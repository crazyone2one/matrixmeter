import { RouteRecordRaw } from "vue-router";

export const NOT_FOUND_ROUTE: RouteRecordRaw = {
  path: "/:pathMatch(.*)*",
  name: "notFound",
  component: () => import("/@/views/base/not-found/index.vue"),
};
export const DEFAULT_LAYOUT = () => import("/@/layout/index.vue");
