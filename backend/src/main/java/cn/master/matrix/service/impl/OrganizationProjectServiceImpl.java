package cn.master.matrix.service.impl;

import cn.master.matrix.constants.OperationLogModule;
import cn.master.matrix.payload.dto.ProjectDTO;
import cn.master.matrix.payload.dto.request.*;
import cn.master.matrix.payload.dto.user.UserExtendDTO;
import cn.master.matrix.service.CommonProjectService;
import cn.master.matrix.service.OrganizationProjectService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import static cn.master.matrix.entity.table.ProjectTableDef.PROJECT;
import static cn.master.matrix.entity.table.UserTableDef.USER;
import static cn.master.matrix.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Service
@RequiredArgsConstructor
public class OrganizationProjectServiceImpl implements OrganizationProjectService {
    private final CommonProjectService commonProjectService;
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
                .from(subWrapper).as("temp")
                .groupBy("temp.id")
                .orderBy("adminFlag", "groupTime");
        return commonProjectService.pageAs(Page.of(request.getPageNum(), request.getPageSize()), wrapper, UserExtendDTO.class);
    }
}
