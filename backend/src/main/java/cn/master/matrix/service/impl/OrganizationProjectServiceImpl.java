package cn.master.matrix.service.impl;

import cn.master.matrix.constants.OperationLogModule;
import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.entity.Organization;
import cn.master.matrix.entity.User;
import cn.master.matrix.entity.UserRoleRelation;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.payload.dto.OptionDTO;
import cn.master.matrix.payload.dto.ProjectDTO;
import cn.master.matrix.payload.dto.request.*;
import cn.master.matrix.payload.dto.user.UserExtendDTO;
import cn.master.matrix.service.CommonProjectService;
import cn.master.matrix.service.OrganizationProjectService;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.query.SelectQueryTable;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.master.matrix.entity.table.ProjectTableDef.PROJECT;
import static cn.master.matrix.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static cn.master.matrix.entity.table.UserTableDef.USER;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Service
@RequiredArgsConstructor
public class OrganizationProjectServiceImpl implements OrganizationProjectService {
    private final CommonProjectService commonProjectService;
    private final UserServiceImpl userService;
    private final static String PREFIX = "/organization-project";
    private final static String ADD_PROJECT = PREFIX + "/add";
    private final static String UPDATE_PROJECT = PREFIX + "/update";
    private final static String REMOVE_PROJECT_MEMBER = PREFIX + "/remove-member/";
    private final static String ADD_MEMBER = PREFIX + "/add-member";

    @Override
    public ProjectDTO add(AddProjectRequest request, String userId) {
        return commonProjectService.add(request, userId, ADD_PROJECT, OperationLogModule.SETTING_ORGANIZATION_PROJECT);
    }

    @Override
    public ProjectDTO get(String id) {
        return commonProjectService.get(id);
    }

    @Override
    public Page<ProjectDTO> getProjectList(OrganizationProjectRequest request) {
        ProjectRequest projectRequest = new ProjectRequest();
        BeanUtils.copyProperties(request, projectRequest);
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.where(PROJECT.ORGANIZATION_ID.eq(request.getOrganizationId())
                .and(PROJECT.NAME.like(request.getKeyword())
                        .or(PROJECT.ID.like(request.getKeyword())))
        ).orderBy(PROJECT.CREATE_TIME.desc());
        return commonProjectService.pageAs(Page.of(request.getPageNum(), request.getPageSize()), wrapper, ProjectDTO.class);
    }

    @Override
    public ProjectDTO update(UpdateProjectRequest request, String userId) {
        return commonProjectService.update(request, userId, UPDATE_PROJECT, OperationLogModule.SETTING_ORGANIZATION_PROJECT);
    }

    @Override
    public boolean delete(String id, String deleteUser) {
        return commonProjectService.delete(id, deleteUser);
    }

    @Override
    public boolean revoke(String id, String userId) {
        return commonProjectService.revoke(id, userId);
    }

    @Override
    public boolean enable(String id, String userId) {
        return commonProjectService.enable(id, userId);
    }

    @Override
    public boolean disable(String id, String userId) {
        return commonProjectService.disable(id, userId);
    }

    @Override
    public Page<UserExtendDTO> getProjectMember(ProjectMemberRequest request) {
        QueryWrapper wrapper = new QueryWrapper();
        QueryWrapper subWrapper = new QueryWrapper();
        subWrapper.select(USER.ALL_COLUMNS, USER_ROLE_RELATION.ROLE_ID, USER_ROLE_RELATION.CREATE_TIME.as("memberTime"))
                .from(USER_ROLE_RELATION)
                .leftJoin(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getProjectId())
                        .and(USER.NAME.like(request.getKeyword())
                                .or(USER.EMAIL.like(request.getKeyword()))
                                .or(USER.PHONE.like(request.getKeyword()))));
        wrapper.select("temp.*", "MAX( if (temp.role_id = 'project_admin', true, false)) as adminFlag", "MIN(temp.memberTime) as groupTime")
                .from(new SelectQueryTable(subWrapper).as("temp"))
                .groupBy("temp.id")
                .orderBy("adminFlag", "groupTime");
        return commonProjectService.pageAs(Page.of(request.getPageNum(), request.getPageSize()), wrapper, UserExtendDTO.class);
    }

    @Override
    public void addProjectMember(ProjectAddMemberBatchRequest request, String createUser) {
        commonProjectService.addProjectMember(request, createUser, ADD_MEMBER, OperationLogType.ADD.name(), Translator.get("add"), OperationLogModule.SETTING_ORGANIZATION_PROJECT);
    }

    @Override
    public void removeProjectMember(String projectId, String userId, String createUser) {
        commonProjectService.removeProjectMember(projectId, userId, createUser, OperationLogModule.SETTING_ORGANIZATION_PROJECT, StringUtils.join(REMOVE_PROJECT_MEMBER, projectId, "/", userId));
    }

    @Override
    public List<UserExtendDTO> getUserAdminList(String organizationId, String keyword) {
        checkOrgIsExist(organizationId);
        return QueryChain.of(User.class)
                .select(QueryMethods.distinct(USER.ID, USER.NAME, USER.EMAIL))
                .from(USER).leftJoin(USER_ROLE_RELATION).on(USER.ID.eq(USER_ROLE_RELATION.USER_ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId)
                        .and(USER.NAME.like(keyword).or(USER.EMAIL.like(keyword))))
                .orderBy(USER.CREATE_TIME.desc())
                .limit(1000)
                .listAs(UserExtendDTO.class);
    }

    @Override
    public List<UserExtendDTO> getUserMemberList(String organizationId, String projectId, String keyword) {
        checkOrgIsExist(organizationId);
        commonProjectService.checkProjectNotExist(projectId);
        val userIds = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId)).list()
                .stream().map(UserRoleRelation::getUserId).distinct().toList();
        if (CollectionUtils.isEmpty(userIds)) {
            return List.of();
        }
        QueryWrapper wrapper = new QueryWrapper();
        QueryWrapper subWrapper = new QueryWrapper();
        subWrapper.select(USER_ROLE_RELATION.ALL_COLUMNS).from(USER_ROLE_RELATION).where(USER_ROLE_RELATION.SOURCE_ID.eq(projectId));
        wrapper.select(QueryMethods.distinct(USER.ID), USER.NAME, USER.EMAIL)
                .select("count(temp.id) > 0 as memberFlag")
                .from(USER.as("u")).leftJoin(subWrapper.as("temp")).on("temp.user_id = u.id")
                .where(USER.ID.in(userIds).and(USER.NAME.like(keyword).or(USER.EMAIL.like(keyword))))
                .groupBy(USER.ID)
                .orderBy(USER.CREATE_TIME.desc()).limit(1000);
        return userService.listAs(wrapper, UserExtendDTO.class);
    }

    @Override
    public List<OptionDTO> getTestResourcePoolOptions(ProjectPoolRequest request) {
        return commonProjectService.getTestResourcePoolOptions(request);
    }

    @Override
    public void rename(UpdateProjectNameRequest request, String userId) {
        commonProjectService.rename(request, userId);
    }

    private void checkOrgIsExist(String organizationId) {
        QueryChain.of(Organization.class).where(Organization::getId).eq(organizationId).oneOpt()
                .orElseThrow(() -> new CustomException(Translator.get("organization_not_exists")));
    }
}
