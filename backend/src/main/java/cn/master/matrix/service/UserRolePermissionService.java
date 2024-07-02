package cn.master.matrix.service;

import cn.master.matrix.payload.dto.request.PermissionSettingUpdateRequest;
import cn.master.matrix.payload.dto.user.UserRolePermissionDTO;
import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.UserRolePermission;

import java.util.List;
import java.util.Set;

/**
 * 用户组权限 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-27T16:23:10.923916400
 */
public interface UserRolePermissionService extends IService<UserRolePermission> {
    UserRolePermissionDTO getUserRolePermission(String userId);

    boolean hasAnyPermissions(String... permissions);

    Set<String> getPermissionIdSetByRoleId(String roleId);

    List<UserRolePermission> getByRoleId(String roleId);

    void updatePermissionSetting(PermissionSettingUpdateRequest request);

    void deleteByRoleId(String roleId);
}
