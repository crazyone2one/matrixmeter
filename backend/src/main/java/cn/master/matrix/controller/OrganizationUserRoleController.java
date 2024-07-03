package cn.master.matrix.controller;

import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.constants.PermissionConstants;
import cn.master.matrix.entity.User;
import cn.master.matrix.entity.UserRole;
import cn.master.matrix.handler.annotation.HasAuthorize;
import cn.master.matrix.handler.annotation.Log;
import cn.master.matrix.handler.validation.Created;
import cn.master.matrix.handler.validation.Updated;
import cn.master.matrix.payload.dto.permission.PermissionDefinitionItem;
import cn.master.matrix.payload.dto.request.OrganizationUserRoleEditRequest;
import cn.master.matrix.payload.dto.request.OrganizationUserRoleMemberEditRequest;
import cn.master.matrix.payload.dto.request.OrganizationUserRoleMemberRequest;
import cn.master.matrix.payload.dto.request.PermissionSettingUpdateRequest;
import cn.master.matrix.payload.dto.user.UserExtendDTO;
import cn.master.matrix.service.OrganizationUserRoleService;
import cn.master.matrix.service.log.OrganizationUserRoleLogService;
import cn.master.matrix.util.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Created by 11's papa on 07/03/2024
 **/
@RestController
@RequiredArgsConstructor
@Tag(name = "系统设置-组织-用户组")
@RequestMapping("/user/role/organization")
public class OrganizationUserRoleController {
    private final OrganizationUserRoleService organizationUserRoleService;

    @PostMapping("/add")
    @Operation(summary = "系统设置-组织-用户组-添加用户组")
    @HasAuthorize(PermissionConstants.ORGANIZATION_USER_ROLE_READ_ADD)
    @Log(type = OperationLogType.ADD, expression = "#mmClass.addLog(#request)", mmClass = OrganizationUserRoleLogService.class)
    public UserRole add(@Validated({Created.class}) @RequestBody OrganizationUserRoleEditRequest request) {
        UserRole userRole = new UserRole();
        userRole.setCreateUser(SessionUtils.getUserId());
        BeanUtils.copyProperties(request, userRole);
        return organizationUserRoleService.add(userRole);
    }

    @PostMapping("/update")
    @Operation(summary = "系统设置-组织-用户组-修改用户组")
    @HasAuthorize(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.updateLog(#request)", mmClass = OrganizationUserRoleLogService.class)
    public UserRole update(@Validated({Updated.class}) @RequestBody OrganizationUserRoleEditRequest request) {
        UserRole userRole = new UserRole();
        BeanUtils.copyProperties(request, userRole);
        return organizationUserRoleService.update(userRole);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "系统设置-组织-用户组-删除用户组")
    @HasAuthorize(PermissionConstants.ORGANIZATION_USER_ROLE_READ_DELETE)
    @Parameter(name = "id", description = "用户组ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Log(type = OperationLogType.DELETE, expression = "#mmClass.deleteLog(#id)", mmClass = OrganizationUserRoleLogService.class)
    public void delete(@PathVariable String id) {
        organizationUserRoleService.delete(id, SessionUtils.getUserId());
    }

    @GetMapping("/list/{organizationId}")
    @Operation(summary = "系统设置-组织-用户组-获取用户组列表")
    @Parameter(name = "organizationId", description = "当前组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(PermissionConstants.ORGANIZATION_USER_ROLE_READ)
    public List<UserRole> list(@PathVariable String organizationId) {
        return organizationUserRoleService.list(organizationId);
    }

    @GetMapping("/permission/setting/{id}")
    @Operation(summary = "系统设置-组织-用户组-获取用户组对应的权限配置")
    @Parameter(name = "id", description = "用户组ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(PermissionConstants.ORGANIZATION_USER_ROLE_READ)
    public List<PermissionDefinitionItem> getPermissionSetting(@PathVariable String id) {
        return organizationUserRoleService.getPermissionSetting(id);
    }

    @PostMapping("/permission/update")
    @Operation(summary = "系统设置-组织-用户组-修改用户组对应的权限配置")
    @HasAuthorize(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.updatePermissionSettingLog(#request)", mmClass = OrganizationUserRoleLogService.class)
    public void updatePermissionSetting(@Validated @RequestBody PermissionSettingUpdateRequest request) {
        organizationUserRoleService.updatePermissionSetting(request);
    }

    @GetMapping("/get-member/option/{organizationId}/{roleId}")
    @Operation(summary = "系统设置-组织-用户组-获取成员下拉选项")
    @HasAuthorize(PermissionConstants.ORGANIZATION_USER_ROLE_READ)
    @Parameters({
            @Parameter(name = "organizationId", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)),
            @Parameter(name = "roleId", description = "用户组ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    })
    public List<UserExtendDTO> getMember(@PathVariable String organizationId,
                                         @PathVariable String roleId,
                                         @Schema(description = "查询关键字，根据邮箱和用户名查询")
                                         @RequestParam(required = false) String keyword) {
        return organizationUserRoleService.getMember(organizationId, roleId, keyword);
    }

    @PostMapping("/list-member")
    @Operation(summary = "系统设置-组织-用户组-获取成员列表")
    @HasAuthorize(PermissionConstants.ORGANIZATION_USER_ROLE_READ)
    public Page<User> listMember(@Validated @RequestBody OrganizationUserRoleMemberRequest request) {
        return organizationUserRoleService.listMember(request);
    }

    @PostMapping("/add-member")
    @Operation(summary = "系统设置-组织-用户组-添加用户组成员")
    @HasAuthorize(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.editMemberLog(#request)", mmClass = OrganizationUserRoleLogService.class)
    public void addMember(@Validated @RequestBody OrganizationUserRoleMemberEditRequest request) {
        organizationUserRoleService.addMember(request, SessionUtils.getUserId());
    }

    @PostMapping("/remove-member")
    @Operation(summary = "系统设置-组织-用户组-删除用户组成员")
    @HasAuthorize(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.editMemberLog(#request)", mmClass = OrganizationUserRoleLogService.class)
    public void removeMember(@Validated @RequestBody OrganizationUserRoleMemberEditRequest request) {
        organizationUserRoleService.removeMember(request);
    }
}
