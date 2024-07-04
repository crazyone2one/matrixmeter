import { createRouter, createWebHashHistory } from "vue-router";

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
  ], // 路由配置
  scrollBehavior() {
    return { top: 0 };
  },
});
router.afterEach((to) => {
  const items = [import.meta.env.VITE_APP_TITLE];
  to.meta.title != null && items.unshift(to.meta.title);
  document.title = items.join(" · ");
});
export default router;
