package cn.master.matrix.controller;

import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.constants.PermissionConstants;
import cn.master.matrix.constants.UserSource;
import cn.master.matrix.entity.Organization;
import cn.master.matrix.entity.Project;
import cn.master.matrix.handler.annotation.HasAnyAuthorize;
import cn.master.matrix.handler.annotation.HasAuthorize;
import cn.master.matrix.handler.annotation.Log;
import cn.master.matrix.handler.validation.Created;
import cn.master.matrix.handler.validation.Updated;
import cn.master.matrix.payload.dto.BaseTreeNode;
import cn.master.matrix.payload.dto.OptionDTO;
import cn.master.matrix.payload.dto.TableBatchProcessDTO;
import cn.master.matrix.payload.dto.request.BasePageRequest;
import cn.master.matrix.payload.dto.request.OrganizationMemberBatchRequest;
import cn.master.matrix.payload.dto.request.ProjectAddMemberBatchRequest;
import cn.master.matrix.payload.dto.request.user.UserChangeEnableRequest;
import cn.master.matrix.payload.dto.request.user.UserCreateRequest;
import cn.master.matrix.payload.dto.request.user.UserEditRequest;
import cn.master.matrix.payload.dto.request.user.UserRoleBatchRelationRequest;
import cn.master.matrix.payload.dto.user.UserDTO;
import cn.master.matrix.payload.dto.user.UserTableResponse;
import cn.master.matrix.payload.dto.user.response.UserBatchCreateResponse;
import cn.master.matrix.payload.dto.user.response.UserSelectOption;
import cn.master.matrix.payload.response.TableBatchProcessResponse;
import cn.master.matrix.service.*;
import cn.master.matrix.service.log.UserLogService;
import cn.master.matrix.util.SessionUtils;
import cn.master.matrix.util.TreeNodeParseUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-21T10:54:08.016115500
 */
@RestController
@Tag(name = "系统设置-系统-用户")
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final GlobalUserRoleService globalUserRoleService;
    private final OrganizationService organizationService;
    private final GlobalUserRoleRelationService globalUserRoleRelationService;
    private final UserLogService userLogService;
    private final SystemProjectService systemProjectService;
    private final UserToolService userToolService;

    @PostMapping("/add")
    @Operation(summary = "系统设置-系统-用户-添加用户")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ADD)
    public UserBatchCreateResponse save(@Validated({Created.class}) @RequestBody UserCreateRequest userCreateDTO) {
        return userService.save(userCreateDTO, UserSource.LOCAL.name(), SessionUtils.getUserId());
    }

    @PostMapping("/delete")
    @HasAuthorize("SYSTEM_USER:READ+DELETE")
    @Operation(summary = "系统设置-系统-用户-删除用户")
    @Log(type = OperationLogType.DELETE, expression = "#mmClass.deleteLog(#request)", mmClass = UserLogService.class)
    public TableBatchProcessResponse remove(@Validated @RequestBody TableBatchProcessDTO request) {
        return userService.deleteUser(request, SessionUtils.getUserId(), SessionUtils.getUser().getUsername());
    }

    @PostMapping("/update")
    @Operation(summary = "系统设置-系统-用户-修改用户")
    @HasAuthorize("SYSTEM_USER:READ+UPDATE")
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.updateLog(#request)", mmClass = UserLogService.class)
    public UserEditRequest update(@Validated({Updated.class}) @RequestBody UserEditRequest request) {
        return userService.updateUser(request, SessionUtils.getUserId());
    }

    @GetMapping("/get/global/system/role")
    @Operation(summary = "系统设置-系统-用户-查找系统级用户组")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ROLE_READ)
    public List<UserSelectOption> getGlobalSystemRole() {
        return globalUserRoleService.getGlobalSystemRoleList();
    }


    @GetMapping("getInfo/{keyword}")
    @HasAuthorize("SYSTEM_USER:READ")
    @Operation(summary = "通过email或id查找用户")
    public UserDTO getInfo(@PathVariable String keyword) {
        return userService.getUserByKeyword(keyword);
    }

    @PostMapping("/page")
    @HasAuthorize("SYSTEM_USER:READ")
    @Operation(summary = "系统设置-系统-用户-分页查找用户")
    public Page<UserTableResponse> page(@Validated @RequestBody BasePageRequest request) {
        return userService.page(request);
    }

    @GetMapping("/get/organization")
    @Operation(summary = "系统设置-系统-用户-用户批量操作-查找组织")
    @HasAnyAuthorize(authorities = {PermissionConstants.SYSTEM_USER_ROLE_READ, PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ})
    public List<OptionDTO> getOrganization() {
        return organizationService.listAll();
    }

    @GetMapping("/get/project")
    @Operation(summary = "系统设置-系统-用户-用户批量操作-查找项目")
    @HasAnyAuthorize(authorities = {PermissionConstants.SYSTEM_USER_ROLE_READ, PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ})
    public List<BaseTreeNode> getProject() {
        Map<Organization, List<Project>> orgProjectMap = organizationService.getOrgProjectMap();
        return TreeNodeParseUtils.parseOrgProjectMap(orgProjectMap);
    }

    @PostMapping("/add/batch/user-role")
    @Operation(summary = "系统设置-系统-用户-批量添加用户到多个用户组中")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_UPDATE)
    public TableBatchProcessResponse batchAddUserGroupRole(@Validated({Created.class}) @RequestBody UserRoleBatchRelationRequest request) {
        TableBatchProcessResponse returnResponse = globalUserRoleRelationService.batchAdd(request, SessionUtils.getUserId());
        userLogService.batchAddUserRoleLog(request, SessionUtils.getUserId());
        return returnResponse;
    }

    @PostMapping("/add-project-member")
    @Operation(summary = "系统设置-系统-用户-批量添加用户到项目")
    @HasAnyAuthorize(authorities = {PermissionConstants.SYSTEM_USER_UPDATE, PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_ADD})
    public TableBatchProcessResponse addProjectMember(@Validated @RequestBody UserRoleBatchRelationRequest userRoleBatchRelationRequest) {
        ProjectAddMemberBatchRequest request = new ProjectAddMemberBatchRequest();
        request.setProjectIds(userRoleBatchRelationRequest.getRoleIds());
        request.setUserIds(userRoleBatchRelationRequest.getSelectIds());
        systemProjectService.addProjectMember(request, SessionUtils.getUserId());
        userLogService.batchAddProjectLog(userRoleBatchRelationRequest, SessionUtils.getUserId());
        return new TableBatchProcessResponse(userRoleBatchRelationRequest.getSelectIds().size(), userRoleBatchRelationRequest.getSelectIds().size());
    }

    @PostMapping("/add-org-member")
    @Operation(summary = "系统设置-系统-用户-批量添加用户到组织")
    @HasAnyAuthorize(authorities = {PermissionConstants.SYSTEM_USER_UPDATE, PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_ADD})
    public TableBatchProcessResponse addMember(@Validated @RequestBody UserRoleBatchRelationRequest userRoleBatchRelationRequest) {
        //获取本次处理的用户
        userRoleBatchRelationRequest.setSelectIds(userToolService.getBatchUserIds(userRoleBatchRelationRequest));
        OrganizationMemberBatchRequest request = new OrganizationMemberBatchRequest();
        request.setOrganizationIds(userRoleBatchRelationRequest.getRoleIds());
        request.setUserIds(userRoleBatchRelationRequest.getSelectIds());
        organizationService.addMemberBySystem(request, SessionUtils.getUserId());
        userLogService.batchAddOrgLog(userRoleBatchRelationRequest, SessionUtils.getUserId());
        return new TableBatchProcessResponse(userRoleBatchRelationRequest.getSelectIds().size(), userRoleBatchRelationRequest.getSelectIds().size());
    }

    @PostMapping("/update/enable")
    @Operation(summary = "系统设置-系统-用户-启用/禁用用户")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.batchUpdateEnableLog(#request)", mmClass = UserLogService.class)
    public TableBatchProcessResponse updateUserEnable(@Validated @RequestBody UserChangeEnableRequest request) {
        return userService.updateUserEnable(request, SessionUtils.getUserId(), SessionUtils.getUser().getUsername());
    }

    @PostMapping("/reset/password")
    @Operation(summary = "系统设置-系统-用户-重置用户密码")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.resetPasswordLog(#request)", mmClass = UserLogService.class)
    public TableBatchProcessResponse resetPassword(@Validated @RequestBody TableBatchProcessDTO request) {
        return userService.resetPassword(request, SessionUtils.getUserId());
    }
}
