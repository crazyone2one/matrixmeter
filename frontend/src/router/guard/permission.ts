import type { Router } from "vue-router";
import usePermission from "/@/hooks/use-permission";
export default function setupPermissionGuard(router: Router) {
  router.beforeEach(async (to, _from, next) => {
    const Permission = usePermission();
    const permissionsAllow = Permission.accessRouter(to);

    if (permissionsAllow) {
      next();
    } else {
      next({
        name: "no-resource",
      });
    }
  });
}
