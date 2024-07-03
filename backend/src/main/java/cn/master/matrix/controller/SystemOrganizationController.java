package cn.master.matrix.controller;

import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.constants.PermissionConstants;
import cn.master.matrix.handler.annotation.HasAnyAuthorize;
import cn.master.matrix.handler.annotation.HasAuthorize;
import cn.master.matrix.handler.annotation.Log;
import cn.master.matrix.handler.validation.Updated;
import cn.master.matrix.payload.dto.OptionDTO;
import cn.master.matrix.payload.dto.OrganizationDTO;
import cn.master.matrix.payload.dto.ProjectDTO;
import cn.master.matrix.payload.dto.request.*;
import cn.master.matrix.payload.dto.user.UserExtendDTO;
import cn.master.matrix.service.OrganizationService;
import cn.master.matrix.service.SystemProjectService;
import cn.master.matrix.service.UserService;
import cn.master.matrix.service.log.SystemOrganizationLogService;
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
import java.util.Map;

/**
 * @author Created by 11's papa on 07/03/2024
 **/
@RestController
@RequiredArgsConstructor
@Tag(name = "系统设置-系统-组织与项目-组织")
@RequestMapping("/system/organization")
public class SystemOrganizationController {
    private final SystemProjectService systemProjectService;
    private final OrganizationService organizationService;
    private final UserService userService;

    @PostMapping("/list")
    @Operation(summary = "系统设置-系统-组织与项目-组织-获取组织列表")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public Page<OrganizationDTO> list(@Validated @RequestBody OrganizationRequest request) {
        return organizationService.list(request);
    }

    @PostMapping("/update")
    @Operation(summary = "系统设置-系统-组织与项目-组织-修改组织")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.updateLog(#organizationEditRequest)", mmClass = SystemOrganizationLogService.class)
    public void update(@Validated({Updated.class}) @RequestBody OrganizationEditRequest organizationEditRequest) {
        OrganizationDTO organizationDTO = new OrganizationDTO();
        BeanUtils.copyProperties(organizationEditRequest, organizationDTO);
        organizationDTO.setUpdateUser(SessionUtils.getUserId());
        organizationService.update(organizationDTO);
    }

    @PostMapping("/rename")
    @Operation(summary = "系统设置-系统-组织与项目-组织-修改组织名称")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.updateNameLog(#organizationEditRequest)", mmClass = SystemOrganizationLogService.class)
    public void rename(@Validated({Updated.class}) @RequestBody OrganizationNameEditRequest organizationEditRequest) {
        OrganizationDTO organizationDTO = new OrganizationDTO();
        BeanUtils.copyProperties(organizationEditRequest, organizationDTO);
        organizationDTO.setUpdateUser(SessionUtils.getUserId());
        organizationService.updateName(organizationDTO);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-组织-删除组织")
    @Parameter(name = "id", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    //@HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#mmClass.deleteLog(#id)", mmClass = SystemOrganizationLogService.class)
    public void delete(@PathVariable String id) {
        OrganizationDeleteRequest organizationDeleteRequest = new OrganizationDeleteRequest();
        organizationDeleteRequest.setOrganizationId(id);
        organizationDeleteRequest.setDeleteUserId(SessionUtils.getUserId());
        organizationService.delete(organizationDeleteRequest);
    }

    @GetMapping("/recover/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-组织-恢复组织")
    @Parameter(name = "id", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_RECOVER)
    @Log(type = OperationLogType.RECOVER, expression = "#mmClass.recoverLog(#id)", mmClass = SystemOrganizationLogService.class)
    public void recover(@PathVariable String id) {
        organizationService.recover(id);
    }

    @GetMapping("/enable/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-组织-启用组织")
    @Parameter(name = "id", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    public void enable(@PathVariable String id) {
        organizationService.enable(id);
    }

    @GetMapping("/disable/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-组织-结束组织")
    @Parameter(name = "id", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    public void disable(@PathVariable String id) {
        organizationService.disable(id);
    }

    @PostMapping("/option/all")
    @Operation(summary = "系统设置-系统-组织与项目-组织-获取系统所有组织下拉选项")
    @HasAnyAuthorize(authorities = {PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ, PermissionConstants.ORGANIZATION_PROJECT_READ, PermissionConstants.PROJECT_BASE_INFO_READ})
    public List<OptionDTO> listAll() {
        return organizationService.listAll();
    }

    @PostMapping("/list-member")
    @Operation(summary = "系统设置-系统-组织与项目-组织-获取组织成员列表")
    @HasAnyAuthorize(authorities = {PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ, PermissionConstants.SYSTEM_USER_READ})
    public Page<UserExtendDTO> listMember(@Validated @RequestBody OrganizationRequest request) {
        return organizationService.getMemberListBySystem(request);
    }

    @PostMapping("/add-member")
    @Operation(summary = "系统设置-系统-组织与项目-组织-添加组织成员")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_ADD)
    public void addMember(@Validated @RequestBody OrganizationMemberRequest request) {
        organizationService.addMemberBySystem(request, SessionUtils.getUserId());
    }

    @GetMapping("/remove-member/{organizationId}/{userId}")
    @Operation(summary = "系统设置-系统-组织与项目-组织-删除组织成员")
    @Parameters({
            @Parameter(name = "organizationId", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)),
            @Parameter(name = "userId", description = "成员ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    })
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_DELETE)
    public void removeMember(@PathVariable String organizationId, @PathVariable String userId) {
        organizationService.removeMember(organizationId, userId, SessionUtils.getUserId());
    }

    @GetMapping("/default")
    @Operation(summary = "系统设置-系统-组织与项目-组织-获取系统默认组织")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public OrganizationDTO getDefault() {
        return organizationService.getDefault();
    }

    @PostMapping("/list-project")
    @Operation(summary = "系统设置-系统-组织与项目-组织-获取组织下的项目列表")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public Page<ProjectDTO> listProject(@Validated @RequestBody OrganizationProjectRequest request) {
        ProjectRequest projectRequest = new ProjectRequest();
        BeanUtils.copyProperties(request, projectRequest);
        return systemProjectService.getProjectPage(projectRequest);
    }

    @GetMapping("/total")
    @Operation(summary = "系统设置-系统-组织与项目-组织-获取组织和项目总数")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public Map<String, Long> getTotal(@RequestParam(value = "organizationId", required = false) String organizationId) {
        return organizationService.getTotal(organizationId);
    }

    @GetMapping("/get-option/{sourceId}")
    @Operation(summary = "系统设置-系统-组织与项目-获取成员下拉选项")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    @Parameter(name = "sourceId", description = "组织ID或项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    public List<UserExtendDTO> getMemberOption(@PathVariable String sourceId,
                                               @Schema(description = "查询关键字，根据邮箱和用户名查询")
                                               @RequestParam(value = "keyword", required = false) String keyword) {
        return userService.getMemberOption(sourceId, keyword);
    }
}
