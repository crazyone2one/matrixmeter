package cn.master.matrix.service.impl;

import cn.master.matrix.constants.InternalUserRole;
import cn.master.matrix.constants.ProjectMenuConstants;
import cn.master.matrix.entity.Organization;
import cn.master.matrix.entity.Project;
import cn.master.matrix.entity.User;
import cn.master.matrix.entity.UserRoleRelation;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.payload.dto.ProjectDTO;
import cn.master.matrix.payload.dto.request.project.ProjectSwitchRequest;
import cn.master.matrix.payload.dto.user.UserDTO;
import cn.master.matrix.service.CommonProjectService;
import cn.master.matrix.service.ProjectService;
import cn.master.matrix.service.UserService;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.master.matrix.entity.table.ProjectTableDef.PROJECT;
import static cn.master.matrix.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static cn.master.matrix.entity.table.UserRoleTableDef.USER_ROLE;
import static cn.master.matrix.entity.table.UserTableDef.USER;

/**
 * @author Created by 11's papa on 07/18/2024
 **/
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final CommonProjectService commonProjectService;
    private final UserService userService;

    @Override
    public ProjectDTO getProjectById(String id) {
        return commonProjectService.get(id);
    }

    @Override
    public List<Project> getUserProject(String organizationId, String userId) {
        checkOrg(organizationId);
        val user = userService.getById(userId);
        String projectId;
        if (user != null && StringUtils.isNotBlank(user.getLastProjectId())) {
            projectId = user.getLastProjectId();
        } else {
            projectId = null;
        }
        List<Project> allProject;
        val exists = QueryChain.of(UserRoleRelation.class).where(UserRoleRelation::getUserId).eq(userId)
                .and(UserRoleRelation::getRoleId).eq(InternalUserRole.ADMIN.name()).exists();
        if (exists) {
            allProject = QueryChain.of(Project.class).where(Project::getOrganizationId).eq(organizationId)
                    .and(Project::getEnable).eq(true)
                    .orderBy("name", true)
                    .list();
        } else {
            allProject = QueryChain.of(Project.class)
                    .select(PROJECT.ALL_COLUMNS).from(USER_ROLE)
                    .join(USER_ROLE_RELATION).on(USER_ROLE.ID.eq(USER_ROLE_RELATION.ROLE_ID))
                    .join(PROJECT).on(USER_ROLE_RELATION.SOURCE_ID.eq(PROJECT.ID))
                    .join(USER).on(USER.ID.eq(USER_ROLE_RELATION.USER_ID))
                    .where(USER_ROLE_RELATION.USER_ID.eq(userId)
                            .and(USER_ROLE.TYPE.eq("PROJECT"))
                            .and(PROJECT.ORGANIZATION_ID.eq(organizationId))
                            .and(PROJECT.ENABLE.eq(true)))
                    .orderBy(PROJECT.NAME, true)
                    .list();
        }
        List<Project> temp = allProject;
        return allProject.stream()
                .filter(project -> StringUtils.equals(project.getId(), projectId))
                .findFirst()
                .map(project -> {
                    temp.remove(project);
                    temp.add(0, project);
                    return temp;
                })
                .orElse(allProject);
    }

    @Override
    public List<Project> getUserProjectWidthModule(String organizationId, String module, String userId) {
        if (StringUtils.isBlank(module)) {
            throw new CustomException(Translator.get("module.name.is.empty"));
        }
        String moduleName = null;
        if (StringUtils.equalsIgnoreCase(module, "API") || StringUtils.equalsIgnoreCase(module, "SCENARIO")) {
            moduleName = ProjectMenuConstants.MODULE_MENU_API_TEST;
        }
        if (StringUtils.equalsIgnoreCase(module, "FUNCTIONAL")) {
            moduleName = ProjectMenuConstants.MODULE_MENU_FUNCTIONAL_CASE;
        }
        if (StringUtils.equalsIgnoreCase(module, "BUG")) {
            moduleName = ProjectMenuConstants.MODULE_MENU_BUG;
        }
        if (StringUtils.equalsIgnoreCase(module, "PERFORMANCE")) {
            moduleName = ProjectMenuConstants.MODULE_MENU_LOAD_TEST;
        }
        if (StringUtils.equalsIgnoreCase(module, "UI")) {
            moduleName = ProjectMenuConstants.MODULE_MENU_UI;
        }
        if (StringUtils.equalsIgnoreCase(module, "TEST_PLAN")) {
            moduleName = ProjectMenuConstants.MODULE_MENU_TEST_PLAN;
        }
        if (StringUtils.isBlank(moduleName)) {
            throw new CustomException(Translator.get("module.name.is.error"));
        }
        checkOrg(organizationId);
        val user = userService.getById(userId);
        String projectId;
        if (user != null && StringUtils.isNotBlank(user.getLastProjectId())) {
            projectId = user.getLastProjectId();
        } else {
            projectId = null;
        }
        List<Project> allProject;
        val exists = QueryChain.of(UserRoleRelation.class).where(UserRoleRelation::getUserId).eq(userId)
                .and(UserRoleRelation::getRoleId).eq(InternalUserRole.ADMIN.name()).exists();
        if (exists) {
            allProject = QueryChain.of(Project.class).where(Project::getOrganizationId).eq(organizationId)
                    .and(Project::getEnable).eq(true)
                    .and(PROJECT.MODULE_SETTING.like(moduleName))
                    .orderBy("name", true)
                    .list();
        } else {
            allProject = QueryChain.of(Project.class)
                    .select(PROJECT.ALL_COLUMNS).from(USER_ROLE)
                    .join(USER_ROLE_RELATION).on(USER_ROLE.ID.eq(USER_ROLE_RELATION.ROLE_ID))
                    .join(PROJECT).on(USER_ROLE_RELATION.SOURCE_ID.eq(PROJECT.ID))
                    .join(USER).on(USER.ID.eq(USER_ROLE_RELATION.USER_ID))
                    .where(USER_ROLE_RELATION.USER_ID.eq(userId)
                            .and(USER_ROLE.TYPE.eq("PROJECT"))
                            .and(PROJECT.ORGANIZATION_ID.eq(organizationId))
                            .and(PROJECT.ENABLE.eq(true))
                            .and(PROJECT.MODULE_SETTING.like(moduleName)))
                    .orderBy(PROJECT.NAME, true)
                    .list();
        }
        List<Project> temp = allProject;
        return allProject.stream()
                .filter(project -> StringUtils.equals(project.getId(), projectId))
                .findFirst()
                .map(project -> {
                    temp.remove(project);
                    temp.add(0, project);
                    return temp;
                })
                .orElse(allProject);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO switchProject(ProjectSwitchRequest request, String currentUserId) {
        if (!StringUtils.equals(currentUserId, request.getUserId())) {
            throw new CustomException(Translator.get("not_authorized"));
        }
        QueryChain.of(Project.class).where(PROJECT.ID.eq(request.getProjectId())).oneOpt()
                .orElseThrow(() -> new CustomException(Translator.get("project_not_exist")));

        UpdateChain.of(User.class)
                .set(USER.LAST_PROJECT_ID, request.getProjectId())
                .where(USER.ID.eq(request.getUserId())).update();
        return userService.getUserDTO(request.getUserId());
    }

    private void checkOrg(String organizationId) {
        QueryChain.of(Organization.class).where(Organization::getId).eq(organizationId).oneOpt()
                .orElseThrow(() -> new CustomException(Translator.get("organization_not_exist")));
    }
}
