package cn.master.matrix.controller;

import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.constants.PermissionConstants;
import cn.master.matrix.entity.User;
import cn.master.matrix.handler.annotation.HasAuthorize;
import cn.master.matrix.handler.annotation.Log;
import cn.master.matrix.handler.validation.Created;
import cn.master.matrix.handler.validation.Updated;
import cn.master.matrix.payload.dto.OptionDTO;
import cn.master.matrix.payload.dto.ProjectDTO;
import cn.master.matrix.payload.dto.request.*;
import cn.master.matrix.payload.dto.user.UserExtendDTO;
import cn.master.matrix.service.SystemProjectService;
import cn.master.matrix.service.UserService;
import cn.master.matrix.service.log.SystemProjectLogService;
import cn.master.matrix.util.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Created by 11's papa on 07/03/2024
 **/
@RestController
@RequiredArgsConstructor
@Tag(name = "系统设置-系统-组织与项目-项目")
@RequestMapping("/system/project")
public class SystemProjectController {
    private final SystemProjectService systemProjectService;
    private final UserService userService;

    @PostMapping("/add")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_ADD)
    @Log(type = OperationLogType.ADD, expression = "#mmClass.addLog(#request)", mmClass = SystemProjectLogService.class)
    @Operation(summary = "系统设置-系统-组织与项目-项目-创建项目")
    public ProjectDTO addProject(@RequestBody @Validated({Created.class}) AddProjectRequest request) {
        return systemProjectService.add(request, SessionUtils.getUserId());
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-项目-根据ID获取项目信息")
    @Parameter(name = "id", description = "项目id", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public ProjectDTO getProject(@PathVariable @NotBlank String id) {
        return systemProjectService.get(id);
    }

    @PostMapping("/page")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    @Operation(summary = "系统设置-系统-组织与项目-项目-获取项目列表")
    public Page<ProjectDTO> getProjectList(@Validated @RequestBody ProjectRequest request) {
        return systemProjectService.getProjectPage(request);
    }

    @PostMapping("/update")
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.updateLog(#request)", mmClass = SystemProjectLogService.class)
    @Operation(summary = "系统设置-系统-组织与项目-项目-编辑")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    public ProjectDTO updateProject(@RequestBody @Validated({Updated.class}) UpdateProjectRequest request) {
        return systemProjectService.update(request, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{id}")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_DELETE)
    @Operation(summary = "系统设置-系统-组织与项目-项目-删除")
    @Parameter(name = "id", description = "项目", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Log(type = OperationLogType.DELETE, expression = "#mmClass.deleteLog(#id)", mmClass = SystemProjectLogService.class)
    public boolean deleteProject(@PathVariable String id) {
        return systemProjectService.delete(id, SessionUtils.getUserId());
    }

    @GetMapping("/enable/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-项目-启用")
    @Parameter(name = "id", description = "项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.updateLog(#id)", mmClass = SystemProjectLogService.class)
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    public void enable(@PathVariable String id) {
        systemProjectService.enable(id, SessionUtils.getUserId());
    }

    @GetMapping("/disable/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-项目-禁用")
    @Parameter(name = "id", description = "项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#id)", mmClass = SystemProjectLogService.class)
    public void disable(@PathVariable String id) {
        systemProjectService.disable(id, SessionUtils.getUserId());
    }

    @PostMapping("/member-list")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    @Operation(summary = "系统设置-系统-组织与项目-项目-成员列表")
    public Page<UserExtendDTO> getProjectMember(@Validated @RequestBody ProjectMemberRequest request) {
        return systemProjectService.getProjectMember(request);
    }

    @PostMapping("/add-member")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_ADD)
    @Operation(summary = "系统设置-系统-组织与项目-项目-添加成员")
    public void addProjectMember(@Validated @RequestBody ProjectAddMemberRequest request) {
        ProjectAddMemberBatchRequest batchRequest = new ProjectAddMemberBatchRequest();
        batchRequest.setProjectIds(List.of(request.getProjectId()));
        batchRequest.setUserIds(request.getUserIds());
        systemProjectService.addProjectMember(batchRequest, SessionUtils.getUserId());
    }

    @GetMapping("/remove-member/{projectId}/{userId}")
    @Operation(summary = "系统设置-系统-组织与项目-项目-移除成员")
    @Parameter(name = "userId", description = "用户id", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Parameter(name = "projectId", description = "项目id", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_DELETE)
    public void removeProjectMember(@PathVariable String projectId, @PathVariable String userId) {
        systemProjectService.removeProjectMember(projectId, userId, SessionUtils.getUserId());
    }

    @GetMapping("/user-list")
    @Operation(summary = "系统设置-系统-组织与项目-项目-系统-组织及项目, 获取管理员下拉选项")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public List<User> getUserList(@Schema(description = "查询关键字，根据邮箱和用户名查询")
                                  @RequestParam(value = "keyword", required = false) String keyword) {
        return userService.getUserList(keyword);
    }

    @PostMapping("/pool-options")
    @Operation(summary = "系统设置-系统-组织与项目-项目-获取资源池下拉选项")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public List<OptionDTO> getProjectOptions(@Validated @RequestBody ProjectPoolRequest request) {
        return systemProjectService.getTestResourcePoolOptions(request);
    }

    @PostMapping("/rename")
    @Operation(summary = "系统设置-系统-组织与项目-项目-修改项目名称")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.renameLog(#request)", mmClass = SystemProjectLogService.class)
    public void rename(@RequestBody @Validated({Updated.class}) UpdateProjectNameRequest request) {
        systemProjectService.rename(request, SessionUtils.getUserId());
    }

    @GetMapping("/list")
    @Operation(summary = "系统设置-系统-组织与项目-项目-获取所有项目")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public List<OptionDTO> getProjectList(@Schema(description = "查询关键字，根据项目名查询", requiredMode = Schema.RequiredMode.REQUIRED) @RequestParam(value = "keyword", required = false) String keyword) {
        return systemProjectService.list(keyword);
    }

}
