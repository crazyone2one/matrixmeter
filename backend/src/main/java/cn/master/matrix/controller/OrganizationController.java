package cn.master.matrix.controller;

import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.constants.PermissionConstants;
import cn.master.matrix.handler.annotation.HasAnyAuthorize;
import cn.master.matrix.handler.annotation.HasAuthorize;
import cn.master.matrix.handler.annotation.Log;
import cn.master.matrix.payload.dto.OptionDTO;
import cn.master.matrix.payload.dto.OptionDisabledDTO;
import cn.master.matrix.payload.dto.OrgUserExtend;
import cn.master.matrix.payload.dto.request.OrgMemberExtendProjectRequest;
import cn.master.matrix.payload.dto.request.OrganizationMemberExtendRequest;
import cn.master.matrix.payload.dto.request.OrganizationMemberUpdateRequest;
import cn.master.matrix.payload.dto.request.OrganizationRequest;
import cn.master.matrix.service.OrganizationService;
import cn.master.matrix.util.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-28T15:16:07.028133900
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "系统设置-组织-成员")
@RequestMapping("/organization")
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping("/add-member")
    @Operation(summary = "系统设置-组织-成员-添加组织成员")
    @HasAuthorize(PermissionConstants.ORGANIZATION_MEMBER_ADD)
    public void addMemberByList(@Validated @RequestBody OrganizationMemberExtendRequest organizationMemberExtendRequest) {
        organizationService.addMemberByOrg(organizationMemberExtendRequest, SessionUtils.getUserId());
    }

    @PostMapping("/role/update-member")
    @Operation(summary = "系统设置-组织-成员-添加组织成员至用户组")
    @HasAuthorize(PermissionConstants.ORGANIZATION_MEMBER_UPDATE)
    public void addMemberRole(@Validated @RequestBody OrganizationMemberExtendRequest organizationMemberExtendRequest) {
        organizationService.addMemberRole(organizationMemberExtendRequest, SessionUtils.getUserId());
    }

    @PostMapping("/update-member")
    @Operation(summary = "系统设置-组织-成员-更新用户")
    @HasAnyAuthorize(authorities = {PermissionConstants.ORGANIZATION_MEMBER_UPDATE, PermissionConstants.PROJECT_USER_READ_ADD, PermissionConstants.PROJECT_USER_READ_DELETE})
    public void updateMember(@Validated @RequestBody OrganizationMemberUpdateRequest organizationMemberExtendRequest) {
        organizationService.updateMember(organizationMemberExtendRequest, SessionUtils.getUserId());
    }

    @PostMapping("/project/add-member")
    @Operation(summary = "系统设置-组织-成员-添加组织成员至项目")
    @HasAuthorize(PermissionConstants.ORGANIZATION_MEMBER_UPDATE)
    public void addMemberToProject(@Validated @RequestBody OrgMemberExtendProjectRequest orgMemberExtendProjectRequest) {
        organizationService.addMemberToProject(orgMemberExtendProjectRequest, SessionUtils.getUserId());
    }

    @GetMapping("/remove-member/{organizationId}/{userId}")
    @Operation(summary = "系统设置-组织-成员-删除组织成员")
    @Parameters({
            @Parameter(name = "organizationId", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)),
            @Parameter(name = "userId", description = "成员ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    })
    @HasAuthorize(PermissionConstants.ORGANIZATION_MEMBER_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#mmClass.batchDelLog(#organizationId, #userId)", mmClass = OrganizationService.class)
    public void removeMember(@PathVariable String organizationId, @PathVariable String userId) {
        organizationService.removeMember(organizationId, userId, SessionUtils.getUserId());
    }

    @GetMapping("/project/list/{organizationId}")
    @Operation(summary = "系统设置-组织-成员-获取当前组织下的所有项目")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ)
    public List<OptionDTO> getProjectList(@PathVariable(value = "organizationId") String organizationId, @Schema(description = "查询关键字，根据项目名查询", requiredMode = Schema.RequiredMode.REQUIRED) @RequestParam(value = "keyword", required = false) String keyword) {
        return organizationService.getProjectList(organizationId, keyword);
    }

    @PostMapping("/member/list")
    @Operation(summary = "系统设置-组织-成员-获取组织成员列表")
    @HasAuthorize(PermissionConstants.ORGANIZATION_MEMBER_READ)
    public Page<OrgUserExtend> page(@Parameter(description = "分页信息") @Validated @RequestBody OrganizationRequest request) {
        return organizationService.getMemberListByOrg(request);
    }

    @GetMapping("/user/role/list/{organizationId}")
    @Operation(summary = "系统设置-组织-成员-获取当前组织下的所有自定义用户组以及组织级别的用户组")
    @HasAuthorize(PermissionConstants.ORGANIZATION_MEMBER_READ)
    public List<OptionDTO> getUserRoleList(@PathVariable(value = "organizationId") String organizationId) {
        return organizationService.getUserRoleList(organizationId);
    }

    @GetMapping("/not-exist/user/list/{organizationId}")
    @Operation(summary = "系统设置-组织-成员-获取不在当前组织的所有用户")
    @HasAuthorize(PermissionConstants.ORGANIZATION_MEMBER_ADD)
    public List<OptionDisabledDTO> getUserList(@PathVariable(value = "organizationId") String organizationId, @Schema(description = "查询关键字，根据用户名查询", requiredMode = Schema.RequiredMode.REQUIRED)
    @RequestParam(value = "keyword", required = false) String keyword) {
        return organizationService.getUserList(organizationId, keyword);
    }

}
