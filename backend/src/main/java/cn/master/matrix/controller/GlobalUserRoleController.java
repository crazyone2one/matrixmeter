package cn.master.matrix.controller;

import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.constants.PermissionConstants;
import cn.master.matrix.entity.UserRole;
import cn.master.matrix.handler.annotation.HasAuthorize;
import cn.master.matrix.handler.annotation.Log;
import cn.master.matrix.handler.validation.Created;
import cn.master.matrix.handler.validation.Updated;
import cn.master.matrix.payload.dto.permission.PermissionDefinitionItem;
import cn.master.matrix.payload.dto.request.PermissionSettingUpdateRequest;
import cn.master.matrix.payload.dto.request.UserRoleUpdateRequest;
import cn.master.matrix.service.GlobalUserRoleService;
import cn.master.matrix.service.log.GlobalUserRoleLogService;
import cn.master.matrix.util.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户组 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-27T16:19:57.911393700
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "系统设置-系统-用户组")
@RequestMapping("/user/role/global")
public class GlobalUserRoleController {

    private final GlobalUserRoleService globalUserRoleService;

    @PostMapping("/add")
    @Operation(summary = "系统设置-系统-用户组-添加自定义全局用户组")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ROLE_ADD)
    @Log(type = OperationLogType.ADD, expression = "#mmClass.addLog(#request)", mmClass = GlobalUserRoleLogService.class)
    public UserRole save(@Validated({Created.class}) @RequestBody UserRoleUpdateRequest request) {
        UserRole userRole = new UserRole();
        userRole.setCreateUser(SessionUtils.getUserId());
        BeanUtils.copyProperties(request, userRole);
        return globalUserRoleService.add(userRole);
    }
    @PostMapping("/update")
    @Operation(summary = "系统设置-系统-用户组-更新自定义全局用户组")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ROLE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.updateLog(#request)", mmClass = GlobalUserRoleLogService.class)
    public UserRole update(@Validated({Updated.class}) @RequestBody UserRoleUpdateRequest request) {
        UserRole userRole = new UserRole();
        BeanUtils.copyProperties(request, userRole);
        return globalUserRoleService.update(userRole);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "系统设置-系统-用户组-删除自定义全局用户组")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ROLE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#mmClass.deleteLog(#id)", mmClass = GlobalUserRoleLogService.class)
    public void delete(@PathVariable String id) {
        globalUserRoleService.delete(id, SessionUtils.getUserId());
    }

    @PostMapping("/permission/update")
    @Operation(summary = "系统设置-系统-用户组-编辑全局用户组对应的权限配置")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ROLE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.updateLog(#request)", mmClass = GlobalUserRoleLogService.class)
    public void updatePermissionSetting(@Validated @RequestBody PermissionSettingUpdateRequest request) {
        globalUserRoleService.updatePermissionSetting(request);
    }

    /**
     * 查询所有用户组。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(summary = "系统设置-系统-用户组-获取全局用户组列表")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ROLE_READ)
    public List<UserRole> list() {
        return globalUserRoleService.list();
    }


    @GetMapping("/permission/setting/{id}")
    @Operation(summary = "系统设置-系统-用户组-获取全局用户组对应的权限配置")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ROLE_READ)
    public List<PermissionDefinitionItem> getPermissionSetting(@PathVariable String id) {
        return globalUserRoleService.getPermissionSetting(id);
    }
}
