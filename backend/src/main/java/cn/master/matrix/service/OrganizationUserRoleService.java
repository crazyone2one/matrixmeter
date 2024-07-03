package cn.master.matrix.service;

import cn.master.matrix.entity.User;
import cn.master.matrix.entity.UserRole;
import cn.master.matrix.payload.dto.permission.PermissionDefinitionItem;
import cn.master.matrix.payload.dto.request.OrganizationUserRoleMemberEditRequest;
import cn.master.matrix.payload.dto.request.OrganizationUserRoleMemberRequest;
import cn.master.matrix.payload.dto.request.PermissionSettingUpdateRequest;
import cn.master.matrix.payload.dto.user.UserExtendDTO;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

/**
 * @author Created by 11's papa on 07/03/2024
 **/
public interface OrganizationUserRoleService extends BaseUserRoleService {
    @Override
    UserRole add(UserRole userRole);

    @Override
    UserRole update(UserRole userRole);

    void delete(String id, String userId);

    List<UserRole> list(String organizationId);

    List<PermissionDefinitionItem> getPermissionSetting(String id);

    @Override
    void updatePermissionSetting(PermissionSettingUpdateRequest request);

    @Override
    List<UserExtendDTO> getMember(String organizationId, String roleId, String keyword);

    Page<User> listMember(OrganizationUserRoleMemberRequest request);

    void addMember(OrganizationUserRoleMemberEditRequest request, String createUserId);

    void removeMember(OrganizationUserRoleMemberEditRequest request);
}
