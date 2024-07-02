package cn.master.matrix.service;

import cn.master.matrix.entity.UserRole;
import cn.master.matrix.payload.dto.permission.PermissionDefinitionItem;
import cn.master.matrix.payload.dto.request.PermissionSettingUpdateRequest;
import cn.master.matrix.payload.dto.user.response.UserSelectOption;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * @author Created by 11's papa on 06/28/2024
 **/
public interface GlobalUserRoleService extends BaseUserRoleService {
    void checkRoleIsGlobalAndHaveMember(@Valid @NotEmpty List<String> roleIdList, boolean isSystem);

    List<UserSelectOption> getGlobalSystemRoleList();

    void checkSystemUserGroup(UserRole userRole);

    void checkGlobalUserRole(UserRole userRole);

    @Override
    List<UserRole> list();

    List<PermissionDefinitionItem> getPermissionSetting(String id);

    @Override
    void updatePermissionSetting(PermissionSettingUpdateRequest request);

    @Override
    UserRole add(UserRole userRole);

    @Override
    UserRole update(UserRole userRole);

    void delete(String id, String userId);
}
