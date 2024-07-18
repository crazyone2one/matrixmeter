package cn.master.matrix.service;

import cn.master.matrix.constants.OperationLogModule;
import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.entity.Project;
import cn.master.matrix.mapper.ProjectMapper;
import cn.master.matrix.mapper.UserRoleRelationMapper;
import cn.master.matrix.payload.dto.OptionDTO;
import cn.master.matrix.payload.dto.ProjectDTO;
import cn.master.matrix.payload.dto.request.*;
import cn.master.matrix.payload.dto.user.UserExtendDTO;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.query.SelectQueryTable;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.master.matrix.entity.table.OrganizationTableDef.ORGANIZATION;
import static cn.master.matrix.entity.table.ProjectTableDef.PROJECT;
import static cn.master.matrix.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static cn.master.matrix.entity.table.UserTableDef.USER;

/**
 * @author Created by 11's papa on 06/28/2024
 **/
@Service
@RequiredArgsConstructor
public class SystemProjectService {
    private final CommonProjectService commonProjectService;
    private final UserRoleRelationMapper userRoleRelationMapper;
    private final ProjectMapper projectMapper;

    private final static String PREFIX = "/system/project";
    private final static String ADD_PROJECT = PREFIX + "/add";
    private final static String UPDATE_PROJECT = PREFIX + "/update";
    private final static String REMOVE_PROJECT_MEMBER = PREFIX + "/remove-member/";
    private final static String ADD_MEMBER = PREFIX + "/add-member";

    public void addProjectMember(ProjectAddMemberBatchRequest request, String createUser) {
        commonProjectService.addProjectMember(request, createUser, ADD_MEMBER,
                OperationLogType.ADD.name(), Translator.get("add"), OperationLogModule.SETTING_SYSTEM_ORGANIZATION);
    }

    public Page<ProjectDTO> getProjectPage(ProjectRequest projectRequest) {
        val queryChain = QueryChain.of(Project.class).select(PROJECT.ALL_COLUMNS)
                .select(ORGANIZATION.NAME.as("organizationName"))
                .from(PROJECT)
                .innerJoin(ORGANIZATION).on(PROJECT.ORGANIZATION_ID.eq(ORGANIZATION.ID))
                .where(PROJECT.ORGANIZATION_ID.eq(projectRequest.getOrganizationId())
                        .and(PROJECT.NAME.like(projectRequest.getKeyword())
                                .or(PROJECT.NUM.like(projectRequest.getKeyword())))
                );
        val page = commonProjectService.pageAs(Page.of(projectRequest.getPageNum(), projectRequest.getPageSize()), queryChain, ProjectDTO.class);
        //val page = projectMapper.paginateWithRelationsAs(Page.of(projectRequest.getPageNum(), projectRequest.getPageSize()), queryChain, ProjectDTO.class);
        commonProjectService.buildUserInfo(page.getRecords());
        return page;
    }

    public ProjectDTO add(AddProjectRequest request, String createUser) {
        return commonProjectService.add(request, createUser, ADD_PROJECT, OperationLogModule.SETTING_SYSTEM_ORGANIZATION);
    }

    public ProjectDTO get(String id) {
        return commonProjectService.get(id);
    }

    public ProjectDTO update(UpdateProjectRequest request, String userId) {
        return commonProjectService.update(request, userId, UPDATE_PROJECT, OperationLogModule.SETTING_SYSTEM_ORGANIZATION);
    }

    public boolean delete(String id, String userId) {
        return commonProjectService.delete(id, userId);
    }

    public void enable(String id, String userId) {
        commonProjectService.enable(id, userId);
    }

    public void disable(String id, String userId) {
        commonProjectService.disable(id, userId);
    }

    public Page<UserExtendDTO> getProjectMember(ProjectMemberRequest request) {
        QueryWrapper wrapper = new QueryWrapper();
        QueryWrapper subWrapper = new QueryWrapper();
        subWrapper.select(USER.ALL_COLUMNS, USER_ROLE_RELATION.ROLE_ID, USER_ROLE_RELATION.CREATE_TIME.as("memberTime"))
                .from(USER_ROLE_RELATION).join(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getProjectId()))
                .and(USER.NAME.like(request.getKeyword())
                        .or(USER.EMAIL.like(request.getKeyword()))
                        .or(USER.PHONE.like(request.getKeyword())))
                .orderBy(USER_ROLE_RELATION.CREATE_TIME.desc());
        wrapper.select("temp.*")
                .select("max(if(temp.role_id = 'org_admin', true, false)) as adminFlag")
                .select("min(temp.memberTime) as groupTime")
                .from(new SelectQueryTable(subWrapper).as("temp"))
                .groupBy("temp.id")
                .orderBy("adminFlag", "groupTime");
        return userRoleRelationMapper.paginateAs(Page.of(request.getPageNum(), request.getPageSize()), wrapper, UserExtendDTO.class);
    }

    public void removeProjectMember(String projectId, String userId, String userId1) {
        commonProjectService.removeProjectMember(projectId, userId, userId1, OperationLogModule.SETTING_SYSTEM_ORGANIZATION, StringUtils.join(REMOVE_PROJECT_MEMBER, projectId, "/", userId));
    }

    public List<OptionDTO> getTestResourcePoolOptions(ProjectPoolRequest request) {
        return commonProjectService.getTestResourcePoolOptions(request);
    }

    public void rename(UpdateProjectNameRequest request, String userId) {
        commonProjectService.rename(request, userId);
    }

    public List<OptionDTO> list(String keyword) {
        val queryChain = QueryChain.of(Project.class)
                .select(PROJECT.ID, PROJECT.NAME)
                .from(PROJECT).where(PROJECT.NAME.like(keyword))
                .orderBy(PROJECT.UPDATE_TIME.desc())
                .limit(1000);
        return commonProjectService.listAs(queryChain, OptionDTO.class);
    }
}
