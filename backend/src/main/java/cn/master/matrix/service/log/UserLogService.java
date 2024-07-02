package cn.master.matrix.service.log;

import cn.master.matrix.constants.HttpMethodConstants;
import cn.master.matrix.constants.OperationLogConstants;
import cn.master.matrix.constants.OperationLogModule;
import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.entity.Organization;
import cn.master.matrix.entity.Project;
import cn.master.matrix.entity.User;
import cn.master.matrix.entity.UserRole;
import cn.master.matrix.mapper.UserMapper;
import cn.master.matrix.payload.LogDTOBuilder;
import cn.master.matrix.payload.dto.LogDTO;
import cn.master.matrix.payload.dto.TableBatchProcessDTO;
import cn.master.matrix.payload.dto.request.user.UserChangeEnableRequest;
import cn.master.matrix.payload.dto.request.user.UserEditRequest;
import cn.master.matrix.payload.dto.request.user.UserRoleBatchRelationRequest;
import cn.master.matrix.payload.dto.user.UserCreateInfo;
import cn.master.matrix.service.OperationLogService;
import cn.master.matrix.service.UserToolService;
import cn.master.matrix.util.JsonUtils;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.query.QueryChain;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Created by 11's papa on 06/28/2024
 **/
@Service
@RequiredArgsConstructor
public class UserLogService {
    private final UserMapper userMapper;
    private final UserToolService userToolService;
    private final OperationLogService operationLogService;

    public LogDTO updateLog(UserEditRequest request) {
        User user = userMapper.selectOneById(request.getId());
        if (user != null) {
            return LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .type(OperationLogType.UPDATE.name())
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .method(HttpMethodConstants.POST.name())
                    .path("/system/user/update")
                    .sourceId(request.getId())
                    .content(user.getName())
                    .originalValue(JsonUtils.toJsonBytes(user))
                    .build().getLogDTO();
        }
        return null;
    }

    public List<LogDTO> batchUpdateEnableLog(UserChangeEnableRequest request) {
        List<LogDTO> logDTOList = new ArrayList<>();
        request.setSelectIds(userToolService.getBatchUserIds(request));
        List<User> userList = userToolService.selectByIdList(request.getSelectIds());
        for (User user : userList) {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .type(OperationLogType.UPDATE.name())
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .method(HttpMethodConstants.POST.name())
                    .path("/system/user/update/enable")
                    .sourceId(user.getId())
                    .content((request.isEnable() ? Translator.get("user.enable") : Translator.get("user.disable")) + ":" + user.getName())
                    .originalValue(JsonUtils.toJsonBytes(user))
                    .build().getLogDTO();
            logDTOList.add(dto);
        }
        return logDTOList;
    }

    public List<LogDTO> deleteLog(TableBatchProcessDTO request) {
        List<LogDTO> logDTOList = new ArrayList<>();
        request.getSelectIds().forEach(item -> {
            User user = userMapper.selectOneById(item);
            if (user != null) {
                LogDTO dto = LogDTOBuilder.builder()
                        .projectId(OperationLogConstants.SYSTEM)
                        .organizationId(OperationLogConstants.SYSTEM)
                        .type(OperationLogType.DELETE.name())
                        .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                        .method(HttpMethodConstants.POST.name())
                        .path("/system/user/delete")
                        .sourceId(user.getId())
                        .content(Translator.get("user.delete") + " : " + user.getName())
                        .originalValue(JsonUtils.toJsonBytes(user))
                        .build().getLogDTO();
                logDTOList.add(dto);

            }
        });
        return logDTOList;
    }

    public List<LogDTO> resetPasswordLog(TableBatchProcessDTO request) {
        request.setSelectIds(userToolService.getBatchUserIds(request));
        List<LogDTO> returnList = new ArrayList<>();
        List<User> userList = QueryChain.of(User.class).where(User::getId).in(request.getSelectIds()).list();
        for (User user : userList) {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .type(OperationLogType.UPDATE.name())
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .method(HttpMethodConstants.POST.name())
                    .path("/system/user/reset/password")
                    .sourceId(user.getId())
                    .content(Translator.get("user.reset.password") + " : " + user.getName())
                    .originalValue(JsonUtils.toJsonBytes(user))
                    .build().getLogDTO();
            returnList.add(dto);
        }
        return returnList;
    }

    public void batchAddUserRoleLog(UserRoleBatchRelationRequest request, String operator) {
        List<LogDTO> logs = new ArrayList<>();
        List<String> userIds = userToolService.getBatchUserIds(request);
        List<User> userList = userToolService.selectByIdList(userIds);

        List<String> roleNameList = QueryChain.of(UserRole.class).where(UserRole::getId).in(request.getRoleIds()).list()
                .stream().map(UserRole::getName).collect(Collectors.toList());
        String roleNames = StringUtils.join(roleNameList, ",");

        for (User user : userList) {
            //用户管理处修改了用户的组织。
            LogDTO log = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .createUser(operator)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .sourceId(user.getId())
                    .type(OperationLogType.UPDATE.name())
                    .content(user.getName() + Translator.get("user.add.group") + ":" + roleNames)
                    .path("/system/user/add/batch/user-role")
                    .method(HttpMethodConstants.POST.name())
                    .modifiedValue(JsonUtils.toJsonBytes(request.getRoleIds()))
                    .build().getLogDTO();
            logs.add(log);
        }
        operationLogService.batchAdd(logs);
    }

    public void batchAddProjectLog(UserRoleBatchRelationRequest request, String operator) {
        List<LogDTO> logs = new ArrayList<>();
        List<String> userIds = userToolService.getBatchUserIds(request);
        List<User> userList = userToolService.selectByIdList(userIds);
        List<String> projectNameList = QueryChain.of(Project.class).where(Project::getId).in(request.getRoleIds()).list()
                .stream().map(Project::getName).collect(Collectors.toList());
        String projectNames = StringUtils.join(projectNameList, ",");
        for (User user : userList) {
            //用户管理处修改了用户的组织。
            LogDTO log = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .createUser(operator)
                    .method(HttpMethodConstants.POST.name())
                    .organizationId(OperationLogConstants.SYSTEM)
                    .sourceId(user.getId())
                    .type(OperationLogType.UPDATE.name())
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .content(user.getName() + Translator.get("user.add.project") + ":" + projectNames)
                    .path("/system/user/add-project-member")
                    .modifiedValue(JsonUtils.toJsonBytes(request.getRoleIds()))
                    .build().getLogDTO();
            logs.add(log);
        }
        operationLogService.batchAdd(logs);
    }

    public void batchAddOrgLog(UserRoleBatchRelationRequest request, String operator) {
        List<LogDTO> logs = new ArrayList<>();
        List<String> userIds = userToolService.getBatchUserIds(request);
        List<User> userList = userToolService.selectByIdList(userIds);
        List<String> roleNameList = QueryChain.of(Organization.class).where(Organization::getId).in(request.getRoleIds()).list()
                .stream().map(Organization::getName).collect(Collectors.toList());
        String roleNames = StringUtils.join(roleNameList, ",");

        for (User user : userList) {
            //用户管理处修改了用户的组织。
            LogDTO log = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .createUser(operator)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .sourceId(user.getId())
                    .type(OperationLogType.UPDATE.name())
                    .content(user.getName() + Translator.get("user.add.org") + ":" + roleNames)
                    .path("/system/user/add-org-member")
                    .method(HttpMethodConstants.POST.name())
                    .modifiedValue(JsonUtils.toJsonBytes(request.getRoleIds()))
                    .build().getLogDTO();
            logs.add(log);
        }
        operationLogService.batchAdd(logs);
    }

    public List<LogDTO> getBatchAddLogs(List<UserCreateInfo> userList, String operator, String requestPath) {
        List<LogDTO> logs = new ArrayList<>();
        userList.forEach(user -> {
            LogDTO log = LogDTOBuilder.builder()
                    .projectId(OperationLogConstants.SYSTEM)
                    .organizationId(OperationLogConstants.SYSTEM)
                    .type(OperationLogType.ADD.name())
                    .module(OperationLogModule.SETTING_SYSTEM_USER_SINGLE)
                    .method(HttpMethodConstants.POST.name())
                    .path(requestPath)
                    .sourceId(user.getId())
                    .content(user.getName() + "(" + user.getEmail() + ")")
                    .originalValue(JsonUtils.toJsonBytes(user))
                    .createUser(operator)
                    .build().getLogDTO();
            logs.add(log);
        });
        return logs;
    }
}
