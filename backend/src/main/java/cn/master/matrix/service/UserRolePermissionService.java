package cn.master.matrix.service;

import cn.master.matrix.entity.dto.user.UserRolePermissionDTO;
import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.UserRolePermission;

/**
 * 用户组权限 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-27T16:23:10.923916400
 */
public interface UserRolePermissionService extends IService<UserRolePermission> {
    UserRolePermissionDTO getUserRolePermission(String userId);

    boolean hasAnyPermissions(String... permissions);
}
