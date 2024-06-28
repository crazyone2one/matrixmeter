package cn.master.matrix.security;

import cn.master.matrix.service.UserRolePermissionService;

/**
 * @author Created by 11's papa on 06/27/2024
 **/

public class SecurityService {
    private final UserRolePermissionService userRolePermissionService;

    public SecurityService(UserRolePermissionService userRolePermissionService) {
        this.userRolePermissionService = userRolePermissionService;
    }

    public boolean hasPermission(String permission) {
        return hasAnyPermissions(permission);
    }

    public boolean hasAnyPermissions(String... permissions) {
        return userRolePermissionService.hasAnyPermissions(permissions);
    }
}
