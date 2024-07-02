package cn.master.matrix.service.impl;

import cn.master.matrix.config.PermissionCache;
import cn.master.matrix.constants.InternalUserRole;
import cn.master.matrix.entity.UserRole;
import cn.master.matrix.entity.UserRoleRelation;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.mapper.UserRoleMapper;
import cn.master.matrix.payload.dto.permission.Permission;
import cn.master.matrix.payload.dto.permission.PermissionDefinitionItem;
import cn.master.matrix.payload.dto.request.PermissionSettingUpdateRequest;
import cn.master.matrix.service.BaseUserRoleRelationService;
import cn.master.matrix.service.BaseUserRoleService;
import cn.master.matrix.service.UserRolePermissionService;
import cn.master.matrix.util.JsonUtils;
import cn.master.matrix.util.ServiceUtils;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static cn.master.matrix.exception.CommonResultCode.ADMIN_USER_ROLE_PERMISSION;
import static cn.master.matrix.exception.CommonResultCode.INTERNAL_USER_ROLE_PERMISSION;

/**
 * 用户组 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-27T16:19:57.911393700
 */
@Service("baseUserRoleService")
@RequiredArgsConstructor
public class BaseUserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements BaseUserRoleService {

    private final UserRolePermissionService userRolePermissionService;
    private final PermissionCache permissionCache;
    private final BaseUserRoleRelationService baseUserRoleRelationService;

    @Override
    public UserRole getWithCheck(String id) {
        return checkResourceExist(mapper.selectOneById(id));
    }

    @Override
    public List<PermissionDefinitionItem> getPermissionSetting(UserRole userRole) {
        // 获取该用户组拥有的权限
        Set<String> permissionIds = userRolePermissionService.getPermissionIdSetByRoleId(userRole.getId());
        // 获取所有的权限
        List<PermissionDefinitionItem> permissionDefinition = permissionCache.getPermissionDefinition();
        // 深拷贝
        permissionDefinition = JsonUtils.parseArray(JsonUtils.toJsonString(permissionDefinition), PermissionDefinitionItem.class);
        // 过滤该用户组级别的菜单，例如系统级别
        permissionDefinition = permissionDefinition.stream()
                .filter(item -> StringUtils.equals(item.getType(), userRole.getType()))
                .toList();
        for (PermissionDefinitionItem firstLevel : permissionDefinition) {
            List<PermissionDefinitionItem> children = firstLevel.getChildren();
            boolean allCheck = true;
            firstLevel.setName(Translator.get(firstLevel.getName()));
            for (PermissionDefinitionItem secondLevel : children) {
                List<Permission> permissions = secondLevel.getPermissions();
                secondLevel.setName(Translator.get(secondLevel.getName()));
                if (CollectionUtils.isEmpty(permissions)) {
                    continue;
                }
                boolean secondAllCheck = true;
                for (Permission p : permissions) {
                    if (StringUtils.isNotBlank(p.getName())) {
                        // 有 name 字段翻译 name 字段
                        p.setName(Translator.get(p.getName()));
                    } else {
                        p.setName(translateDefaultPermissionName(p));
                    }
                    if (permissionIds.contains(p.getId())) {
                        p.setEnable(true);
                    } else {
                        // 如果权限有未勾选，则二级菜单设置为未勾选
                        p.setEnable(false);
                        secondAllCheck = false;
                    }
                }
                secondLevel.setEnable(secondAllCheck);
                if (!secondAllCheck) {
                    // 如果二级菜单有未勾选，则一级菜单设置为未勾选
                    allCheck = false;
                }
            }
            firstLevel.setEnable(allCheck);
        }
        return permissionDefinition;
    }

    private String translateDefaultPermissionName(Permission p) {
        //if (StringUtils.isNotBlank(p.getName())) {
        //    p.getName();
        //}
        String[] idSplit = p.getId().split(":");
        String permissionKey = idSplit[idSplit.length - 1];
        Map<String, String> translationMap = new HashMap<>() {{
            put("READ", "permission.read");
            put("READ+ADD", "permission.add");
            put("READ+UPDATE", "permission.edit");
            put("READ+DELETE", "permission.delete");
            put("READ+IMPORT", "permission.import");
            put("READ+RECOVER", "permission.recover");
            put("READ+EXPORT", "permission.export");
            put("READ+EXECUTE", "permission.execute");
            put("READ+DEBUG", "permission.debug");
        }};
        return Translator.get(translationMap.get(permissionKey));
    }

    @Override
    public UserRole checkResourceExist(UserRole userRole) {
        return ServiceUtils.checkResourceExist(userRole, "permission.system_user_role.name");
    }

    @Override
    public void checkInternalUserRole(UserRole userRole) {
        if (BooleanUtils.isTrue(userRole.getInternal())) {
            throw new CustomException(INTERNAL_USER_ROLE_PERMISSION);
        }
    }

    @Override
    public void checkAdminUserRole(UserRole userRole) {
        if (StringUtils.equalsAny(userRole.getId(), InternalUserRole.ADMIN.getValue(),
                InternalUserRole.ORG_ADMIN.getValue(), InternalUserRole.PROJECT_ADMIN.getValue())) {
            throw new CustomException(ADMIN_USER_ROLE_PERMISSION);
        }
    }

    @Override
    public void updatePermissionSetting(PermissionSettingUpdateRequest request) {
        userRolePermissionService.updatePermissionSetting(request);
    }

    @Override
    public UserRole add(UserRole userRole) {
        mapper.insert(userRole);
        return userRole;
    }

    @Override
    public UserRole update(UserRole userRole) {
        mapper.update(userRole);
        return userRole;
    }

    @Override
    public void delete(UserRole userRole, String defaultRoleId, String userId, String orgId) {
        String id = userRole.getId();
        checkInternalUserRole(userRole);
        userRolePermissionService.deleteByRoleId(id);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteById(id));
        // 检查是否只有一个用户组，如果是则添加系统成员等默认用户组
        checkOneLimitRole(id, defaultRoleId, userId, orgId);
        // 删除用户组与用户的关联关系
        baseUserRoleRelationService.deleteByRoleId(id);
    }

    private void checkOneLimitRole(String roleId, String defaultRoleId, String userId, String orgId) {
        // 查询要删除的用户组关联的用户ID
        List<String> userIds = baseUserRoleRelationService.getUserIdByRoleId(roleId);

        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        // 查询用户列表与所有用户组的关联关系，并分组（UserRoleRelation 中只有 userId 和 sourceId）
        Map<String, List<UserRoleRelation>> userRoleRelationMap = baseUserRoleRelationService
                .getUserIdAndSourceIdByUserIds(userIds)
                .stream()
                .collect(Collectors.groupingBy(i -> i.getUserId() + i.getSourceId()));
        List<UserRoleRelation> addRelations = new ArrayList<>();
        userRoleRelationMap.forEach((groupId, relations) -> {
            // 如果当前用户组只有一个用户，并且就是要删除的用户组，则添加组织成员等默认用户组
            if (relations.size() == 1 && StringUtils.equals(relations.get(0).getRoleId(), roleId)) {
                UserRoleRelation relation = new UserRoleRelation();
                relation.setUserId(relations.get(0).getUserId());
                relation.setSourceId(relations.get(0).getSourceId());
                relation.setRoleId(defaultRoleId);
                relation.setCreateUser(userId);
                relation.setOrganizationId(orgId);
                addRelations.add(relation);
            }
        });
        baseUserRoleRelationService.batchInsert(addRelations);
    }
}
