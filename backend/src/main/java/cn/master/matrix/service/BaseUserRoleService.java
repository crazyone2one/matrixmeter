package cn.master.matrix.service;

import cn.master.matrix.payload.dto.permission.PermissionDefinitionItem;
import cn.master.matrix.payload.dto.request.PermissionSettingUpdateRequest;
import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.UserRole;

import java.util.List;

/**
 * 用户组 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-27T16:19:57.911393700
 */
public interface BaseUserRoleService extends IService<UserRole> {
    UserRole getWithCheck(String id);

    List<PermissionDefinitionItem> getPermissionSetting(UserRole userRole);

    UserRole checkResourceExist(UserRole userRole);

    void checkInternalUserRole(UserRole userRole);

    void checkAdminUserRole(UserRole userRole);

    void updatePermissionSetting(PermissionSettingUpdateRequest request);

    UserRole add(UserRole userRole);

    UserRole update(UserRole userRole);

    void delete(UserRole userRole, String value, String userId, String system);
}
