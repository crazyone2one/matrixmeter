package cn.master.matrix.service.impl;

import cn.master.matrix.constants.*;
import cn.master.matrix.entity.*;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.mapper.OrganizationMapper;
import cn.master.matrix.mapper.UserMapper;
import cn.master.matrix.mapper.UserRoleRelationMapper;
import cn.master.matrix.payload.dto.*;
import cn.master.matrix.payload.dto.request.*;
import cn.master.matrix.payload.dto.user.UserExtendDTO;
import cn.master.matrix.service.OperationLogService;
import cn.master.matrix.service.OrganizationService;
import cn.master.matrix.util.JsonUtils;
import cn.master.matrix.util.ServiceUtils;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.query.SelectQueryTable;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.master.matrix.entity.table.OrganizationTableDef.ORGANIZATION;
import static cn.master.matrix.entity.table.ProjectTableDef.PROJECT;
import static cn.master.matrix.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static cn.master.matrix.entity.table.UserRoleTableDef.USER_ROLE;
import static cn.master.matrix.entity.table.UserTableDef.USER;

/**
 * 组织 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-28T15:16:07.028133900
 */
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements OrganizationService {
    private final UserRoleRelationMapper userRoleRelationMapper;
    private final UserMapper userMapper;
    private final OperationLogService operationLogService;

    private static final String ADD_MEMBER_PATH = "/system/organization/add-member";
    private static final String REMOVE_MEMBER_PATH = "/system/organization/remove-member";
    public static final Integer DEFAULT_REMAIN_DAY_COUNT = 30;
    private static final Long DEFAULT_ORGANIZATION_NUM = 100001L;

    @Override
    public List<OptionDTO> listAll() {
        val organizations = mapper.selectAll();
        return organizations.stream().map(o -> new OptionDTO(o.getId(), o.getName())).toList();
    }

    @Override
    public LinkedHashMap<Organization, List<Project>> getOrgProjectMap() {
        val projects = QueryChain.of(Project.class).list();
        if (CollectionUtils.isNotEmpty(projects)) {
            LinkedHashMap<Organization, List<Project>> returnMap = new LinkedHashMap<>();
            val organizations = queryChain()
                    .where(Organization::getId).in(projects.stream().map(Project::getOrganizationId).toList()).list();
            for (Organization org : organizations) {
                List<Project> projectsInOrg = new ArrayList<>();
                for (Project project : projects) {
                    if (StringUtils.equals(project.getOrganizationId(), org.getId())) {
                        projectsInOrg.add(project);
                    }
                }
                projects.remove(projectsInOrg);
                returnMap.put(org, projectsInOrg);
            }
            return returnMap;
        } else {
            return new LinkedHashMap<>();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMemberBySystem(OrganizationMemberBatchRequest request, String createUserId) {
        checkOrgExistByIds(request.getOrganizationIds());
        Map<String, User> userMap = checkUserExist(request.getUserIds());
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        request.getOrganizationIds().forEach(organizationId -> {
            for (String userId : request.getUserIds()) {
                if (userMap.get(userId) == null) {
                    throw new CustomException(Translator.get("user.not.exist") + ", id: " + userId);
                }
                //组织用户关系已存在, 不再重复添加
                val exists = QueryChain.of(userRoleRelationMapper).where(UserRoleRelation::getUserId).eq(userId)
                        .and(UserRoleRelation::getSourceId).eq(organizationId).exists();
                if (exists) {
                    continue;
                }
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setUserId(userId);
                userRoleRelation.setSourceId(organizationId);
                userRoleRelation.setRoleId(InternalUserRole.ORG_MEMBER.getValue());
                userRoleRelation.setCreateUser(createUserId);
                userRoleRelation.setOrganizationId(organizationId);
                userRoleRelations.add(userRoleRelation);
            }
        });
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            userRoleRelationMapper.insertBatch(userRoleRelations);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMemberBySystem(OrganizationMemberRequest organizationMemberRequest, String createUserId) {
        List<LogDTO> logs = new ArrayList<>();
        OrganizationMemberBatchRequest batchRequest = new OrganizationMemberBatchRequest();
        batchRequest.setOrganizationIds(List.of(organizationMemberRequest.getOrganizationId()));
        batchRequest.setUserIds(organizationMemberRequest.getUserIds());
        addMemberBySystem(batchRequest, createUserId);
        // 添加日志
        List<User> users = QueryChain.of(User.class).where(USER.ID.in(batchRequest.getUserIds())).list();
        List<String> nameList = users.stream().map(User::getName).collect(Collectors.toList());
        setLog(organizationMemberRequest.getOrganizationId(), createUserId, OperationLogType.ADD.name(), Translator.get("add") + Translator.get("organization_member_log") + ": " + StringUtils.join(nameList, ","), ADD_MEMBER_PATH, null, null, logs);
        operationLogService.batchAdd(logs);
    }

    @Override
    public Page<OrgUserExtend> getMemberListByOrg(OrganizationRequest request) {
        String organizationId = request.getOrganizationId();
        Page<OrgUserExtend> page = listMemberByOrg(request);
        val orgUserExtends = page.getRecords();
        if (CollectionUtils.isEmpty(orgUserExtends)) {
            return new Page<>();
        }
        Map<String, OrgUserExtend> userMap = orgUserExtends.stream().collect(Collectors.toMap(OrgUserExtend::getId, user -> user));
        val userRoleRelations = QueryChain.of(userRoleRelationMapper)
                .where(USER_ROLE_RELATION.USER_ID.in(new ArrayList<>(userMap.keySet()))
                        .and(USER_ROLE_RELATION.ORGANIZATION_ID.likeRaw(organizationId)))
                .orderBy(USER_ROLE_RELATION.CREATE_TIME.desc())
                .list();
        Map<String, Set<String>> userIdRoleIdMap = new HashMap<>();
        Map<String, Set<String>> userIdProjectIdMap = new HashMap<>();
        Set<String> roleIdSet = new HashSet<>();
        Set<String> projectIdSet = new HashSet<>();
        for (UserRoleRelation userRoleRelationsByUser : userRoleRelations) {
            String sourceId = userRoleRelationsByUser.getSourceId();
            String roleId = userRoleRelationsByUser.getRoleId();
            String userId = userRoleRelationsByUser.getUserId();
            //收集组织级别的用户组
            if (StringUtils.equals(sourceId, organizationId)) {
                getTargetIds(userIdRoleIdMap, roleIdSet, roleId, userId);
            }
            //收集项目id
            if (!StringUtils.equals(sourceId, organizationId)) {
                getTargetIds(userIdProjectIdMap, projectIdSet, sourceId, userId);
            }
        }
        val userRoles = QueryChain.of(UserRole.class).where(UserRole::getId).in(new ArrayList<>(roleIdSet)).list();
        List<Project> projects = new ArrayList<>();
        if (!projectIdSet.isEmpty()) {
            projects = QueryChain.of(Project.class).where(Project::getId).in(new ArrayList<>(projectIdSet)).list();
        }
        for (OrgUserExtend orgUserExtend : orgUserExtends) {
            if (!projects.isEmpty()) {
                Set<String> projectIds = userIdProjectIdMap.get(orgUserExtend.getId());
                if (CollectionUtils.isNotEmpty(projectIds)) {
                    List<Project> projectFilters = projects.stream().filter(t -> projectIds.contains(t.getId())).toList();
                    List<OptionDTO> projectList = new ArrayList<>();
                    setProjectList(projectList, projectFilters);
                    orgUserExtend.setProjectIdNameMap(projectList);
                }
            }
            Set<String> userRoleIds = userIdRoleIdMap.get(orgUserExtend.getId());
            List<UserRole> userRoleFilters = userRoles.stream().filter(t -> userRoleIds.contains(t.getId())).toList();
            List<OptionDTO> userRoleList = new ArrayList<>();
            setUserRoleList(userRoleList, userRoleFilters);
            orgUserExtend.setUserRoleIdNameMap(userRoleList);
        }
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMemberByOrg(OrganizationMemberExtendRequest request, String createUserId) {
        String organizationId = request.getOrganizationId();
        checkOrgExistById(organizationId);
        Map<String, User> userMap;
        userMap = getUserMap(request);
        Map<String, UserRole> userRoleMap = checkUseRoleExist(request.getUserRoleIds(), organizationId);
        setRelationByMemberAndGroupIds(request, createUserId, userMap, userRoleMap, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMemberRole(OrganizationMemberExtendRequest request, String userId) {
        String organizationId = request.getOrganizationId();
        checkOrgExistById(organizationId);
        Map<String, User> userMap;
        userMap = getUserMap(request);
        Map<String, UserRole> userRoleMap = checkUseRoleExist(request.getUserRoleIds(), organizationId);
        //在新增组织成员与用户组和组织的关系
        setRelationByMemberAndGroupIds(request, userId, userMap, userRoleMap, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMember(OrganizationMemberUpdateRequest request, String createUserId) {
        String organizationId = request.getOrganizationId();
        //校验组织是否存在
        checkOrgExistById(organizationId);
        //校验用户是否存在
        String memberId = request.getMemberId();
        User user = userMapper.selectOneById(memberId);
        if (user == null) {
            throw new CustomException(Translator.get("user.not.exist"));
        }
        val userRoleRelations = QueryChain.of(userRoleRelationMapper)
                .where(USER_ROLE_RELATION.USER_ID.eq(memberId).and(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId)))
                .list();
        if (CollectionUtils.isEmpty(userRoleRelations)) {
            throw new CustomException(Translator.get("organization_member_not_exist"));
        }
        List<LogDTO> logDTOList = new ArrayList<>();
        //更新用户组
        List<String> userRoleIds = request.getUserRoleIds();
        updateUserRoleRelation(createUserId, organizationId, user, userRoleIds, logDTOList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMemberToProject(OrgMemberExtendProjectRequest request, String userId) {
        String requestOrganizationId = request.getOrganizationId();
        checkOrgExistById(requestOrganizationId);
        List<LogDTO> logDTOList = new ArrayList<>();
        List<String> projectIds = request.getProjectIds();
        Map<String, User> userMap;
        if (request.isSelectAll()) {
            OrganizationRequest organizationRequest = new OrganizationRequest();
            BeanUtils.copyProperties(request, organizationRequest);
            List<OrgUserExtend> orgUserExtends = listMemberByOrg(organizationRequest).getRecords();
            List<String> excludeIds = request.getExcludeIds();
            if (CollectionUtils.isNotEmpty(excludeIds)) {
                userMap = orgUserExtends.stream().filter(user -> !excludeIds.contains(user.getId())).collect(Collectors.toMap(User::getId, user -> user));
            } else {
                userMap = orgUserExtends.stream().collect(Collectors.toMap(User::getId, user -> user));
            }
        } else {
            userMap = checkUserExist(request.getMemberIds());
        }
        List<String> userIds = userMap.values().stream().map(User::getId).toList();
        userIds.forEach(memberId -> {
            projectIds.forEach(projectId -> {
                //过滤已存在的关系
                List<UserRoleRelation> userRoleRelations = QueryChain.of(userRoleRelationMapper)
                        .where(USER_ROLE_RELATION.SOURCE_ID.eq(projectId).and(USER_ROLE_RELATION.USER_ID.eq(memberId))).list();
                if (CollectionUtils.isEmpty(userRoleRelations)) {
                    UserRoleRelation userRoleRelation = buildUserRoleRelation(userId, memberId, projectId, InternalUserRole.PROJECT_MEMBER.getValue(), requestOrganizationId);
                    userRoleRelation.setOrganizationId(request.getOrganizationId());
                    userRoleRelationMapper.insert(userRoleRelation);
                    //add Log
                    LogDTO dto = new LogDTO(
                            projectId,
                            requestOrganizationId,
                            memberId,
                            userId,
                            OperationLogType.ADD.name(),
                            OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
                            "");
                    setLog(dto, "/organization/project/add-member", logDTOList, userRoleRelation);
                }
            });
        });
        operationLogService.batchAdd(logDTOList);
    }

    @Override
    public List<LogDTO> batchDelLog(String organizationId, String userId) {
        List<String> projectIds = getProjectIds(organizationId);
        List<LogDTO> dtoList = new ArrayList<>();
        val user = userMapper.selectOneById(userId);
        if (CollectionUtils.isNotEmpty(projectIds)) {
            List<UserRoleRelation> userRoleRelations = QueryChain.of(userRoleRelationMapper)
                    .where(USER_ROLE_RELATION.SOURCE_ID.in(projectIds).and(USER_ROLE_RELATION.USER_ID.eq(userId))).list();
            userRoleRelations.forEach(userRoleRelation -> {
                LogDTO dto = new LogDTO(
                        userRoleRelation.getSourceId(),
                        organizationId,
                        userId,
                        userRoleRelation.getCreateUser(),
                        OperationLogType.DELETE.name(),
                        OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
                        user.getName());

                dto.setPath("/organization/remove-member/{organizationId}/{userId}");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JsonUtils.toJsonBytes(userRoleRelation));
                dtoList.add(dto);
            });
        }
        List<UserRoleRelation> userRoleWidthOrgRelations = QueryChain.of(userRoleRelationMapper)
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId).and(USER_ROLE_RELATION.USER_ID.eq(userId))).list();
        //记录组织日志
        for (UserRoleRelation userRoleWidthOrgRelation : userRoleWidthOrgRelations) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    organizationId,
                    userRoleWidthOrgRelation.getId(),
                    userRoleWidthOrgRelation.getCreateUser(),
                    OperationLogType.DELETE.name(),
                    OperationLogModule.SETTING_ORGANIZATION_MEMBER,
                    user.getName());

            dto.setPath("/organization/remove-member/{organizationId}/{userId}");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JsonUtils.toJsonBytes(userRoleWidthOrgRelation));
            dtoList.add(dto);
        }
        return dtoList;
    }

    private List<String> getProjectIds(String organizationId) {
        val projects = QueryChain.of(Project.class).where(Project::getOrganizationId).eq(organizationId).list();
        if (CollectionUtils.isEmpty(projects)) {
            return List.of();
        }
        return projects.stream().map(Project::getId).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(String organizationId, String userId, String currentUser) {
        List<LogDTO> logs = new ArrayList<>();
        checkOrgExistById(organizationId);
        //检查用户是不是最后一个管理员
        val exists = QueryChain.of(userRoleRelationMapper)
                .where(USER_ROLE_RELATION.USER_ID.eq(userId)
                        .and(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId))
                        .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.ORG_ADMIN.getValue()))).exists();
        if (!exists) {
            throw new CustomException(Translator.get("keep_at_least_one_administrator"));
        }
        //删除组织下项目与成员的关系
        List<String> projectIds = getProjectIds(organizationId);
        if (CollectionUtils.isNotEmpty(projectIds)) {
            val queryChain = QueryChain.of(userRoleRelationMapper)
                    .where(USER_ROLE_RELATION.USER_ID.eq(userId)
                            .and(USER_ROLE_RELATION.SOURCE_ID.in(projectIds)));
            LogicDeleteManager.execWithoutLogicDelete(() -> userRoleRelationMapper.deleteByQuery(queryChain));
        }
        //删除组织与成员的关系
        val queryChain = QueryChain.of(userRoleRelationMapper)
                .where(USER_ROLE_RELATION.USER_ID.eq(userId)
                        .and(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId)));
        LogicDeleteManager.execWithoutLogicDelete(() -> userRoleRelationMapper.deleteByQuery(queryChain));
        // 操作记录
        User user = userMapper.selectOneById(userId);
        setLog(organizationId, currentUser, OperationLogType.DELETE.name(), Translator.get("delete") + Translator.get("organization_member_log") + ": " + user.getName(), REMOVE_MEMBER_PATH, user, null, logs);
        operationLogService.batchAdd(logs);
    }

    @Override
    public List<OptionDTO> getProjectList(String organizationId, String keyword) {
        //校验组织是否存在
        checkOrgExistById(organizationId);
        return QueryChain.of(Project.class)
                .where(Project::getEnable).eq(1)
                .and(Project::getOrganizationId).eq(organizationId)
                .and(Project::getName).like(keyword)
                .orderBy(Project::getUpdateTime).desc().limit(1000)
                .listAs(OptionDTO.class);
    }

    @Override
    public List<OptionDTO> getUserRoleList(String organizationId) {
        //校验组织是否存在
        checkOrgExistById(organizationId);
        List<String> scopeIds = Arrays.asList(UserRoleEnum.GLOBAL.toString(), organizationId);
        List<OptionDTO> userRoleList = new ArrayList<>();
        val userRoles = QueryChain.of(UserRole.class)
                .where(USER_ROLE.TYPE.eq(UserRoleType.ORGANIZATION.toString())
                        .and(USER_ROLE.SCOPE_ID.in(scopeIds)))
                .list();
        setUserRoleList(userRoleList, userRoles);
        return userRoleList;
    }

    @Override
    public List<OptionDisabledDTO> getUserList(String organizationId, String keyword) {
        //校验组织是否存在
        checkOrgExistById(organizationId);
        List<OptionDisabledDTO> optionDisabledDTOS = QueryChain.of(userMapper)
                .where(USER.NAME.like(keyword)
                        .or(USER.EMAIL.like(keyword))
                        .or(USER.PHONE.like(keyword)))
                .orderBy(USER.UPDATE_TIME.desc())
                .limit(1000)
                .listAs(OptionDisabledDTO.class);
        val userIds = QueryChain.of(userRoleRelationMapper).where(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId)).list()
                .stream().map(UserRoleRelation::getUserId).distinct().toList();
        for (OptionDisabledDTO optionDisabledDTO : optionDisabledDTOS) {
            if (CollectionUtils.isNotEmpty(userIds) && userIds.contains(optionDisabledDTO.getId())) {
                optionDisabledDTO.setDisabled(true);
            }
        }
        return optionDisabledDTOS;
    }

    @Override
    public Organization checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(mapper.selectOneById(id), "permission.system_organization_project.name");
    }

    @Override
    public Page<OrganizationDTO> list(OrganizationRequest request) {
        val queryChain = queryChain()
                .where(ORGANIZATION.NAME.like(request.getKeyword())
                        .or(ORGANIZATION.NUM.like(request.getKeyword())))
                .orderBy(ORGANIZATION.CREATE_TIME.desc());
        val page = mapper.paginateAs(Page.of(request.getPageNum(), request.getPageSize()), queryChain, OrganizationDTO.class);
        val records = page.getRecords();
        List<OrganizationDTO> organizations = buildOrgAdminInfo(records);
        buildExtraInfo(organizations);
        return page;
    }

    @Override
    public List<String> getOrgAdminIds(String organizationId) {
        return QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId)
                        .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.ORG_ADMIN.getValue())))
                .list()
                .stream().map(UserRoleRelation::getUserId).toList();
    }

    @Override
    public void update(OrganizationDTO organizationDTO) {
        checkOrganizationNotExist(organizationDTO.getId());
        checkOrganizationExist(organizationDTO);
        mapper.update(organizationDTO);
        // 新增的组织管理员ID
        List<String> addOrgAdmins = organizationDTO.getUserIds();
        // 旧的组织管理员ID
        List<String> oldOrgAdmins = getOrgAdminIds(organizationDTO.getId());
        // 需要新增组织管理员ID
        List<String> addIds = addOrgAdmins.stream().filter(addOrgAdmin -> !oldOrgAdmins.contains(addOrgAdmin)).toList();
        // 需要删除的组织管理员ID
        List<String> deleteIds = oldOrgAdmins.stream().filter(oldOrgAdmin -> !addOrgAdmins.contains(oldOrgAdmin)).toList();
        // 添加组织管理员
        if (CollectionUtils.isNotEmpty(addIds)) {
            addIds.forEach(userId -> {
                // 添加组织管理员
                createAdmin(userId, organizationDTO.getId(), organizationDTO.getUpdateUser());
            });
        }
        // 删除组织管理员
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            val queryChain = QueryChain.of(userRoleRelationMapper).where(USER_ROLE_RELATION.SOURCE_ID.eq(organizationDTO.getId())
                    .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.ORG_ADMIN.getValue()))
                    .and(USER_ROLE_RELATION.USER_ID.in(deleteIds)));
            LogicDeleteManager.execWithoutLogicDelete(() -> userRoleRelationMapper.deleteByQuery(queryChain));
        }
    }

    @Override
    public void updateName(OrganizationDTO organizationDTO) {
        checkOrganizationNotExist(organizationDTO.getId());
        checkOrganizationExist(organizationDTO);
        updateChain().set(ORGANIZATION.NAME, organizationDTO.getName())
                .where(ORGANIZATION.ID.eq(organizationDTO.getId()))
                .update();
    }

    @Override
    public void delete(OrganizationDeleteRequest organizationDeleteRequest) {
        // 默认组织不允许删除
        checkOrgDefault(organizationDeleteRequest.getOrganizationId());
        checkOrganizationNotExist(organizationDeleteRequest.getOrganizationId());
        organizationDeleteRequest.setDeleteTime(LocalDateTime.now());
        //mapper.deleteById(organizationDeleteRequest);
        updateChain().set(ORGANIZATION.DELETE_TIME, System.currentTimeMillis())
                .set(ORGANIZATION.DELETE_USER, organizationDeleteRequest.getDeleteUserId())
                .set(ORGANIZATION.DELETED, true)
                .where(ORGANIZATION.ID.eq(organizationDeleteRequest.getOrganizationId()))
                .update();
    }

    @Override
    public void recover(String id) {
        checkOrganizationNotExist(id);
        // todo
    }

    @Override
    public void enable(String id) {
        checkOrganizationNotExist(id);
        updateChain().set(ORGANIZATION.ENABLE, true).where(ORGANIZATION.ID.eq(id)).update();
    }

    @Override
    public void disable(String id) {
        checkOrganizationNotExist(id);
        updateChain().set(ORGANIZATION.ENABLE, false).where(ORGANIZATION.ID.eq(id)).update();
    }

    @Override
    public Page<UserExtendDTO> getMemberListBySystem(OrganizationRequest request) {
        QueryWrapper wrapper = new QueryWrapper();
        QueryWrapper subWrapper = new QueryWrapper();
        subWrapper.select(USER.ALL_COLUMNS, USER_ROLE_RELATION.ROLE_ID, USER_ROLE_RELATION.CREATE_TIME.as("memberTime"))
                .from(USER_ROLE_RELATION).join(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getOrganizationId()))
                .and(USER.NAME.like(request.getKeyword())
                        .or(USER.EMAIL.like(request.getKeyword()))
                        .or(USER.PHONE.like(request.getKeyword())))
                .orderBy(USER_ROLE_RELATION.CREATE_TIME.desc());
        wrapper.select("temp.*")
                .select("max(if(temp.role_id = 'org_admin', true, false)) as adminFlag")
                .select("min(temp.memberTime) as groupTime")
                .from(subWrapper.as("temp"))
                .groupBy("temp.id")
                .orderBy("adminFlag", "groupTime");
        return userRoleRelationMapper.paginateAs(Page.of(request.getPageNum(), request.getPageSize()), wrapper, UserExtendDTO.class);
    }

    @Override
    public OrganizationDTO getDefault() {
        OrganizationDTO organizationDTO = new OrganizationDTO();
        val one = queryChain().where(Organization::getNum).eq(100001L).one();
        BeanUtils.copyProperties(one, organizationDTO);
        return organizationDTO;
    }

    @Override
    public Map<String, Long> getTotal(String organizationId) {
        Map<String, Long> total = new HashMap<>();
        if (StringUtils.isNotEmpty(organizationId)) {
            total.put("projectTotal", QueryChain.of(Project.class).where(Project::getOrganizationId).eq(organizationId).count());
            total.put("organizationTotal", 1L);
        } else {
            // 查询所有组织
            total.put("projectTotal", QueryChain.of(Project.class).count());
            total.put("organizationTotal", QueryChain.of(Organization.class).count());
        }
        return total;
    }

    private void checkOrgDefault(String id) {
        Organization organization = mapper.selectOneById(id);
        if (organization.getNum().equals(DEFAULT_ORGANIZATION_NUM)) {
            throw new CustomException(Translator.get("default_organization_not_allow_delete"));
        }
    }

    private void createAdmin(String memberId, String organizationId, String createUser) {
        UserRoleRelation orgAdmin = new UserRoleRelation();
        orgAdmin.setUserId(memberId);
        orgAdmin.setRoleId(InternalUserRole.ORG_ADMIN.getValue());
        orgAdmin.setSourceId(organizationId);
        orgAdmin.setCreateUser(createUser);
        orgAdmin.setOrganizationId(organizationId);
        userRoleRelationMapper.insertSelective(orgAdmin);
    }

    private void checkOrganizationExist(OrganizationDTO organizationDTO) {
        val exists = queryChain().where(Organization::getId).ne(organizationDTO.getId())
                .and(Organization::getName).eq(organizationDTO.getName()).exists();
        if (exists) {
            throw new CustomException(Translator.get("organization_name_already_exists"));
        }
    }

    private void checkOrganizationNotExist(String id) {
        if (Objects.isNull(mapper.selectOneById(id))) {
            throw new CustomException(Translator.get("organization_not_exist"));
        }
    }

    private void buildExtraInfo(List<OrganizationDTO> organizations) {
        List<String> ids = organizations.stream().map(OrganizationDTO::getId).toList();
        List<OrganizationCountDTO> orgCountList = getCountByIds(ids);
        Map<String, OrganizationCountDTO> orgCountMap = orgCountList.stream().collect(Collectors.toMap(OrganizationCountDTO::getId, count -> count));
        organizations.forEach(organizationDTO -> {
            organizationDTO.setProjectCount(orgCountMap.get(organizationDTO.getId()).getProjectCount());
            organizationDTO.setMemberCount(orgCountMap.get(organizationDTO.getId()).getMemberCount());
            //if (BooleanUtils.isTrue(organizationDTO.getDeleted())) {
            //    organizationDTO.setRemainDayCount(getDeleteRemainDays(organizationDTO.getDeleteTime()));
            //}
        });
    }

    private List<OrganizationCountDTO> getCountByIds(List<String> ids) {
        QueryWrapper wrapper = new QueryWrapper();
        QueryWrapper membersGroupWrapper = new QueryWrapper();
        membersGroupWrapper.select(USER_ROLE_RELATION.SOURCE_ID)
                .select(QueryMethods.count(QueryMethods.distinct(USER.ID)).as("membercount"))
                .from(USER_ROLE_RELATION).join(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.in(ids));
        QueryWrapper projectGroupWrapper = new QueryWrapper();
        projectGroupWrapper.select(PROJECT.ORGANIZATION_ID)
                .select(QueryMethods.count(PROJECT.ID).as("projectcount"))
                .from(PROJECT)
                .where(PROJECT.ORGANIZATION_ID.in(ids))
                .groupBy(PROJECT.ORGANIZATION_ID);
        wrapper.select(ORGANIZATION.ID)
                .select("coalesce(membercount, 0) as memberCount")
                .select("coalesce(projectcount, 0) as projectCount")
                .from(ORGANIZATION.as("o"))
                .leftJoin(new SelectQueryTable(membersGroupWrapper).as("members_group")).on("o.id = members_group.source_id")
                .leftJoin(new SelectQueryTable(projectGroupWrapper).as("projects_group")).on("projects_group.organization_id")
        ;
        return mapper.selectListByQueryAs(wrapper, OrganizationCountDTO.class);
    }

    private List<OrganizationDTO> buildOrgAdminInfo(List<OrganizationDTO> records) {
        records.forEach(dto -> {
            List<User> orgAdminList = getOrgAdminList(dto.getId());
            dto.setOrgAdmins(orgAdminList);
            List<String> userIds = orgAdminList.stream().map(User::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(userIds) && userIds.contains(dto.getCreateUser())) {
                dto.setOrgCreateUserIsAdmin(true);
            }
        });
        return records;
    }

    private List<User> getOrgAdminList(String orgId) {
        return QueryChain.of(userRoleRelationMapper)
                .select(USER.ALL_COLUMNS).from(USER_ROLE_RELATION).join(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.ROLE_ID.eq("org_admin")
                        .and(USER_ROLE_RELATION.SOURCE_ID.eq(orgId)))
                .listAs(User.class);
    }

    private void setLog(String organizationId, String createUser, String type, String content, String path, Object originalValue, Object modifiedValue, List<LogDTO> logs) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                organizationId,
                createUser,
                type,
                OperationLogModule.SETTING_SYSTEM_ORGANIZATION,
                content);
        dto.setPath(path);
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JsonUtils.toJsonBytes(originalValue));
        dto.setModifiedValue(JsonUtils.toJsonBytes(modifiedValue));
        logs.add(dto);
    }

    private void updateUserRoleRelation(String createUserId, String organizationId, User user, List<String> userRoleIds, List<LogDTO> logDTOList) {
        String memberId = user.getId();
        Map<String, UserRole> userRoleMap = checkUseRoleExist(userRoleIds, organizationId);
        List<String> userRoleInDBInOrgIds = userRoleMap.values().stream().map(UserRole::getId).toList();
        //删除旧的关系
        val userRoleRelationQueryChain = QueryChain.of(userRoleRelationMapper)
                .where(USER_ROLE_RELATION.USER_ID.eq(memberId).and(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId)));
        LogicDeleteManager.execWithoutLogicDelete(() -> userRoleRelationMapper.deleteByQuery(userRoleRelationQueryChain));
        userRoleInDBInOrgIds.forEach(userRoleId -> {
            UserRoleRelation userRoleRelation = buildUserRoleRelation(createUserId, memberId, organizationId, userRoleId, organizationId);
            userRoleRelation.setOrganizationId(organizationId);
            userRoleRelationMapper.insert(userRoleRelation);
            //add Log
            String path = "/organization/update-member";
            LogDTO dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    organizationId,
                    memberId,
                    createUserId,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_ORGANIZATION_MEMBER,
                    user.getName());
            setLog(dto, path, logDTOList, userRoleRelation);
        });
    }

    private void setRelationByMemberAndGroupIds(OrganizationMemberExtendRequest request, String createUserId, Map<String, User> userMap, Map<String, UserRole> userRoleMap, boolean add) {
        List<LogDTO> logDTOList = new ArrayList<>();
        String organizationId = request.getOrganizationId();
        userMap.keySet().forEach(memberId -> {
            if (userMap.get(memberId) == null) {
                throw new CustomException("id:" + memberId + Translator.get("user.not.exist"));
            }
            request.getUserRoleIds().forEach(userRoleId -> {
                if (Objects.nonNull(userRoleMap.get(userRoleId))) {
                    val userRoleRelations = QueryChain.of(userRoleRelationMapper).where(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId)
                            .and(USER_ROLE_RELATION.USER_ID.eq(memberId))
                            .and(USER_ROLE_RELATION.ROLE_ID.eq(userRoleId))).list();
                    if (CollectionUtils.isEmpty(userRoleRelations)) {
                        UserRoleRelation userRoleRelation = buildUserRoleRelation(createUserId, memberId, organizationId, userRoleId, organizationId);
                        userRoleRelation.setOrganizationId(organizationId);
                        userRoleRelationMapper.insert(userRoleRelation);
                        //add Log
                        String path = add ? "/organization/add-member" : "/organization/role/update-member";
                        String type = add ? OperationLogType.ADD.name() : OperationLogType.UPDATE.name();
                        LogDTO dto = new LogDTO(
                                OperationLogConstants.ORGANIZATION,
                                organizationId,
                                memberId,
                                createUserId,
                                type,
                                OperationLogModule.SETTING_ORGANIZATION_MEMBER,
                                userMap.get(memberId).getName());
                        setLog(dto, path, logDTOList, userRoleRelation);
                    }
                }
            });
        });
    }

    private static void setLog(LogDTO dto, String path, List<LogDTO> logDTOList, Object originalValue) {
        dto.setPath(path);
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JsonUtils.toJsonBytes(originalValue));
        logDTOList.add(dto);
    }

    private UserRoleRelation buildUserRoleRelation(String createUserId, String memberId, String sourceId, String roleId, String organizationId) {
        UserRoleRelation userRoleRelation = new UserRoleRelation();
        userRoleRelation.setUserId(memberId);
        userRoleRelation.setOrganizationId(organizationId);
        userRoleRelation.setSourceId(sourceId);
        userRoleRelation.setRoleId(roleId);
        userRoleRelation.setCreateUser(createUserId);
        return userRoleRelation;
    }

    private Map<String, UserRole> checkUseRoleExist(List<String> userRoleIds, String organizationId) {
        List<String> scopeIds = Arrays.asList(UserRoleEnum.GLOBAL.toString(), organizationId);
        val userRoles = QueryChain.of(UserRole.class)
                .where(USER_ROLE.ID.in(userRoleIds)
                        .and(USER_ROLE.TYPE.eq(UserRoleType.ORGANIZATION.toString()))
                        .and(USER_ROLE.SCOPE_ID.in(scopeIds)))
                .list();
        if (CollectionUtils.isEmpty(userRoles)) {
            throw new CustomException(Translator.get("user_role_not_exist"));
        }
        return userRoles.stream().collect(Collectors.toMap(UserRole::getId, user -> user));
    }

    private Map<String, User> getUserMap(OrganizationMemberExtendRequest request) {
        Map<String, User> userMap;
        if (request.isSelectAll()) {
            OrganizationRequest organizationRequest = new OrganizationRequest();
            BeanUtils.copyProperties(request, organizationRequest);
            List<OrgUserExtend> orgUserExtends = listMemberByOrg(organizationRequest).getRecords();
            List<String> excludeIds = request.getExcludeIds();
            if (CollectionUtils.isNotEmpty(excludeIds)) {
                userMap = orgUserExtends.stream().filter(user -> !excludeIds.contains(user.getId())).collect(Collectors.toMap(User::getId, user -> user));
            } else {
                userMap = orgUserExtends.stream().collect(Collectors.toMap(User::getId, user -> user));
            }
        } else {
            userMap = checkUserExist(request.getMemberIds());
        }
        return userMap;
    }

    private void checkOrgExistById(String organizationId) {
        Organization organization = mapper.selectOneById(organizationId);
        if (organization == null) {
            throw new CustomException(Translator.get("organization_not_exist"));
        }
    }

    private Page<OrgUserExtend> listMemberByOrg(OrganizationRequest request) {
        QueryWrapper query = new QueryWrapper();
        QueryWrapper subQuery = new QueryWrapper();
        subQuery.select(USER.ALL_COLUMNS, USER_ROLE_RELATION.ROLE_ID, USER_ROLE_RELATION.CREATE_TIME.as("memberTime"))
                .from(USER_ROLE_RELATION).join(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getOrganizationId())
                        .and(USER.NAME.like(request.getKeyword())
                                .or(USER.EMAIL.like(request.getKeyword()))
                                .or(USER.PHONE.like(request.getKeyword())))
                ).orderBy(USER.UPDATE_TIME.desc());
        query.select("temp.*", " min(temp.memberTime) as groupTime")
                .from(subQuery.as("temp"))
                .groupBy("temp.id")
                .orderBy("groupTime");
        return userRoleRelationMapper.paginateAs(Page.of(request.getPageNum(), request.getPageSize()), query, OrgUserExtend.class);
    }

    private static void setUserRoleList(List<OptionDTO> userRoleList, List<UserRole> userRoles) {
        for (UserRole userRole : userRoles) {
            OptionDTO optionDTO = new OptionDTO();
            optionDTO.setId(userRole.getId());
            optionDTO.setName(userRole.getName());
            userRoleList.add(optionDTO);
        }
    }

    private void setProjectList(List<OptionDTO> projectList, List<Project> projectFilters) {
        for (Project project : projectFilters) {
            OptionDTO optionDTO = new OptionDTO();
            optionDTO.setId(project.getId());
            optionDTO.setName(project.getName());
            projectList.add(optionDTO);
        }
    }

    private void getTargetIds(Map<String, Set<String>> userIdTargetIdMap, Set<String> targetIdSet, String sourceId, String userId) {
        Set<String> targetIds = userIdTargetIdMap.get(userId);
        if (CollectionUtils.isEmpty(targetIds)) {
            targetIds = new HashSet<>();
        }
        targetIds.add(sourceId);
        targetIdSet.add(sourceId);
        userIdTargetIdMap.put(userId, targetIds);
    }

    private Map<String, User> checkUserExist(List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            throw new CustomException(Translator.get("user.not.empty"));
        }
        val users = QueryChain.of(User.class).where(User::getId).in(userIds).list();
        if (CollectionUtils.isEmpty(users)) {
            throw new CustomException(Translator.get("user.not.exist"));
        }
        return users.stream().collect(Collectors.toMap(User::getId, user -> user));
    }

    private void checkOrgExistByIds(List<String> organizationIds) {
        val count = queryChain().where(Organization::getId).in(organizationIds).count();
        if (count != organizationIds.size()) {
            throw new CustomException(Translator.get("organization_not_exist"));
        }
    }
}
