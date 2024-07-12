package cn.master.matrix.service.impl;

import cn.master.matrix.constants.*;
import cn.master.matrix.entity.*;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.mapper.UserRoleRelationMapper;
import cn.master.matrix.payload.dto.LogDTO;
import cn.master.matrix.payload.dto.request.user.UserExcludeOptionDTO;
import cn.master.matrix.payload.dto.user.UserTableResponse;
import cn.master.matrix.service.BaseUserRoleRelationService;
import cn.master.matrix.service.OperationLogService;
import cn.master.matrix.util.JsonUtils;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static cn.master.matrix.constants.InternalUserRole.ADMIN;
import static cn.master.matrix.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static cn.master.matrix.entity.table.UserRoleTableDef.USER_ROLE;
import static cn.master.matrix.entity.table.UserTableDef.USER;
import static cn.master.matrix.exception.CommonResultCode.USER_ROLE_RELATION_EXIST;
import static cn.master.matrix.exception.CommonResultCode.USER_ROLE_RELATION_REMOVE_ADMIN_USER_PERMISSION;

/**
 * 用户组关系 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-27T16:20:34.166413800
 */
@Service("baseUserRoleRelationService")
@RequiredArgsConstructor
public class BaseUserRoleRelationServiceImpl extends ServiceImpl<UserRoleRelationMapper, UserRoleRelation> implements BaseUserRoleRelationService {
    private final OperationLogService operationLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserSystemGlobalRole(User user, String operator, List<String> roleList) {
        List<String> deleteRoleList = new ArrayList<>();
        List<UserRoleRelation> saveList = new ArrayList<>();
        List<UserRoleRelation> userRoleRelationList = queryChain().where(UserRoleRelation::getUserId).eq(user.getId())
                .and(UserRoleRelation::getRoleId).in(
                        QueryChain.of(UserRole.class).select(USER_ROLE.ID).from(USER_ROLE)
                                .where(USER_ROLE.TYPE.eq("SYSTEM")
                                        .and(USER_ROLE.SCOPE_ID.eq("global")))
                                .listAs(String.class)
                )
                .list();
        List<String> userSavedRoleIdList = userRoleRelationList.stream().map(UserRoleRelation::getRoleId).toList();
        //获取要移除的权限
        for (String userSavedRoleId : userSavedRoleIdList) {
            if (!roleList.contains(userSavedRoleId)) {
                deleteRoleList.add(userSavedRoleId);
            }
        }
        for (String roleId : roleList) {
            if (!userSavedRoleIdList.contains(roleId)) {
                val userRoleRelation = UserRoleRelation.builder()
                        .userId(user.getId())
                        .roleId(roleId)
                        .sourceId(UserRoleScope.SYSTEM)
                        .createUser(operator)
                        .organizationId(UserRoleScope.SYSTEM)
                        .build();
                saveList.add(userRoleRelation);
            }
        }
        if (CollectionUtils.isNotEmpty(deleteRoleList)) {
            List<String> deleteIdList = new ArrayList<>();
            userRoleRelationList.forEach(item -> {
                if (deleteRoleList.contains(item.getRoleId())) {
                    deleteIdList.add(item.getId());
                }
            });
            LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteBatchByIds(deleteIdList));
            operationLogService.batchAdd(this.getBatchLogs(deleteRoleList, user, "updateUser", operator, OperationLogType.DELETE.name()));
        }
        if (CollectionUtils.isNotEmpty(saveList)) {
            mapper.insertBatch(saveList);
        }
    }

    @Override
    public Map<String, UserTableResponse> selectGlobalUserRoleAndOrganization(List<String> userIdList) {
        val userRoleRelationList = queryChain().where(UserRoleRelation::getUserId).in(userIdList)
                .and(UserRoleRelation::getRoleId).in(
                        QueryChain.of(UserRole.class).select(USER_ROLE.ID).from(USER_ROLE)
                                .where(USER_ROLE.SCOPE_ID.eq("global"))
                                .listAs(String.class)
                )
                .list();
        List<String> userRoleIdList = userRoleRelationList.stream().map(UserRoleRelation::getRoleId).distinct().toList();
        List<String> sourceIdList = userRoleRelationList.stream().map(UserRoleRelation::getSourceId).distinct().toList();
        Map<String, UserRole> userRoleMap = new HashMap<>();
        Map<String, Organization> organizationMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(userRoleIdList)) {
            userRoleMap = QueryChain.of(UserRole.class).where(UserRole::getId).in(userRoleIdList)
                    .and(UserRole::getScopeId).eq(UserRoleEnum.GLOBAL.toString()).list()
                    .stream().collect(Collectors.toMap(UserRole::getId, item -> item));
        }
        if (CollectionUtils.isNotEmpty(sourceIdList)) {
            organizationMap = QueryChain.of(Organization.class).where(Organization::getId).in(sourceIdList).list()
                    .stream()
                    .collect(Collectors.toMap(Organization::getId, item -> item));
        }
        Map<String, UserTableResponse> returnMap = new HashMap<>();
        for (UserRoleRelation userRoleRelation : userRoleRelationList) {
            UserTableResponse userInfo = returnMap.get(userRoleRelation.getUserId());
            if (userInfo == null) {
                userInfo = new UserTableResponse();
                userInfo.setId(userRoleRelation.getUserId());
                returnMap.put(userRoleRelation.getUserId(), userInfo);
            }
            UserRole userRole = userRoleMap.get(userRoleRelation.getRoleId());
            if (userRole != null && StringUtils.equalsIgnoreCase(userRole.getType(), UserRoleScope.SYSTEM)) {
                userInfo.getUserRoleList().add(userRole);
            }
            Organization organization = organizationMap.get(userRoleRelation.getSourceId());
            if (organization != null && !userInfo.getOrganizationList().contains(organization)) {
                userInfo.getOrganizationList().add(organization);
            }
        }
        return returnMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByUserIdList(List<String> userIdList) {
        val queryChain = queryChain().where(UserRoleRelation::getUserId).in(userIdList);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain));
    }

    @Override
    public void batchSave(List<String> userRoleIdList, List<User> saveUserList) {
        List<UserRoleRelation> userRoleRelationSaveList = new ArrayList<>();
        for (String userRoleId : userRoleIdList) {
            for (User user : saveUserList) {
                val userRoleRelation = UserRoleRelation.builder()
                        .userId(user.getId())
                        .roleId(userRoleId)
                        .sourceId(UserRoleScope.SYSTEM)
                        .createUser(user.getCreateUser())
                        .organizationId(UserRoleScope.SYSTEM)
                        .build();
                userRoleRelationSaveList.add(userRoleRelation);
            }
        }
        mapper.insertBatch(userRoleRelationSaveList);
    }

    @Override
    public void deleteByRoleId(String roleId) {
        List<UserRoleRelation> userRoleRelations = getByRoleId(roleId);
        userRoleRelations.forEach(userRoleRelation ->
                checkAdminPermissionRemove(userRoleRelation.getUserId(), userRoleRelation.getRoleId()));
        LogicDeleteManager.execWithoutLogicDelete(() ->
                mapper.deleteByQuery(queryChain().where(UserRoleRelation::getRoleId).eq(roleId)));
    }

    private void checkAdminPermissionRemove(String userId, String roleId) {
        if (StringUtils.equals(roleId, ADMIN.getValue()) && StringUtils.equals(userId, ADMIN.getValue())) {
            throw new CustomException(USER_ROLE_RELATION_REMOVE_ADMIN_USER_PERMISSION);
        }
    }

    @Override
    public List<UserRoleRelation> getByRoleId(String roleId) {
        return queryChain().where(UserRoleRelation::getRoleId).eq(roleId).list();
    }

    @Override
    public List<String> getUserIdByRoleId(String roleId) {
        return queryChain().select(USER_ROLE_RELATION.USER_ID).from(USER_ROLE_RELATION)
                .where(USER_ROLE_RELATION.ROLE_ID.eq(roleId)).listAs(String.class);
    }

    @Override
    public List<UserRoleRelation> getUserIdAndSourceIdByUserIds(List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return List.of();
        }
        return queryChain().where(USER_ROLE_RELATION.USER_ID.in(userIds)).list();
    }

    @Override
    public void batchInsert(List<UserRoleRelation> addRelations) {
        if (CollectionUtils.isEmpty(addRelations)) {
            return;
        }
        mapper.insertBatch(addRelations);
    }

    @Override
    public void checkExist(UserRoleRelation userRoleRelation) {
        val exists = queryChain().where(USER_ROLE_RELATION.USER_ID.eq(userRoleRelation.getUserId())
                .and(USER_ROLE_RELATION.ROLE_ID.eq(userRoleRelation.getRoleId()))).exists();
        if (exists) {
            throw new CustomException(USER_ROLE_RELATION_EXIST);
        }
    }

    @Override
    public UserRole getUserRole(String id) {
        val userRoleRelation = mapper.selectOneById(id);
        return Objects.isNull(userRoleRelation) ? null : QueryChain.of(UserRole.class).where(USER_ROLE.ID.eq(userRoleRelation.getRoleId())).one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        val userRoleRelation = mapper.selectOneById(id);
        checkAdminPermissionRemove(userRoleRelation.getUserId(), userRoleRelation.getRoleId());
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteById(id));
    }

    @Override
    public List<UserExcludeOptionDTO> getExcludeSelectOptionWithLimit(String roleId, String keyword) {
        // 查询所有用户选项
        List<UserExcludeOptionDTO> selectOptions = getExcludeSelectOptionWithLimit(keyword);
        // 查询已经关联的用户ID
        Set<String> excludeUserIds = new HashSet<>(getUserIdByRoleId(roleId));
        // 标记已经关联的用户
        assert selectOptions != null;
        selectOptions.forEach((excludeOption) -> {
            if (excludeUserIds.contains(excludeOption.getId())) {
                excludeOption.setExclude(true);
            }
        });
        return selectOptions;
    }

    @Override
    public Map<Organization, List<Project>> selectOrganizationProjectByUserId(String userId) {
        Map<Organization, List<Project>> returnMap = new LinkedHashMap<>();
        val userRoleRelations = queryChain().where(USER_ROLE_RELATION.USER_ID.eq(userId)).list();
        for (UserRoleRelation userRoleRelation : userRoleRelations) {
            Organization organization = QueryChain.of(Organization.class).where(Organization::getId).eq(userRoleRelation.getOrganizationId()).one();
            if (organization != null) {
                returnMap.computeIfAbsent(organization, k -> new ArrayList<>());
                Project project = QueryChain.of(Project.class).where(Project::getId).eq(userRoleRelation.getSourceId()).one();
                if (project != null && !returnMap.get(organization).contains(project)) {
                    returnMap.get(organization).add(project);
                }
            }
        }
        return returnMap;
    }

    @Override
    public boolean isSuperUser(String userId) {
        return queryChain().where(USER_ROLE_RELATION.USER_ID.eq(userId).and(USER_ROLE_RELATION.ROLE_ID.eq("admin"))).exists();
    }

    private List<UserExcludeOptionDTO> getExcludeSelectOptionWithLimit(String keyword) {
        return QueryChain.of(User.class)
                .select(USER.ID, USER.NAME, USER.EMAIL).from(USER)
                .where(USER.NAME.like(keyword)
                        .or(USER.EMAIL.like(keyword))).limit(1000).listAs(UserExcludeOptionDTO.class);
    }

    private List<LogDTO> getBatchLogs(@Valid @NotEmpty List<String> userRoleId,
                                      @Valid User user,
                                      @Valid @NotEmpty String operationMethod,
                                      @Valid @NotEmpty String operator,
                                      @Valid @NotEmpty String operationType) {
        List<LogDTO> logs = new ArrayList<>();
        val userRoles = QueryChain.of(UserRole.class).where(UserRole::getId).in(userRoleId).list();
        userRoles.forEach(userRole -> {
            LogDTO log = new LogDTO();
            log.setProjectId(OperationLogConstants.SYSTEM);
            log.setOrganizationId(OperationLogConstants.SYSTEM);
            log.setType(operationType);
            log.setCreateUser(operator);
            log.setModule(OperationLogModule.SETTING_SYSTEM_USER_SINGLE);
            log.setMethod(operationMethod);
            log.setSourceId(user.getId());
            log.setContent(user.getName() + StringUtils.SPACE
                    + Translator.get(StringUtils.lowerCase(operationType)) + StringUtils.SPACE
                    + Translator.get("permission.project_group.name") + StringUtils.SPACE
                    + userRole.getName());
            log.setOriginalValue(JsonUtils.toJsonBytes(userRole));
            logs.add(log);
        });
        return logs;
    }
}
