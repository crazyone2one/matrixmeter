package cn.master.matrix.service.impl;

import cn.master.matrix.constants.UserRoleType;
import cn.master.matrix.entity.*;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.mapper.UserMapper;
import cn.master.matrix.payload.dto.TableBatchProcessDTO;
import cn.master.matrix.payload.dto.request.BasePageRequest;
import cn.master.matrix.payload.dto.request.user.PersonalUpdatePasswordRequest;
import cn.master.matrix.payload.dto.request.user.PersonalUpdateRequest;
import cn.master.matrix.payload.dto.request.user.UserCreateRequest;
import cn.master.matrix.payload.dto.request.user.UserEditRequest;
import cn.master.matrix.payload.dto.user.*;
import cn.master.matrix.payload.dto.user.response.UserBatchCreateResponse;
import cn.master.matrix.payload.response.TableBatchProcessResponse;
import cn.master.matrix.service.*;
import cn.master.matrix.service.log.UserLogService;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static cn.master.matrix.entity.table.OrganizationTableDef.ORGANIZATION;
import static cn.master.matrix.entity.table.ProjectTableDef.PROJECT;
import static cn.master.matrix.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static cn.master.matrix.entity.table.UserTableDef.USER;

/**
 * 用户 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-21T10:54:08.016115500
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final GlobalUserRoleService globalUserRoleService;
    @Qualifier("baseUserRoleRelationService")
    private final BaseUserRoleRelationService userRoleRelationService;
    private final UserToolService userToolService;
    private final UserRolePermissionService userRolePermissionService;
    private final PasswordEncoder passwordEncoder;
    private final UserLogService userLogService;
    private final OperationLogService operationLogService;

    @Override
    public UserDTO getUserByKeyword(String keyword) {
        val one = queryChain().where(User::getEmail).eq(keyword).or(User::getId).eq(keyword)
                .oneAs(UserDTO.class);
        if (Objects.nonNull(one)) {
            val userRoleRelations = QueryChain.of(UserRoleRelation.class).where(UserRoleRelation::getUserId).eq(one.getId()).list();
            one.setUserRoleRelations(userRoleRelations);
            val roleIds = userRoleRelations.stream().map(UserRoleRelation::getRoleId).toList();
            val userRoles = QueryChain.of(UserRole.class).where(UserRole::getId).in(roleIds).list();
            one.setUserRoles(userRoles);
        }
        return one;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserEditRequest updateUser(UserEditRequest request, String operator) {
        globalUserRoleService.checkRoleIsGlobalAndHaveMember(request.getUserRoleIdList(), true);
        checkUserEmail(request.getId(), request.getEmail());
        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setUpdateUser(operator);
        mapper.update(user);
        userRoleRelationService.updateUserSystemGlobalRole(user, user.getUpdateUser(), request.getUserRoleIdList());
        return request;
    }

    @Override
    public Page<UserTableResponse> page(BasePageRequest request) {
        val page = queryChain()
                .where(USER.ID.eq(request.getKeyword())
                        .or(USER.EMAIL.like(request.getKeyword()))
                        .or(USER.NAME.like(request.getKeyword()))
                        .or(USER.PHONE.like(request.getKeyword())))
                .orderBy(USER.CREATE_TIME.desc(), USER.ID.desc())
                .pageAs(new Page<>(request.getPageNum(), request.getPageSize()), UserTableResponse.class);
        val userList = page.getRecords();
        if (CollectionUtils.isNotEmpty(userList)) {
            List<String> userIdList = userList.stream().map(User::getId).toList();
            Map<String, UserTableResponse> roleAndOrganizationMap = userRoleRelationService.selectGlobalUserRoleAndOrganization(userIdList);
            userList.forEach(userInfo -> {
                UserTableResponse roleOrgModel = roleAndOrganizationMap.get(userInfo.getId());
                if (roleOrgModel != null) {
                    userInfo.setUserRoleList(roleOrgModel.getUserRoleList());
                    userInfo.setOrganizationList(roleOrgModel.getOrganizationList());
                }
            });
        }
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TableBatchProcessResponse deleteUser(TableBatchProcessDTO request, String userId, String username) {
        List<String> userIdList = userToolService.getBatchUserIds(request);
        checkUserInDb(userIdList);
        checkProcessUserAndThrowException(userIdList, userId, username, Translator.get("user.not.delete"));
        TableBatchProcessResponse response = new TableBatchProcessResponse();
        response.setTotalCount(userIdList.size());
        response.setSuccessCount(mapper.deleteBatchByIds(userIdList));
        userRoleRelationService.deleteByUserIdList(userIdList);
        return response;
    }

    @Override
    public UserBatchCreateResponse save(UserCreateRequest userCreateDTO, String source, String operator) {

        globalUserRoleService.checkRoleIsGlobalAndHaveMember(userCreateDTO.getUserRoleIdList(), true);
        UserBatchCreateResponse response = new UserBatchCreateResponse();
        //检查用户邮箱的合法性
        Map<String, String> errorEmails = this.validateUserInfo(userCreateDTO.getUserInfoList().stream().map(UserCreateInfo::getEmail).toList());
        if (MapUtils.isNotEmpty(errorEmails)) {
            response.setErrorEmails(errorEmails);
        } else {
            response.setSuccessList(this.saveUserAndRole(userCreateDTO, source, operator, "/system/user/addUser"));
        }
        return response;
    }

    private List<UserCreateInfo> saveUserAndRole(UserCreateRequest userCreateDTO, String source, String operator, String requestPath) {
        List<UserCreateInfo> insertList = new ArrayList<>();
        List<User> saveUserList = new ArrayList<>();
        for (UserCreateInfo userCreateInfo : userCreateDTO.getUserInfoList()) {
            User user = new User();
            BeanUtils.copyProperties(userCreateInfo, user);
            user.setCreateUser(operator);
            user.setUpdateUser(operator);
            user.setSource(source);
            user.setPassword(passwordEncoder.encode(user.getEmail()));
            user.setEnable(false);
            mapper.insert(user);
            saveUserList.add(user);
            insertList.add(userCreateInfo);
        }
        userRoleRelationService.batchSave(userCreateDTO.getUserRoleIdList(), saveUserList);
        operationLogService.batchAdd(userLogService.getBatchAddLogs(userCreateDTO.getUserInfoList(), operator, requestPath));
        return insertList;
    }

    private Map<String, String> validateUserInfo(List<String> list) {
        Map<String, String> errorMessage = new HashMap<>();
        String userEmailRepeatError = Translator.get("user.email.repeat");
        List<String> emailList = new ArrayList<>();
        val userInDbMap = queryChain().where(User::getEmail).in(list).list().stream().collect(Collectors.toMap(User::getEmail, User::getId));
        for (String createEmail : list) {
            if (emailList.contains(createEmail)) {
                errorMessage.put(createEmail, userEmailRepeatError);
            } else {
                //判断邮箱是否已存在数据库中
                if (userInDbMap.containsKey(createEmail)) {
                    errorMessage.put(createEmail, userEmailRepeatError);
                } else {
                    emailList.add(createEmail);
                }
            }
        }
        return errorMessage;
    }

    @Override
    public UserDTO getUserDTO(String userId) {
        val userDTO = queryChain().where(User::getId).eq(userId).oneAs(UserDTO.class);
        if (Objects.isNull(userDTO)) {
            return null;
        }
        if (BooleanUtils.isFalse(userDTO.getEnable())) {
            throw new DisabledException(Translator.get("user_has_been_disabled"));
        }
        UserRolePermissionDTO dto = userRolePermissionService.getUserRolePermission(userId);
        userDTO.setUserRoleRelations(dto.getUserRoleRelations());
        userDTO.setUserRoles(dto.getUserRoles());
        userDTO.setUserRolePermissions(dto.getList());
        return userDTO;
    }

    @Override
    public PersonalDTO getPersonalById(String id) {
        val userDTO = getUserByKeyword(id);
        PersonalDTO personalDTO = new PersonalDTO();
        if (Objects.nonNull(userDTO)) {
            BeanUtils.copyProperties(userDTO, personalDTO);
            personalDTO.setOrgProjectList(userRoleRelationService.selectOrganizationProjectByUserId(userDTO.getId()));
        }
        return personalDTO;
    }

    @Override
    public boolean updateAccount(PersonalUpdateRequest request, String operator) {
        this.checkUserEmail(request.getId(), request.getEmail());
        User editUser = new User();
        editUser.setId(request.getId());
        editUser.setName(request.getUsername());
        editUser.setPhone(request.getPhone());
        editUser.setEmail(request.getEmail());
        editUser.setUpdateUser(operator);
        return mapper.update(editUser) > 0;
    }

    @Override
    public boolean updatePassword(PersonalUpdatePasswordRequest request) {
        this.checkOldPassword(request.getId(), request.getOldPassword());
        User editUser = new User();
        editUser.setId(request.getId());
        editUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        return mapper.update(editUser) > 0;
    }

    @Override
    public List<UserExtendDTO> getMemberOption(String sourceId, String keyword) {
        val wrapper = queryChain().select(QueryMethods.distinct(USER.ALL_COLUMNS))
                .from(USER).leftJoin(USER_ROLE_RELATION).on(USER.ID.eq(USER_ROLE_RELATION.USER_ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(sourceId)
                        .and(USER.NAME.like(keyword).or(USER.EMAIL.like(keyword))))
                .groupBy(USER.ID).limit(1000);
        return mapper.selectListByQueryAs(wrapper, UserExtendDTO.class);
    }

    @Override
    public List<User> getUserList(String keyword) {
        return queryChain().where(USER.NAME.like(keyword).or(USER.EMAIL.like(keyword))).list();
    }

    @Override
    public void autoSwitch(UserDTO user) {
        // 判断是否是系统管理员
        if (isSystemAdmin(user)) {
            return;
        }
        // 用户有 last_project_id 权限
        if (hasLastProjectPermission(user)) {
            return;
        }
        // 用户有 last_organization_id 权限
        if (hasLastOrganizationPermission(user)) {
            return;
        }
        // 判断其他权限
        checkNewOrganizationAndProject(user);
    }

    private void checkNewOrganizationAndProject(UserDTO user) {
        List<UserRoleRelation> userRoleRelations = user.getUserRoleRelations();
        List<String> projectRoleIds = user.getUserRoles()
                .stream().filter(ug -> StringUtils.equals(ug.getType(), UserRoleType.PROJECT.name()))
                .map(UserRole::getId)
                .toList();
        List<UserRoleRelation> project = userRoleRelations.stream().filter(ug -> projectRoleIds.contains(ug.getRoleId()))
                .toList();
        if (CollectionUtils.isEmpty(project)) {
            List<String> organizationIds = user.getUserRoles()
                    .stream()
                    .filter(ug -> StringUtils.equals(ug.getType(), UserRoleType.ORGANIZATION.name()))
                    .map(UserRole::getId)
                    .toList();
            List<UserRoleRelation> organizations = userRoleRelations.stream().filter(ug -> organizationIds.contains(ug.getRoleId()))
                    .toList();
            if (CollectionUtils.isNotEmpty(organizations)) {
                //获取所有的组织
                List<String> orgIds = organizations.stream().map(UserRoleRelation::getSourceId).toList();
                val organizationsList = QueryChain.of(Organization.class).where(ORGANIZATION.ID.in(orgIds)
                        .and(ORGANIZATION.ENABLE.eq(true))).list();
                if (CollectionUtils.isNotEmpty(organizationsList)) {
                    String wsId = organizationsList.get(0).getId();
                    switchUserResource(wsId, user);
                }
            } else {
                UpdateChain.of(mapper).set(User::getLastProjectId, StringUtils.EMPTY)
                        .set(User::getLastOrganizationId, StringUtils.EMPTY)
                        .where(User::getId).eq(user.getId())
                        .update();
            }
        } else {
            UserRoleRelation userRoleRelation = project.stream()
                    .filter(p -> StringUtils.isNotBlank(p.getSourceId()))
                    .toList().get(0);
            String projectId = userRoleRelation.getSourceId();

            val p = QueryChain.of(Project.class).where(PROJECT.ID.eq(projectId)).one();
            UpdateChain.of(mapper).set(User::getLastProjectId, projectId)
                    .set(User::getLastOrganizationId, p.getOrganizationId())
                    .where(User::getId).eq(user.getId())
                    .update();
        }
    }

    private void switchUserResource(String sourceId, UserDTO userDTO) {
        UserDTO user = getUserDTO(userDTO.getId());
        User newUser = new User();
        user.setLastOrganizationId(sourceId);
        userDTO.setLastOrganizationId(sourceId);
        user.setLastProjectId(StringUtils.EMPTY);
        List<Project> projects = getProjectListByWsAndUserId(userDTO.getId(), sourceId);
        if (CollectionUtils.isNotEmpty(projects)) {
            user.setLastProjectId(projects.get(0).getId());
        }
        BeanUtils.copyProperties(user, newUser);
        mapper.update(newUser);
    }

    private List<Project> getProjectListByWsAndUserId(String userId, String orgId) {
        val projects = QueryChain.of(Project.class).where(PROJECT.ORGANIZATION_ID.eq(orgId)
                .and(PROJECT.ENABLE.eq(true))).list();
        val userRoleRelations = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(userId)).list();
        List<Project> projectList = new ArrayList<>();
        userRoleRelations.forEach(userRoleRelation -> projects.forEach(project -> {
            if (StringUtils.equals(userRoleRelation.getSourceId(), project.getId())) {
                if (!projectList.contains(project)) {
                    projectList.add(project);
                }
            }
        }));
        return projectList;
    }

    private boolean hasLastOrganizationPermission(UserDTO user) {
        if (StringUtils.isNotBlank(user.getLastOrganizationId())) {
            val organizations = QueryChain.of(Organization.class).where(ORGANIZATION.ID.eq(user.getLastOrganizationId())
                    .and(ORGANIZATION.ENABLE.eq(true))).list();
            if (CollectionUtils.isEmpty(organizations)) {
                return false;
            }
            List<UserRoleRelation> userRoleRelations = user.getUserRoleRelations().stream()
                    .filter(ug -> StringUtils.equals(user.getLastOrganizationId(), ug.getSourceId()))
                    .toList();

            if (CollectionUtils.isNotEmpty(userRoleRelations)) {
                val projects = QueryChain.of(Project.class).where(PROJECT.ORGANIZATION_ID.eq(user.getLastOrganizationId())
                        .and(PROJECT.ENABLE.eq(true))).list();
                // 组织下没有项目
                if (CollectionUtils.isEmpty(projects)) {
                    UpdateChain.of(mapper).set(User::getLastProjectId, StringUtils.EMPTY)
                            .where(User::getId).eq(user.getId())
                            .update();
                    return true;
                }
                // 组织下有项目，选中有权限的项目
                List<String> projectIds = projects.stream()
                        .map(Project::getId)
                        .toList();
                List<UserRoleRelation> roleRelations = user.getUserRoleRelations();
                List<String> projectRoleIds = user.getUserRoles()
                        .stream().filter(ug -> StringUtils.equals(ug.getType(), UserRoleType.PROJECT.name()))
                        .map(UserRole::getId)
                        .toList();
                List<String> projectIdsWithPermission = roleRelations.stream().filter(ug -> projectRoleIds.contains(ug.getRoleId()))
                        .map(UserRoleRelation::getSourceId)
                        .filter(StringUtils::isNotBlank)
                        .filter(projectIds::contains)
                        .toList();
                List<String> intersection = projectIds.stream().filter(projectIdsWithPermission::contains).toList();
                // 当前组织下的所有项目都没有权限
                if (CollectionUtils.isEmpty(intersection)) {
                    UpdateChain.of(mapper).set(User::getLastProjectId, StringUtils.EMPTY)
                            .where(User::getId).eq(user.getId())
                            .update();
                    return true;
                }
                Optional<Project> first = projects.stream().filter(p -> StringUtils.equals(intersection.get(0), p.getId())).findFirst();
                if (first.isPresent()) {
                    Project project = first.get();
                    String wsId = project.getOrganizationId();
                    UpdateChain.of(mapper).set(User::getLastProjectId, project.getId())
                            .set(User::getLastOrganizationId, wsId)
                            .where(User::getId).eq(user.getId())
                            .update();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasLastProjectPermission(UserDTO user) {
        if (StringUtils.isNotBlank(user.getLastProjectId())) {
            val userRoleRelations = user.getUserRoleRelations().stream()
                    .filter(ur -> StringUtils.equals(user.getLastProjectId(), ur.getSourceId()))
                    .toList();
            if (CollectionUtils.isNotEmpty(userRoleRelations)) {
                val projects = QueryChain.of(Project.class).where(PROJECT.ID.eq(user.getLastProjectId())
                        .and(PROJECT.ENABLE.eq(true))).list();
                if (CollectionUtils.isNotEmpty(projects)) {
                    val project = projects.get(0);
                    if (StringUtils.equals(project.getOrganizationId(), user.getLastOrganizationId())) {
                        return true;
                    }
                    UpdateChain.of(mapper).set(User::getLastOrganizationId, project.getOrganizationId())
                            .where(User::getId).eq(user.getId())
                            .update();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSystemAdmin(UserDTO user) {
        if (userRoleRelationService.isSuperUser(user.getId())) {
            // 如果是系统管理员，判断是否有项目权限
            if (StringUtils.isNotBlank(user.getLastProjectId())) {
                val projects = QueryChain.of(Project.class).where(PROJECT.ID.eq(user.getLastProjectId())
                        .and(PROJECT.ENABLE.eq(true))).list();
                if (CollectionUtils.isNotEmpty(projects)) {
                    val project = projects.get(0);
                    if (StringUtils.equals(project.getOrganizationId(), user.getLastOrganizationId())) {
                        return true;
                    }
                    UpdateChain.of(mapper).set(User::getLastOrganizationId, project.getOrganizationId())
                            .where(User::getId).eq(user.getId()).update();
                    return true;
                }
            }
            // 项目没有权限  则取当前组织下的第一个项目
            if (StringUtils.isNotBlank(user.getLastOrganizationId())) {
                val organizations = QueryChain.of(Organization.class).where(Organization::getId).eq(user.getLastOrganizationId())
                        .and(Organization::getEnable).eq(true).list();
                if (CollectionUtils.isNotEmpty(organizations)) {
                    val organization = organizations.get(0);
                    val projects = QueryChain.of(Project.class).where(Project::getOrganizationId).eq(organization.getId())
                            .and(Project::getEnable).eq(true).list();
                    if (CollectionUtils.isNotEmpty(projects)) {
                        val project = projects.get(0);
                        UpdateChain.of(mapper).set(User::getLastProjectId, project.getId())
                                .where(User::getId).eq(user.getId()).update();
                        return true;
                    }
                }
            }
            //项目和组织都没有权限
            Project project = getEnableProjectAndOrganization();
            if (Objects.nonNull(project)) {
                UpdateChain.of(mapper).set(User::getLastProjectId, project.getId())
                        .set(User::getLastOrganizationId, project.getOrganizationId())
                        .where(User::getId).eq(user.getId()).update();
                return true;
            }
            return true;
        }
        return false;
    }

    private Project getEnableProjectAndOrganization() {
        return QueryChain.of(Project.class).select(PROJECT.ALL_COLUMNS)
                .from(PROJECT)
                .leftJoin(ORGANIZATION).on(PROJECT.ORGANIZATION_ID.eq(ORGANIZATION.ID))
                .where(PROJECT.ENABLE.eq(true).and(ORGANIZATION.ENABLE.eq(true))).limit(1).one();
    }

    private void checkOldPassword(String id, String oldPassword) {
        queryChain().where(User::getId).eq(id)
                .and(User::getPassword).eq(passwordEncoder.encode(oldPassword)).oneOpt()
                .orElseThrow(() -> new CustomException(Translator.get("password_modification_failed")));
    }

    private void checkUserInDb(List<String> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            throw new CustomException(Translator.get("user.not.exist"));
        }
        val count = queryChain().where(User::getId).in(userIdList).count();
        if (userIdList.size() != count) {
            throw new CustomException(Translator.get("user.not.exist"));
        }
    }

    private void checkProcessUserAndThrowException(List<String> userIdList, String operatorId, String operatorName, String exceptionMessage) {
        for (String userId : userIdList) {
            //当前用户或admin不能被操作
            if (StringUtils.equals(userId, operatorId)) {
                throw new CustomException(exceptionMessage + ":" + operatorName);
            } else if (StringUtils.equals(userId, "admin")) {
                throw new CustomException(exceptionMessage + ": admin");
            }
        }
    }

    private void checkUserEmail(String id, String email) {
        queryChain().where(User::getEmail).eq(email).and(User::getId).ne(id)
                .oneOpt()
                .orElseThrow(() -> new CustomException(Translator.get("user_email_already_exists")));
    }
}
