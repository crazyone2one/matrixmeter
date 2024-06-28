package cn.master.matrix.service.impl;

import cn.master.matrix.entity.UserRole;
import cn.master.matrix.entity.UserRolePermission;
import cn.master.matrix.entity.UserRoleRelation;
import cn.master.matrix.entity.dto.user.UserRolePermissionDTO;
import cn.master.matrix.entity.dto.user.UserRoleResourceDTO;
import cn.master.matrix.mapper.UserRolePermissionMapper;
import cn.master.matrix.service.UserRolePermissionService;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        val userRolePermissions = QueryChain.of(UserRolePermission.class).where(UserRolePermission::getRoleId).in(authorities).list();
        val list = userRolePermissions.stream().map(UserRolePermission::getPermissionId).toList().stream().distinct().toList();
        return Arrays.stream(permissions).anyMatch(list::contains);
    }
}
