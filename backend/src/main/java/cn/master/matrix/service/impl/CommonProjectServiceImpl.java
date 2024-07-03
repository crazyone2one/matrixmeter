package cn.master.matrix.service.impl;

import cn.master.matrix.constants.*;
import cn.master.matrix.entity.*;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.handler.invoker.ProjectServiceInvoker;
import cn.master.matrix.handler.validation.Created;
import cn.master.matrix.handler.validation.Updated;
import cn.master.matrix.mapper.ProjectMapper;
import cn.master.matrix.mapper.ProjectTestResourcePoolMapper;
import cn.master.matrix.mapper.UserMapper;
import cn.master.matrix.mapper.UserRoleRelationMapper;
import cn.master.matrix.payload.dto.LogDTO;
import cn.master.matrix.payload.dto.OptionDTO;
import cn.master.matrix.payload.dto.ProjectDTO;
import cn.master.matrix.payload.dto.request.*;
import cn.master.matrix.service.CommonProjectService;
import cn.master.matrix.service.OperationLogService;
import cn.master.matrix.util.JsonUtils;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.master.matrix.entity.table.TestResourcePoolTableDef.TEST_RESOURCE_POOL;
import static cn.master.matrix.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;

/**
 * 项目 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-28T16:11:33.913527
 */
@Service
@RequiredArgsConstructor
public class CommonProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements CommonProjectService {
    private final OperationLogService operationLogService;
    private final UserRoleRelationMapper userRoleRelationMapper;
    private final ProjectServiceInvoker serviceInvoker;
    private final ProjectTestResourcePoolMapper projectTestResourcePoolMapper;
    private final UserMapper userMapper;

    public static final Integer DEFAULT_REMAIN_DAY_COUNT = 30;
    public static final String API_TEST = "apiTest";
    public static final String TEST_PLAN = "testPlan";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProjectMember(ProjectAddMemberBatchRequest request, String createUser, String path, String type, String content, String module) {
        List<LogDTO> logDTOList = new ArrayList<>();
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        request.getProjectIds().forEach(projectId -> {
            val project = mapper.selectOneById(projectId);
            Map<String, String> userMap = addUserPre(request, createUser, path, module, projectId, project);
            request.getUserIds().forEach(userId -> {
                val exists = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(userId)
                        .and(USER_ROLE_RELATION.SOURCE_ID.eq(projectId))
                        .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.PROJECT_ADMIN.getValue()))).exists();
                addProjectRelation(createUser, path, type, content, module, logDTOList, userRoleRelations, projectId, project, userMap, userId, exists);
            });
        });
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            userRoleRelationMapper.insertBatch(userRoleRelations);
        }
        operationLogService.batchAdd(logDTOList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectDTO add(AddProjectRequest request, String userId, String path, String module) {
        Project project = new Project();
        BeanUtils.copyProperties(request, project);
        project.setCreateUser(userId);
        project.setModuleSetting(request.getModuleIds());

        if (CollectionUtils.isNotEmpty(request.getResourcePoolIds())) {
            checkResourcePoolExist(request.getResourcePoolIds());
            List<ProjectTestResourcePool> projectTestResourcePools = new ArrayList<>();
            val queryChain = QueryChain.of(projectTestResourcePoolMapper).where(ProjectTestResourcePool::getProjectId).eq(project.getId());
            LogicDeleteManager.execWithoutLogicDelete(() -> projectTestResourcePoolMapper.deleteByQuery(queryChain));
            request.getResourcePoolIds().forEach(resourcePoolId -> {
                ProjectTestResourcePool projectTestResourcePool = new ProjectTestResourcePool();
                projectTestResourcePool.setProjectId(project.getId());
                projectTestResourcePool.setTestResourcePoolId(resourcePoolId);
                projectTestResourcePools.add(projectTestResourcePool);
            });
            projectTestResourcePoolMapper.insertBatch(projectTestResourcePools);
        }

        mapper.insert(project);
        serviceInvoker.invokeCreateServices(project.getId());

        ProjectDTO projectDTO = new ProjectDTO();
        BeanUtils.copyProperties(project, projectDTO);

        ProjectAddMemberBatchRequest memberRequest = new ProjectAddMemberBatchRequest();
        memberRequest.setProjectIds(List.of(project.getId()));
        memberRequest.setUserIds(request.getUserIds());
        addProjectAdmin(memberRequest, userId, path, OperationLogType.ADD.name(), Translator.get("add"), module);
        return projectDTO;
    }

    @Override
    public ProjectDTO get(String id) {
        val project = queryChain().where(Project::getId).eq(id).and(Project::getEnable).eq(true).one();
        ProjectDTO projectDTO = new ProjectDTO();
        if (Objects.nonNull(project)) {
            BeanUtils.copyProperties(project, projectDTO);
            buildUserInfo(projectDTO);
        }
        return projectDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectDTO update(UpdateProjectRequest request, String userId, String path, String module) {
        Project project = new Project();
        BeanUtils.copyProperties(request, project);
        project.setUpdateUser(userId);
        checkProjectExistByName(project);
        checkProjectNotExist(project.getId());

        if (CollectionUtils.isNotEmpty(request.getResourcePoolIds())) {
            checkResourcePoolExist(request.getResourcePoolIds());
            List<ProjectTestResourcePool> projectTestResourcePools = new ArrayList<>();
            val queryChain = QueryChain.of(projectTestResourcePoolMapper).where(ProjectTestResourcePool::getProjectId).eq(project.getId());
            LogicDeleteManager.execWithoutLogicDelete(() -> projectTestResourcePoolMapper.deleteByQuery(queryChain));
            request.getResourcePoolIds().forEach(resourcePoolId -> {
                ProjectTestResourcePool projectTestResourcePool = new ProjectTestResourcePool();
                projectTestResourcePool.setProjectId(project.getId());
                projectTestResourcePool.setTestResourcePoolId(resourcePoolId);
                projectTestResourcePools.add(projectTestResourcePool);
            });
            projectTestResourcePoolMapper.insertBatch(projectTestResourcePools);
        } else {
            val queryChain = QueryChain.of(projectTestResourcePoolMapper).where(ProjectTestResourcePool::getProjectId).eq(project.getId());
            LogicDeleteManager.execWithoutLogicDelete(() -> projectTestResourcePoolMapper.deleteByQuery(queryChain));
        }
        val orgUserIds = QueryChain.of(userRoleRelationMapper).where(USER_ROLE_RELATION.SOURCE_ID.eq(project.getId())
                        .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.PROJECT_ADMIN.getValue()))).list()
                .stream().map(UserRoleRelation::getUserId).toList();
        List<LogDTO> logDTOList = new ArrayList<>();
        List<String> deleteIds = orgUserIds.stream()
                .filter(item -> !request.getUserIds().contains(item))
                .toList();
        List<String> insertIds = request.getUserIds().stream()
                .filter(item -> !orgUserIds.contains(item))
                .toList();
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            val queryChain = QueryChain.of(userRoleRelationMapper).where(USER_ROLE_RELATION.SOURCE_ID.eq(project.getId())
                    .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.PROJECT_ADMIN.getValue()))
                    .and(USER_ROLE_RELATION.USER_ID.in(deleteIds)));
            queryChain.list().forEach(userRoleRelation -> {
                User user = userMapper.selectOneById(userRoleRelation.getUserId());
                String logProjectId = OperationLogConstants.SYSTEM;
                if (StringUtils.equals(module, OperationLogModule.SETTING_ORGANIZATION_PROJECT)) {
                    logProjectId = OperationLogConstants.ORGANIZATION;
                }
                LogDTO logDTO = new LogDTO(logProjectId, project.getOrganizationId(), userRoleRelation.getId(), userId, OperationLogType.DELETE.name(), module, Translator.get("delete") + Translator.get("project_admin") + ": " + user.getName());
                setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
            });
            LogicDeleteManager.execWithoutLogicDelete(() -> userRoleRelationMapper.deleteByQuery(queryChain));
        }
        if (CollectionUtils.isNotEmpty(insertIds)) {
            ProjectAddMemberBatchRequest memberRequest = new ProjectAddMemberBatchRequest();
            memberRequest.setProjectIds(List.of(project.getId()));
            memberRequest.setUserIds(insertIds);
            this.addProjectAdmin(memberRequest, userId, path, OperationLogType.ADD.name(),
                    Translator.get("add"), module);
        }
        if (CollectionUtils.isNotEmpty(logDTOList)) {
            operationLogService.batchAdd(logDTOList);
        }
        if (CollectionUtils.isNotEmpty(request.getModuleIds())) {
            project.setModuleSetting(request.getModuleIds());
        } else {
            project.setModuleSetting(null);
        }
        mapper.update(project);
        ProjectDTO projectDTO = new ProjectDTO();
        BeanUtils.copyProperties(project, projectDTO);
        return projectDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String id, String deleteUser) {
        checkProjectNotExist(id);
        return updateChain().set(Project::getDeleteUser, deleteUser)
                .set(Project::getDeleted, true)
                .set(Project::getDeleteTime, LocalDateTime.now())
                .where(Project::getId).eq(id)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revoke(String id, String userId) {
        checkProjectNotExist(id);
        return updateChain().set(Project::getUpdateUser, userId)
                .set(Project::getDeleted, false)
                .where(Project::getId).eq(id)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enable(String id, String userId) {
        checkProjectNotExist(id);
        return updateChain().set(Project::getUpdateUser, userId)
                .set(Project::getEnable, true)
                .where(Project::getId).eq(id)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disable(String id, String userId) {
        checkProjectNotExist(id);
        return updateChain().set(Project::getUpdateUser, userId)
                .set(Project::getEnable, false)
                .where(Project::getId).eq(id)
                .update();
    }

    @Override
    public void removeProjectMember(String projectId, String userId, String createUser, String module, String path) {
        checkProjectNotExist(projectId);
        val user = QueryChain.of(userMapper).where(User::getId).eq(userId).oneOpt()
                .orElseThrow(() -> new CustomException(Translator.get("user_not_exist")));
        //判断用户是不是最后一个管理员  如果是  就报错
        val exists = QueryChain.of(userRoleRelationMapper).where(USER_ROLE_RELATION.USER_ID.ne(userId)
                .and(USER_ROLE_RELATION.SOURCE_ID.eq(projectId))
                .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.PROJECT_ADMIN.getValue()))).exists();
        if (!exists) {
            throw new CustomException(Translator.get("keep_at_least_one_administrator"));
        }
        if (StringUtils.equals(projectId, user.getLastProjectId())) {
            user.setLastProjectId(StringUtils.EMPTY);
            userMapper.update(user);
        }
        List<LogDTO> logDTOList = new ArrayList<>();
        val queryChain = QueryChain.of(userRoleRelationMapper).where(USER_ROLE_RELATION.USER_ID.eq(userId)
                .and(USER_ROLE_RELATION.SOURCE_ID.eq(projectId)));
        queryChain.list().forEach(userRoleRelation -> {
            String logProjectId = OperationLogConstants.SYSTEM;
            if (StringUtils.equals(module, OperationLogModule.SETTING_ORGANIZATION_PROJECT)) {
                logProjectId = OperationLogConstants.ORGANIZATION;
            }
            LogDTO logDTO = new LogDTO(logProjectId, OperationLogConstants.SYSTEM, userRoleRelation.getId(), createUser, OperationLogType.DELETE.name(), module, Translator.get("delete") + Translator.get("project_member") + ": " + user.getName());
            setLog(logDTO, path, HttpMethodConstants.GET.name(), logDTOList);
        });
        operationLogService.batchAdd(logDTOList);
        LogicDeleteManager.execWithoutLogicDelete(() -> userRoleRelationMapper.deleteByQuery(queryChain));
    }

    private void checkProjectExistByName(Project project) {
        val exists = queryChain().where(Project::getName).eq(project.getName())
                .and(Project::getOrganizationId).eq(project.getOrganizationId())
                .and(Project::getId).ne(project.getId())
                .exists();
        if (exists) {
            throw new CustomException(Translator.get("project_name_already_exists"));
        }
    }

    private void buildUserInfo(ProjectDTO projectDTO) {
        // todo
    }

    private void addProjectAdmin(ProjectAddMemberBatchRequest request, String createUser, String path, String type, String content, String module) {
        List<LogDTO> logDTOList = new ArrayList<>();
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        request.getProjectIds().forEach(projectId -> {
            Project project = mapper.selectOneById(projectId);
            Map<String, String> nameMap = addUserPre(request, createUser, path, module, projectId, project);
            request.getUserIds().forEach(userId -> {
                val exists = QueryChain.of(userRoleRelationMapper).where(USER_ROLE_RELATION.SOURCE_ID.eq(projectId)
                        .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.PROJECT_ADMIN.getValue()))).exists();
                addProjectRelation(createUser, path, type, content, module, logDTOList, userRoleRelations, projectId, project, nameMap, userId, exists);
            });
        });
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            userRoleRelationMapper.insertBatch(userRoleRelations);
        }
        operationLogService.batchAdd(logDTOList);
    }

    private void addProjectRelation(String createUser, String path, String type, String content, String module, List<LogDTO> logDTOList, List<UserRoleRelation> userRoleRelations, @NotBlank(message = "{project.id.not_blank}", groups = {Created.class, Updated.class}) String projectId, Project project, Map<String, String> nameMap, @NotBlank(message = "{user_role_relation.user_id.not_blank}", groups = {Created.class, Updated.class}) String userId, boolean exists) {
        if (!exists) {
            UserRoleRelation adminRole = new UserRoleRelation();
            adminRole.setUserId(userId);
            adminRole.setRoleId(InternalUserRole.PROJECT_ADMIN.getValue());
            adminRole.setSourceId(projectId);
            adminRole.setCreateUser(createUser);
            adminRole.setOrganizationId(project.getOrganizationId());
            userRoleRelations.add(adminRole);
            String logProjectId = OperationLogConstants.SYSTEM;
            if (StringUtils.equals(module, OperationLogModule.SETTING_ORGANIZATION_PROJECT)) {
                logProjectId = OperationLogConstants.ORGANIZATION;
            }
            LogDTO logDTO = new LogDTO(logProjectId, project.getOrganizationId(), adminRole.getId(), createUser, type, module, content + Translator.get("project_admin") + ": " + nameMap.get(userId));
            setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
        }
    }

    private void checkResourcePoolExist(List<String> resourcePoolIds) {
        val list = QueryChain.of(TestResourcePool.class).where(TestResourcePool::getId).in(resourcePoolIds)
                .and(TestResourcePool::getEnable).eq(true)
                .list();
        if (resourcePoolIds.size() != list.size()) {
            throw new CustomException(Translator.get("resource_pool_not_exist"));
        }
    }

    private Map<String, String> addUserPre(ProjectAddMemberBatchRequest request, String createUser, String path, String module, String projectId, Project project) {
        checkProjectNotExist(projectId);
        val users = QueryChain.of(User.class).where(User::getId).in(request.getUserIds()).list();
        if (request.getUserIds().size() != users.size()) {
            throw new CustomException(Translator.get("user_not_exist"));
        }
        Map<String, String> userMap = users.stream().collect(Collectors.toMap(User::getId, User::getName));
        checkOrgRoleExit(request.getUserIds(), project.getOrganizationId(), createUser, userMap, path, module);
        return userMap;
    }

    private void checkOrgRoleExit(List<String> userIds, String orgId, String createUser, Map<String, String> nameMap, String path, String module) {
        List<LogDTO> logDTOList = new ArrayList<>();
        val userRoleRelations = QueryChain.of(UserRoleRelation.class).where(UserRoleRelation::getSourceId).eq(orgId)
                .and(UserRoleRelation::getUserId).in(userIds).list();
        List<String> orgUserIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).toList();
        if (CollectionUtils.isNotEmpty(userIds)) {
            List<UserRoleRelation> userRoleRelation = new ArrayList<>();
            userIds.forEach(id -> {
                if (!orgUserIds.contains(id)) {
                    UserRoleRelation memberRole = new UserRoleRelation();
                    memberRole.setUserId(id);
                    memberRole.setRoleId(InternalUserRole.ORG_MEMBER.getValue());
                    memberRole.setSourceId(orgId);
                    memberRole.setCreateUser(createUser);
                    memberRole.setOrganizationId(orgId);
                    userRoleRelation.add(memberRole);
                    LogDTO logDTO = new LogDTO(orgId, orgId, memberRole.getId(), createUser, OperationLogType.ADD.name(), module, Translator.get("add") + Translator.get("organization_member") + ": " + nameMap.get(id));
                    setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
                }
            });
            if (CollectionUtils.isNotEmpty(userRoleRelation)) {
                userRoleRelationMapper.insertBatch(userRoleRelation);
            }
        }
        operationLogService.batchAdd(logDTOList);
    }

    @Override
    public void checkProjectNotExist(String projectId) {
        queryChain().where(Project::getId).eq(projectId).oneOpt()
                .orElseThrow(() -> new CustomException(Translator.get("project_is_not_exist")));
    }

    @Override
    public List<OptionDTO> getTestResourcePoolOptions(ProjectPoolRequest request) {
        List<OptionDTO> optionDTOS = new ArrayList<>();
        //获取制定组织的资源池  和全部组织的资源池
        List<TestResourcePool> testResourcePools = new ArrayList<>();
        if (StringUtils.isNotEmpty(request.getOrganizationId())) {
            val poolOrganizationList = QueryChain.of(TestResourcePoolOrganization.class)
                    .where(TestResourcePoolOrganization::getOrgId).eq(request.getOrganizationId())
                    .list();
            if (CollectionUtils.isNotEmpty(poolOrganizationList)) {
                List<String> poolIds = poolOrganizationList.stream().map(TestResourcePoolOrganization::getTestResourcePoolId).toList();
                testResourcePools.addAll(QueryChain.of(TestResourcePool.class).where(TestResourcePool::getId).in(poolIds).and(TestResourcePool::getEnable).eq(true).list());
            }
        }
        val queryChain = QueryChain.of(TestResourcePool.class).where(TEST_RESOURCE_POOL.ALL_ORG.eq(true).and(TEST_RESOURCE_POOL.ENABLE.eq(true)));
        testResourcePools.addAll(queryChain.list());
        testResourcePools = testResourcePools.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<String> moduleIds = request.getModulesIds();
        testResourcePools.forEach(pool -> {
            if (moduleIds.contains(API_TEST) || moduleIds.contains(TEST_PLAN)) {
                OptionDTO optionDTO = new OptionDTO();
                optionDTO.setId(pool.getId());
                optionDTO.setName(pool.getName());
                optionDTOS.add(optionDTO);
            }
        });
        return optionDTOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rename(UpdateProjectNameRequest request, String userId) {
        checkProjectNotExist(request.getId());
        Project project = new Project();
        project.setId(request.getId());
        project.setName(request.getName());
        project.setOrganizationId(request.getOrganizationId());
        checkProjectExistByName(project);
        project.setUpdateUser(userId);
        mapper.update(project);
    }

    @Override
    public List<String> getProjectIdByOrgId(String orgId) {
        return queryChain().where(Project::getOrganizationId).eq(orgId).list()
                .stream()
                .map(Project::getId).toList();
    }

    private void setLog(LogDTO dto, String path, String method, List<LogDTO> logDTOList) {
        dto.setPath(path);
        dto.setMethod(method);
        dto.setOriginalValue(JsonUtils.toJsonBytes(StringUtils.EMPTY));
        logDTOList.add(dto);
    }
}
