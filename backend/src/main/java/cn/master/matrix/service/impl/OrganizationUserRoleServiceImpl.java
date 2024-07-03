package cn.master.matrix.service.impl;

import cn.master.matrix.config.PermissionCache;
import cn.master.matrix.constants.InternalUserRole;
import cn.master.matrix.constants.UserRoleEnum;
import cn.master.matrix.constants.UserRoleType;
import cn.master.matrix.entity.User;
import cn.master.matrix.entity.UserRole;
import cn.master.matrix.entity.UserRoleRelation;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.payload.dto.permission.PermissionDefinitionItem;
import cn.master.matrix.payload.dto.request.OrganizationUserRoleMemberEditRequest;
import cn.master.matrix.payload.dto.request.OrganizationUserRoleMemberRequest;
import cn.master.matrix.payload.dto.request.PermissionSettingUpdateRequest;
import cn.master.matrix.payload.dto.user.UserExtendDTO;
import cn.master.matrix.service.BaseUserRoleRelationService;
import cn.master.matrix.service.OrganizationUserRoleService;
import cn.master.matrix.service.UserRolePermissionService;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static cn.master.matrix.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static cn.master.matrix.entity.table.UserRoleTableDef.USER_ROLE;
import static cn.master.matrix.entity.table.UserTableDef.USER;
import static cn.master.matrix.exception.SystemResultCode.NO_GLOBAL_USER_ROLE_PERMISSION;
import static cn.master.matrix.exception.SystemResultCode.NO_ORG_USER_ROLE_PERMISSION;

/**
 * @author Created by 11's papa on 07/03/2024
 **/
@Service
public class OrganizationUserRoleServiceImpl extends BaseUserRoleServiceImpl implements OrganizationUserRoleService {
    public OrganizationUserRoleServiceImpl(UserRolePermissionService userRolePermissionService,
                                           PermissionCache permissionCache,
                                           BaseUserRoleRelationService baseUserRoleRelationService) {
        super(userRolePermissionService, permissionCache, baseUserRoleRelationService);
    }

    @Override
    public UserRole add(UserRole userRole) {
        userRole.setInternal(false);
        userRole.setType(UserRoleType.ORGANIZATION.name());
        checkNewRoleExist(userRole);
        return super.add(userRole);
    }

    @Override
    public UserRole update(UserRole userRole) {
        UserRole oldRole = get(userRole.getId());
        // 非组织用户组不允许修改, 全局用户组不允许修改
        checkOrgUserRole(oldRole);
        checkGlobalUserRole(oldRole);
        userRole.setType(UserRoleType.ORGANIZATION.name());
        checkNewRoleExist(userRole);
        return super.update(userRole);
    }

    @Override
    public void delete(String id, String userId) {
        UserRole userRole = get(id);
        // 非组织用户组不允许删除, 内置全局用户组不允许删除
        checkOrgUserRole(userRole);
        checkGlobalUserRole(userRole);
        super.delete(userRole, InternalUserRole.ORG_MEMBER.getValue(), userId, userRole.getScopeId());
    }

    @Override
    public void updatePermissionSetting(PermissionSettingUpdateRequest request) {
        UserRole userRole = get(request.getUserRoleId());
        checkOrgUserRole(userRole);
        checkGlobalUserRole(userRole);
        super.updatePermissionSetting(request);
    }

    @Override
    public List<UserExtendDTO> getMember(String organizationId, String roleId, String keyword) {
        return super.getMember(organizationId, roleId, keyword);
    }

    @Override
    public Page<User> listMember(OrganizationUserRoleMemberRequest request) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.select(USER.ALL_COLUMNS).from(USER_ROLE_RELATION)
                .leftJoin(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getOrganizationId())
                        .and(USER_ROLE_RELATION.ROLE_ID.eq(request.getUserRoleId()))
                        .and(USER.NAME.like(request.getKeyword())
                                .or(USER.EMAIL.like(request.getKeyword()))
                                .or(USER.PHONE.like(request.getKeyword()))))
                .orderBy(USER_ROLE_RELATION.CREATE_TIME.desc());
        return super.baseUserRoleRelationService.pageAs(Page.of(request.getPageNum(), request.getPageSize()), wrapper, User.class);
    }

    @Override
    public void addMember(OrganizationUserRoleMemberEditRequest request, String createUserId) {
        request.getUserIds().forEach(userId -> {
            checkMemberParam(userId, request.getUserRoleId());
            UserRoleRelation relation = new UserRoleRelation();
            relation.setUserId(userId);
            relation.setRoleId(request.getUserRoleId());
            relation.setSourceId(request.getOrganizationId());
            relation.setCreateUser(createUserId);
            relation.setOrganizationId(request.getOrganizationId());
            super.baseUserRoleRelationService.save(relation);
        });
    }

    @Override
    public void removeMember(OrganizationUserRoleMemberEditRequest request) {
        String removeUserId = request.getUserIds().get(0);
        checkMemberParam(removeUserId, request.getUserRoleId());
        //检查移除的是不是管理员
        if (StringUtils.equals(request.getUserRoleId(), InternalUserRole.ORG_ADMIN.getValue())) {
            val userRoleRelationQueryChain = QueryChain.of(UserRoleRelation.class)
                    .where(USER_ROLE_RELATION.USER_ID.eq(removeUserId)
                            .and(USER_ROLE_RELATION.SOURCE_ID.eq(request.getOrganizationId()))
                            .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.ORG_ADMIN.getValue())));
            if (super.baseUserRoleRelationService.count(userRoleRelationQueryChain) == 0) {
                throw new CustomException(Translator.get("keep_at_least_one_administrator"));
            }
        }
        val queryChain = QueryChain.of(UserRoleRelation.class)
                .where(USER_ROLE_RELATION.USER_ID.eq(removeUserId)
                        .and(USER_ROLE_RELATION.SOURCE_ID.eq(request.getOrganizationId()))
                        .and(USER_ROLE_RELATION.ROLE_ID.ne(request.getUserRoleId())));
        if (super.baseUserRoleRelationService.count(queryChain) == 0) {
            throw new CustomException(Translator.get("org_at_least_one_user_role_require"));
        }
        val where = QueryChain.of(UserRoleRelation.class)
                .where(USER_ROLE_RELATION.USER_ID.eq(removeUserId)
                        .and(USER_ROLE_RELATION.SOURCE_ID.eq(request.getOrganizationId()))
                        .and(USER_ROLE_RELATION.ROLE_ID.eq(request.getUserRoleId())));
        LogicDeleteManager.execWithoutLogicDelete(() -> super.baseUserRoleRelationService.remove(where));
    }

    @Override
    public List<UserRole> list(String organizationId) {
        val userRoles = queryChain().where(USER_ROLE.SCOPE_ID.in(Arrays.asList(organizationId, UserRoleEnum.GLOBAL.toString()))
                        .and(USER_ROLE.TYPE.eq(UserRoleType.ORGANIZATION.name())))
                .orderBy(USER_ROLE.CREATE_TIME.asc()).list();

        userRoles.sort(Comparator.comparing(UserRole::getInternal).thenComparing(UserRole::getScopeId)
                .thenComparing(Comparator.comparing(UserRole::getCreateTime).thenComparing(UserRole::getId).reversed()).reversed());
        return userRoles;
    }

    @Override
    public List<PermissionDefinitionItem> getPermissionSetting(String id) {
        UserRole userRole = get(id);
        checkOrgUserRole(userRole);
        return getPermissionSetting(userRole);
    }

    public void checkGlobalUserRole(UserRole userRole) {
        if (StringUtils.equals(userRole.getScopeId(), UserRoleEnum.GLOBAL.toString())) {
            throw new CustomException(NO_GLOBAL_USER_ROLE_PERMISSION);
        }
    }

    private void checkOrgUserRole(UserRole userRole) {
        if (!UserRoleType.ORGANIZATION.name().equals(userRole.getType())) {
            throw new CustomException(NO_ORG_USER_ROLE_PERMISSION);
        }
    }

    public void checkNewRoleExist(UserRole userRole) {
        List<UserRole> userRoles = queryChain().where(USER_ROLE.NAME.eq(userRole.getName())
                        .and(USER_ROLE.SCOPE_ID.in(Arrays.asList(userRole.getScopeId(), UserRoleEnum.GLOBAL.toString())))
                        .and(USER_ROLE.TYPE.eq(userRole.getType())))
                .and(USER_ROLE.ID.ne(userRole.getId())).list();
        if (CollectionUtils.isNotEmpty(userRoles)) {
            throw new CustomException(Translator.get("user_role_exist"));
        }
    }
}
