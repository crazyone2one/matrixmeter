package cn.master.matrix.controller;

import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.constants.PermissionConstants;
import cn.master.matrix.handler.annotation.HasAuthorize;
import cn.master.matrix.handler.annotation.Log;
import cn.master.matrix.handler.validation.Created;
import cn.master.matrix.handler.validation.Updated;
import cn.master.matrix.payload.dto.OptionDTO;
import cn.master.matrix.payload.dto.ProjectDTO;
import cn.master.matrix.payload.dto.request.*;
import cn.master.matrix.payload.dto.user.UserExtendDTO;
import cn.master.matrix.service.OrganizationProjectService;
import cn.master.matrix.service.log.OrganizationProjectLogService;
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
 * @author Created by 11's papa on 07/02/2024
 **/
@RestController
@RequiredArgsConstructor
@Tag(name = "系统设置-组织-项目")
@RequestMapping("/organization/project")
public class OrganizationProjectController {
    private final OrganizationProjectService organizationProjectService;

    @PostMapping("/add")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ_ADD)
    @Log(type = OperationLogType.ADD, expression = "#mmClass.addLog(#request)", mmClass = OrganizationProjectLogService.class)
    @Operation(summary = "系统设置-组织-项目-创建项目")
    public ProjectDTO addProject(@RequestBody @Validated({Created.class}) AddProjectRequest request) {
        return organizationProjectService.add(request, SessionUtils.getUserId());
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "系统设置-组织-项目-根据ID获取项目信息")
    @Parameter(name = "id", description = "项目id", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ)
    public ProjectDTO getProject(@PathVariable @NotBlank String id) {
        return organizationProjectService.get(id);
    }

    @PostMapping("/update")
    @Operation(summary = "系统设置-组织-项目-编辑")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.updateLog(#request)", mmClass = OrganizationProjectLogService.class)
    public ProjectDTO updateProject(@RequestBody @Validated({Updated.class}) UpdateProjectRequest request) {
        return organizationProjectService.update(request, SessionUtils.getUserId());
    }

    @PostMapping("/rename")
    @Operation(summary = "系统设置-组织-项目-修改项目名称")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.renameLog(#request)", mmClass = OrganizationProjectLogService.class)
    public void rename(@RequestBody @Validated({Updated.class}) UpdateProjectNameRequest request) {
        organizationProjectService.rename(request, SessionUtils.getUserId());
    }

    @PostMapping("/page")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ)
    @Operation(summary = "系统设置-组织-项目-获取项目列表")
    public Page<ProjectDTO> getProjectList(@Validated @RequestBody OrganizationProjectRequest request) {
        return organizationProjectService.getProjectList(request);
    }

    @GetMapping("/delete/{id}")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ_DELETE)
    @Operation(summary = "系统设置-组织-项目-删除")
    @Parameter(name = "id", description = "项目", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Log(type = OperationLogType.DELETE, expression = "#mmClass.deleteLog(#id)", mmClass = OrganizationProjectLogService.class)
    public boolean deleteProject(@PathVariable String id) {
        return organizationProjectService.delete(id, SessionUtils.getUserId());
    }

    @GetMapping("/revoke/{id}")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ_RECOVER)
    @Operation(summary = "系统设置-组织-项目-撤销删除")
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.recoverLog(#id)", mmClass = OrganizationProjectLogService.class)
    @Parameter(name = "id", description = "项目", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    public boolean revokeProject(@PathVariable String id) {
        return organizationProjectService.revoke(id, SessionUtils.getUserId());
    }

    @GetMapping("/enable/{id}")
    @Operation(summary = "系统设置-组织-项目-启用")
    @Parameter(name = "id", description = "项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.updateLog(#id)", mmClass = OrganizationProjectLogService.class)
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ_UPDATE)
    public void enable(@PathVariable String id) {
        organizationProjectService.enable(id, SessionUtils.getUserId());
    }

    @GetMapping("/disable/{id}")
    @Operation(summary = "系统设置-组织-项目-禁用")
    @Parameter(name = "id", description = "项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.updateLog(#id)", mmClass = OrganizationProjectLogService.class)
    public void disable(@PathVariable String id) {
        organizationProjectService.disable(id, SessionUtils.getUserId());
    }

    @PostMapping("/member-list")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ)
    @Operation(summary = "系统设置-组织-项目-成员列表")
    public Page<UserExtendDTO> getProjectMember(@Validated @RequestBody ProjectMemberRequest request) {
        return organizationProjectService.getProjectMember(request);
    }

    @PostMapping("/add-members")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_MEMBER_ADD)
    @Operation(summary = "系统设置-组织-项目-添加成员")
    public void addProjectMember(@Validated @RequestBody ProjectAddMemberRequest request) {
        ProjectAddMemberBatchRequest batchRequest = new ProjectAddMemberBatchRequest();
        batchRequest.setProjectIds(List.of(request.getProjectId()));
        batchRequest.setUserIds(request.getUserIds());
        organizationProjectService.addProjectMember(batchRequest, SessionUtils.getUserId());
    }

    @GetMapping("/remove-member/{projectId}/{userId}")
    @Operation(summary = "系统设置-组织-项目-移除成员")
    @Parameter(name = "userId", description = "用户id", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Parameter(name = "projectId", description = "项目id", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_MEMBER_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#mmClass.deleteLog(#projectId)", mmClass = OrganizationProjectLogService.class)
    public void removeProjectMember(@PathVariable String projectId, @PathVariable String userId) {
        organizationProjectService.removeProjectMember(projectId, userId, SessionUtils.getUserId());
    }

    @GetMapping("/user-admin-list/{organizationId}")
    @Operation(summary = "系统设置-组织-项目-获取管理员列表")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ)
    public List<UserExtendDTO> getUserAdminList(@PathVariable String organizationId, @Schema(description = "查询关键字，根据邮箱和用户名查询")
    @RequestParam(value = "keyword", required = false) String keyword) {
        return organizationProjectService.getUserAdminList(organizationId, keyword);
    }

    @GetMapping("/user-member-list/{organizationId}/{projectId}")
    @Operation(summary = "系统设置-组织-项目-获取成员列表")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ)
    public List<UserExtendDTO> getUserMemberList(@PathVariable String organizationId, @PathVariable String projectId,
                                                 @Schema(description = "查询关键字，根据邮箱和用户名查询")
                                                 @RequestParam(value = "keyword", required = false) String keyword) {
        return organizationProjectService.getUserMemberList(organizationId, projectId, keyword);
    }

    @PostMapping("/pool-options")
    @Operation(summary = "系统设置-组织-项目-获取资源池下拉选项")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ)
    public List<OptionDTO> getProjectOptions(@Validated @RequestBody ProjectPoolRequest request) {
        return organizationProjectService.getTestResourcePoolOptions(request);
    }
}
