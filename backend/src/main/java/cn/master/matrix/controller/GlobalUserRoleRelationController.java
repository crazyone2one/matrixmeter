package cn.master.matrix.controller;

import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.constants.PermissionConstants;
import cn.master.matrix.handler.annotation.HasAuthorize;
import cn.master.matrix.handler.annotation.Log;
import cn.master.matrix.handler.validation.Created;
import cn.master.matrix.payload.dto.request.GlobalUserRoleRelationQueryRequest;
import cn.master.matrix.payload.dto.request.GlobalUserRoleRelationUpdateRequest;
import cn.master.matrix.payload.dto.request.user.UserExcludeOptionDTO;
import cn.master.matrix.payload.dto.user.UserRoleRelationUserDTO;
import cn.master.matrix.service.GlobalUserRoleRelationService;
import cn.master.matrix.service.log.GlobalUserRoleRelationLogService;
import cn.master.matrix.util.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户组关系 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-27T16:20:34.166413800
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "系统设置-系统-用户组-用户关联关系")
@RequestMapping("/user/role/relation/global")
public class GlobalUserRoleRelationController {

    private final GlobalUserRoleRelationService globalUserRoleRelationService;

    @PostMapping("/add")
    @Operation(summary = "系统设置-系统-用户组-用户关联关系-创建全局用户组和用户的关联关系")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ROLE_UPDATE)
    @Log(type = OperationLogType.ADD, expression = "#mmClass.addLog(#request)", mmClass = GlobalUserRoleRelationLogService.class)
    public void add(@Validated({Created.class}) @RequestBody GlobalUserRoleRelationUpdateRequest request) {
        request.setCreateUser(SessionUtils.getUserId());
        globalUserRoleRelationService.add(request);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "系统设置-系统-用户组-用户关联关系-删除全局用户组和用户的关联关系")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ROLE_UPDATE)
    @Log(type = OperationLogType.DELETE, expression = "#mmClass.deleteLog(#id)", mmClass = GlobalUserRoleRelationLogService.class)
    public void remove(@PathVariable @Parameter(description = "用户组关系主键") String id) {
        globalUserRoleRelationService.delete(id);
    }


    @PostMapping("/page")
    @Operation(summary = "系统设置-系统-用户组-用户关联关系-获取全局用户组对应的用户列表")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ROLE_READ)
    public Page<UserRoleRelationUserDTO> page(@Validated @RequestBody GlobalUserRoleRelationQueryRequest request) {
        return globalUserRoleRelationService.page(request);
    }
    @GetMapping("/user/option/{roleId}")
    @Operation(summary = "系统设置-系统-用户组-用户关联关系-获取需要关联的用户选项")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ROLE_READ)
    public List<UserExcludeOptionDTO> getSelectOption(@Schema(description = "用户组ID", requiredMode = Schema.RequiredMode.REQUIRED)
                                                      @PathVariable String roleId,
                                                      @Schema(description = "查询关键字，根据邮箱和用户名查询", requiredMode = Schema.RequiredMode.REQUIRED)
                                                      @RequestParam(value = "keyword", required = false) String keyword) {
        return globalUserRoleRelationService.getExcludeSelectOption(roleId, keyword);
    }
}
