import { createRouter, createWebHashHistory } from "vue-router";
import createRouteGuard from "./guard";
import appRoutes from "./routes";
import { NOT_FOUND_ROUTE } from "./routes/base";

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: "/",
      name: "layout",
      component: () => import("/@/layout/index.vue"),
      children: [
        {
          path: "/",
          name: "home",
          component: () => import("/@/views/dashboard/index.vue"),
        },
      ],
    },
    {
      path: "/login",
      name: "login",
      component: () => import("/@/views/login/index.vue"),
      meta: {
        requiresAuth: false,
        title: "Sign In",
      },
    },
    ...appRoutes,
    NOT_FOUND_ROUTE,
  ], // 路由配置
  scrollBehavior() {
    return { top: 0 };
  },
});
createRouteGuard(router);
router.afterEach((to) => {
  const items = [import.meta.env.VITE_APP_TITLE];
  to.meta.title != null && items.unshift(to.meta.title);
  document.title = items.join(" · ");
});
export default router;
