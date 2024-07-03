package cn.master.matrix.service.impl;

import cn.master.matrix.entity.User;
import cn.master.matrix.entity.UserRole;
import cn.master.matrix.entity.UserRoleRelation;
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
                        .or(USER.EMAIL.eq(request.getKeyword()))
                        .or(USER.NAME.eq(request.getKeyword()))
                        .or(USER.PHONE.eq(request.getKeyword())))
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
