package cn.master.matrix.service.impl;

import cn.master.matrix.constants.UserRoleScope;
import cn.master.matrix.entity.User;
import cn.master.matrix.entity.UserRole;
import cn.master.matrix.entity.UserRoleRelation;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.payload.dto.request.GlobalUserRoleRelationQueryRequest;
import cn.master.matrix.payload.dto.request.GlobalUserRoleRelationUpdateRequest;
import cn.master.matrix.payload.dto.request.user.UserExcludeOptionDTO;
import cn.master.matrix.payload.dto.request.user.UserRoleBatchRelationRequest;
import cn.master.matrix.payload.dto.user.UserRoleRelationUserDTO;
import cn.master.matrix.payload.response.TableBatchProcessResponse;
import cn.master.matrix.service.*;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.master.matrix.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static cn.master.matrix.entity.table.UserTableDef.USER;
import static cn.master.matrix.exception.SystemResultCode.GLOBAL_USER_ROLE_LIMIT;

/**
 * @author Created by 11's papa on 06/28/2024
 **/
@Service
public class GlobalUserRoleRelationServiceImpl extends BaseUserRoleRelationServiceImpl implements GlobalUserRoleRelationService {
    private final UserToolService userToolService;
    private final GlobalUserRoleService globalUserRoleService;
    private final BaseUserRoleService baseUserRoleService;

    public GlobalUserRoleRelationServiceImpl(OperationLogService operationLogService,
                                             UserToolService userToolService,
                                             GlobalUserRoleService globalUserRoleService,
                                             BaseUserRoleService baseUserRoleService) {
        super(operationLogService);
        this.userToolService = userToolService;
        this.globalUserRoleService = globalUserRoleService;
        this.baseUserRoleService = baseUserRoleService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TableBatchProcessResponse batchAdd(UserRoleBatchRelationRequest request, String operator) {
        checkGlobalSystemUserRoleLegality(request.getRoleIds());
        request.setSelectIds(userToolService.getBatchUserIds(request));
        checkUserLegality(request.getSelectIds());
        List<UserRoleRelation> savedUserRoleRelation = this.selectByUserIdAndRuleId(request.getSelectIds(), request.getRoleIds());
        Map<String, List<String>> userRoleIdMap = savedUserRoleRelation.stream()
                .collect(Collectors.groupingBy(UserRoleRelation::getUserId, Collectors.mapping(UserRoleRelation::getRoleId, Collectors.toList())));
        List<UserRoleRelation> saveList = new ArrayList<>();
        for (String userId : request.getSelectIds()) {
            for (String roleId : request.getRoleIds()) {
                if (userRoleIdMap.containsKey(userId) && userRoleIdMap.get(userId).contains(roleId)) {
                    continue;
                }
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setUserId(userId);
                userRoleRelation.setRoleId(roleId);
                userRoleRelation.setCreateUser(operator);
                userRoleRelation.setSourceId(UserRoleScope.SYSTEM);
                userRoleRelation.setOrganizationId(UserRoleScope.SYSTEM);
                saveList.add(userRoleRelation);
            }
        }
        if (CollectionUtils.isNotEmpty(saveList)) {
            mapper.insertBatch(saveList);
        }
        TableBatchProcessResponse response = new TableBatchProcessResponse();
        response.setTotalCount(request.getSelectIds().size());
        response.setSuccessCount(saveList.size());
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(GlobalUserRoleRelationUpdateRequest request) {
        checkGlobalSystemUserRoleLegality(Collections.singletonList(request.getRoleId()));
        checkUserLegality(request.getUserIds());
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        request.getUserIds().forEach(userId -> {
            UserRoleRelation userRoleRelation = new UserRoleRelation();
            BeanUtils.copyProperties(request, userRoleRelation);
            userRoleRelation.setUserId(userId);
            userRoleRelation.setSourceId(UserRoleScope.SYSTEM);
            checkExist(userRoleRelation);
            userRoleRelation.setOrganizationId(UserRoleScope.SYSTEM);
            userRoleRelations.add(userRoleRelation);
        });
        mapper.insertBatch(userRoleRelations);
    }

    @Override
    public Page<UserRoleRelationUserDTO> page(GlobalUserRoleRelationQueryRequest request) {
        val page = queryChain().select(USER_ROLE_RELATION.ID, USER.ID.as("userId"), USER.NAME, USER.EMAIL, USER.PHONE)
                .from(USER_ROLE_RELATION).innerJoin(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID)
                        .and(USER_ROLE_RELATION.ROLE_ID.eq(request.getRoleId())))
                .and(USER.NAME.like(request.getKeyword())
                        .or(USER.EMAIL.like(request.getKeyword()))
                        .or(USER.PHONE.like(request.getKeyword())))
                .orderBy(USER_ROLE_RELATION.CREATE_TIME.desc())
                .pageAs(Page.of(request.getPageNum(), request.getPageSize()), UserRoleRelationUserDTO.class);
        UserRole userRole = globalUserRoleService.getById(request.getRoleId());
        globalUserRoleService.checkSystemUserGroup(userRole);
        globalUserRoleService.checkGlobalUserRole(userRole);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        UserRole userRole = getUserRole(id);
        baseUserRoleService.checkResourceExist(userRole);
        UserRoleRelation userRoleRelation = mapper.selectOneById(id);
        globalUserRoleService.checkSystemUserGroup(userRole);
        globalUserRoleService.checkGlobalUserRole(userRole);
        super.delete(id);
        val exists = queryChain().where(USER_ROLE_RELATION.USER_ID.eq(userRoleRelation.getUserId())
                .and(USER_ROLE_RELATION.SOURCE_ID.eq(UserRoleScope.SYSTEM))).exists();
        if (!exists) {
            throw new CustomException(GLOBAL_USER_ROLE_LIMIT);
        }
    }

    @Override
    public List<UserExcludeOptionDTO> getExcludeSelectOption(String roleId, String keyword) {
        baseUserRoleService.getWithCheck(roleId);
        return super.getExcludeSelectOptionWithLimit(roleId, keyword);
    }

    private List<UserRoleRelation> selectByUserIdAndRuleId(List<String> userIds, List<String> roleIds) {
        return queryChain().where(UserRoleRelation::getUserId).in(userIds).and(UserRoleRelation::getRoleId).in(roleIds).list();
    }

    private void checkUserLegality(List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            throw new CustomException(Translator.get("user.not.exist"));
        }
        val count = QueryChain.of(User.class).where(User::getId).in(userIds).count();
        if (count != userIds.size()) {
            throw new CustomException(Translator.get("user.id.not.exist"));
        }
    }

    private void checkGlobalSystemUserRoleLegality(List<String> roleIds) {
        val userRoles = QueryChain.of(UserRole.class).where(UserRole::getId).in(roleIds).list();
        if (userRoles.size() != roleIds.size()) {
            throw new CustomException(Translator.get("user_role_not_exist"));
        }
        userRoles.forEach(userRole -> {
            globalUserRoleService.checkSystemUserGroup(userRole);
            globalUserRoleService.checkGlobalUserRole(userRole);
        });
    }
}
