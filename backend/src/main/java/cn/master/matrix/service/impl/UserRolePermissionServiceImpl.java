package cn.master.matrix.service.impl;

import cn.master.matrix.entity.UserRole;
import cn.master.matrix.entity.UserRolePermission;
import cn.master.matrix.entity.UserRoleRelation;
import cn.master.matrix.mapper.UserRolePermissionMapper;
import cn.master.matrix.payload.dto.request.PermissionSettingUpdateRequest;
import cn.master.matrix.payload.dto.user.UserRolePermissionDTO;
import cn.master.matrix.payload.dto.user.UserRoleResourceDTO;
import cn.master.matrix.service.UserRolePermissionService;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户组权限 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-27T16:23:10.923916400
 */
@Service
public class UserRolePermissionServiceImpl extends ServiceImpl<UserRolePermissionMapper, UserRolePermission> implements UserRolePermissionService {

    @Override
    public UserRolePermissionDTO getUserRolePermission(String userId) {
        UserRolePermissionDTO permissionDTO = new UserRolePermissionDTO();
        List<UserRoleResourceDTO> list = new ArrayList<>();
        val userRoleRelations = QueryChain.of(UserRoleRelation.class).where(UserRoleRelation::getUserId).eq(userId).list();
        if (CollectionUtils.isEmpty(userRoleRelations)) {
            return permissionDTO;
        }
        permissionDTO.setUserRoleRelations(userRoleRelations);
        List<String> roleList = userRoleRelations.stream().map(UserRoleRelation::getRoleId).toList();
        val userRoles = QueryChain.of(UserRole.class).where(UserRole::getId).in(roleList).list();
        permissionDTO.setUserRoles(userRoles);
        for (UserRole userRole : userRoles) {
            UserRoleResourceDTO dto = new UserRoleResourceDTO();
            dto.setUserRole(userRole);
            val userRolePermissions = QueryChain.of(UserRolePermission.class).where(UserRolePermission::getRoleId).eq(userRole.getId()).list();
            dto.setUserRolePermissions(userRolePermissions);
            list.add(dto);
        }
        permissionDTO.setList(list);
        return permissionDTO;
    }

    @Override
    public boolean hasAnyPermissions(String... permissions) {
        val authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        // todo 将Permissions数据存在缓存
        val userRolePermissions = QueryChain.of(UserRolePermission.class).where(UserRolePermission::getRoleId).in(authorities).list();
        val permissionsInDb = userRolePermissions.stream().map(UserRolePermission::getPermissionId).toList().stream().distinct().toList();
        val tmpAuthorities = Arrays.asList(permissions[0].split(","));
        return tmpAuthorities.stream().anyMatch(permissionsInDb::contains);
    }

    @Override
    public Set<String> getPermissionIdSetByRoleId(String roleId) {
        return getByRoleId(roleId).stream()
                .map(UserRolePermission::getPermissionId)
                .collect(Collectors.toSet());
    }

    @Override
    public List<UserRolePermission> getByRoleId(String roleId) {
        return queryChain().where(UserRolePermission::getRoleId).eq(roleId).list();
    }

    @Override
    public void updatePermissionSetting(PermissionSettingUpdateRequest request) {
        List<PermissionSettingUpdateRequest.PermissionUpdateRequest> permissions = request.getPermissions();
        // 先删除
        val queryChain = queryChain().where(UserRolePermission::getRoleId).eq(request.getUserRoleId());
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain));
        // 再新增
        String groupId = request.getUserRoleId();
        permissions.forEach(permission -> {
            if (BooleanUtils.isTrue(permission.getEnable())) {
                String permissionId = permission.getId();
                UserRolePermission groupPermission = new UserRolePermission();
                groupPermission.setRoleId(groupId);
                groupPermission.setPermissionId(permissionId);
                mapper.insert(groupPermission);
            }
        });
    }

    @Override
    public void deleteByRoleId(String roleId) {
        val queryChain = queryChain().where(UserRolePermission::getRoleId).eq(roleId);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain));
    }
}
