package cn.master.matrix.service.impl;

import cn.master.matrix.config.PermissionCache;
import cn.master.matrix.constants.UserRoleScope;
import cn.master.matrix.constants.UserRoleType;
import cn.master.matrix.entity.UserRole;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.payload.dto.permission.PermissionDefinitionItem;
import cn.master.matrix.payload.dto.request.PermissionSettingUpdateRequest;
import cn.master.matrix.payload.dto.user.response.UserSelectOption;
import cn.master.matrix.service.BaseUserRoleRelationService;
import cn.master.matrix.service.GlobalUserRoleService;
import cn.master.matrix.service.UserRolePermissionService;
import cn.master.matrix.util.Translator;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

import static cn.master.matrix.constants.InternalUserRole.MEMBER;
import static cn.master.matrix.entity.table.UserRoleTableDef.USER_ROLE;
import static cn.master.matrix.exception.SystemResultCode.*;

/**
 * @author Created by 11's papa on 06/28/2024
 **/
@Service
public class GlobalUserRoleServiceImpl extends BaseUserRoleServiceImpl implements GlobalUserRoleService {
    public GlobalUserRoleServiceImpl(UserRolePermissionService userRolePermissionService,
                                     PermissionCache permissionCache,
                                     BaseUserRoleRelationService baseUserRoleRelationService) {
        super(userRolePermissionService, permissionCache, baseUserRoleRelationService);
    }

    @Override
    public void checkRoleIsGlobalAndHaveMember(List<String> roleIdList, boolean isSystem) {
        val globalRoleList = queryChain().select(USER_ROLE.ID).from(USER_ROLE)
                .where(USER_ROLE.ID.in(roleIdList)
                        .and(USER_ROLE.SCOPE_ID.eq("global"))
                        .and(USER_ROLE.TYPE.eq("SYSTEM").when(isSystem)))
                .listAs(String.class);
        if (globalRoleList.size() != roleIdList.size()) {
            throw new CustomException(Translator.get("role.not.global"));
        }
    }

    @Override
    public List<UserSelectOption> getGlobalSystemRoleList() {
        val userRoles = queryChain().where(USER_ROLE.SCOPE_ID.eq(UserRoleScope.GLOBAL)
                .and(USER_ROLE.TYPE.eq(UserRoleType.SYSTEM.name()))).list();
        List<UserSelectOption> returnList = new ArrayList<>();
        userRoles.forEach(userRole -> {
            UserSelectOption option = new UserSelectOption();
            option.setId(userRole.getId());
            option.setName(userRole.getName());
            option.setSelected(StringUtils.equals(userRole.getId(), MEMBER.getValue()));
            option.setCloseable(!StringUtils.equals(userRole.getId(), MEMBER.getValue()));
            returnList.add(option);
        });
        return returnList;
    }

    @Override
    public void checkSystemUserGroup(UserRole userRole) {
        if (!StringUtils.equalsIgnoreCase(userRole.getType(), UserRoleType.SYSTEM.name())) {
            throw new CustomException(GLOBAL_USER_ROLE_RELATION_SYSTEM_PERMISSION);
        }
    }

    @Override
    public void checkGlobalUserRole(UserRole userRole) {
        if (!StringUtils.equals(userRole.getScopeId(), UserRoleScope.GLOBAL)) {
            throw new CustomException(GLOBAL_USER_ROLE_PERMISSION);
        }
    }

    @Override
    public List<UserRole> list() {
        val userRoles = queryChain().where(USER_ROLE.SCOPE_ID.eq(UserRoleScope.GLOBAL)).list();
        // 先按照类型排序，再按照创建时间排序
        userRoles.sort(Comparator.comparingInt(this::getTypeOrder)
                .thenComparingInt(item -> getInternal(item.getInternal()))
                .thenComparing(UserRole::getCreateTime));
        return userRoles;
    }

    @Override
    public List<PermissionDefinitionItem> getPermissionSetting(String id) {
        UserRole userRole = getWithCheck(id);
        checkGlobalUserRole(userRole);
        return getPermissionSetting(userRole);
    }

    @Override
    public void updatePermissionSetting(PermissionSettingUpdateRequest request) {
        UserRole userRole = getWithCheck(request.getUserRoleId());
        checkGlobalUserRole(userRole);
        // 内置管理员级别用户组无法更改权限
        checkAdminUserRole(userRole);
        super.updatePermissionSetting(request);
    }

    @Override
    public UserRole add(UserRole userRole) {
        userRole.setInternal(false);
        userRole.setScopeId(UserRoleScope.GLOBAL);
        checkExist(userRole);
        return super.add(userRole);
    }

    @Override
    public UserRole update(UserRole userRole) {
        UserRole originUserRole = getWithCheck(userRole.getId());
        checkGlobalUserRole(originUserRole);
        checkInternalUserRole(originUserRole);
        userRole.setInternal(false);
        checkExist(userRole);
        return super.update(userRole);
    }

    @Override
    public void delete(String id, String userId) {
        UserRole userRole = getWithCheck(id);
        checkGlobalUserRole(userRole);
        super.delete(userRole, MEMBER.getValue(), userId, UserRoleScope.SYSTEM);
    }

    private void checkExist(UserRole userRole) {
        if (StringUtils.isBlank(userRole.getName())) {
            return;
        }
        val userRoles = queryChain().where(USER_ROLE.NAME.eq(userRole.getName())
                .and(USER_ROLE.SCOPE_ID.eq(UserRoleScope.GLOBAL))
                .and(USER_ROLE.ID.ne(userRole.getId()))).list();
        if (CollectionUtils.isNotEmpty(userRoles)) {
            throw new CustomException(GLOBAL_USER_ROLE_EXIST);
        }
    }

    private int getInternal(Boolean internal) {
        return BooleanUtils.isTrue(internal) ? 0 : 1;
    }

    private int getTypeOrder(UserRole userRole) {
        Map<String, Integer> typeOrderMap = new HashMap<>(3) {{
            put(UserRoleType.SYSTEM.name(), 1);
            put(UserRoleType.ORGANIZATION.name(), 2);
            put(UserRoleType.PROJECT.name(), 3);
        }};
        return typeOrderMap.getOrDefault(userRole.getType(), 0);
    }
}
