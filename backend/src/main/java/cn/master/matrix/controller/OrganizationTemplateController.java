package cn.master.matrix.controller;

import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.constants.PermissionConstants;
import cn.master.matrix.entity.Template;
import cn.master.matrix.handler.annotation.HasAuthorize;
import cn.master.matrix.handler.annotation.Log;
import cn.master.matrix.handler.validation.Created;
import cn.master.matrix.handler.validation.Updated;
import cn.master.matrix.payload.dto.TemplateDTO;
import cn.master.matrix.payload.dto.request.TemplateUpdateRequest;
import cn.master.matrix.service.OrganizationTemplateService;
import cn.master.matrix.service.log.OrganizationTemplateLogService;
import cn.master.matrix.util.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 07/03/2024
 **/
@RestController
@Tag(name = "系统设置-组织-模版")
@RequiredArgsConstructor
@RequestMapping("/organization/template")
public class OrganizationTemplateController {
    private final OrganizationTemplateService organizationTemplateService;

    @PostMapping("/add")
    @Operation(summary = "创建模版")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_ADD)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.addLog(#request)", mmClass = OrganizationTemplateLogService.class)
    public Template add(@Validated({Created.class}) @RequestBody TemplateUpdateRequest request) {
        return organizationTemplateService.add(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "更新模版")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.ADD, expression = "#mmClass.updateLog(#request)", mmClass = OrganizationTemplateLogService.class)
    public Template update(@Validated({Updated.class}) @RequestBody TemplateUpdateRequest request) {
        return organizationTemplateService.update(request);
    }

    @GetMapping("/list/{organizationId}/{scene}")
    @Operation(summary = "获取模版列表")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_READ)
    public List<Template> list(@Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
                               @PathVariable String organizationId,
                               @Schema(description = "模板的使用场景（FUNCTIONAL,BUG,API,UI,TEST_PLAN）", requiredMode = Schema.RequiredMode.REQUIRED)
                               @PathVariable String scene) {
        return organizationTemplateService.list(organizationId, scene);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取模版详情")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_READ)
    public TemplateDTO get(@PathVariable String id) {
        return organizationTemplateService.getDtoWithCheck(id);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除模版")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#mmClass.deleteLog(#id)", mmClass = OrganizationTemplateLogService.class)
    public void delete(@PathVariable String id) {
        organizationTemplateService.delete(id);
    }

    @GetMapping("/disable/{organizationId}/{scene}")
    @Operation(summary = "关闭组织模板，开启项目模板")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_ENABLE)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.disableOrganizationTemplateLog(#organizationId,#scene)", mmClass = OrganizationTemplateLogService.class)
    public void disableOrganizationTemplate(@PathVariable String organizationId, @PathVariable String scene) {
        organizationTemplateService.disableOrganizationTemplate(organizationId, scene);
    }

    @GetMapping("/enable/config/{organizationId}")
    @Operation(summary = "是否启用组织模版")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_READ)
    public Map<String, Boolean> getOrganizationTemplateEnableConfig(@PathVariable String organizationId) {
        return organizationTemplateService.getOrganizationTemplateEnableConfig(organizationId);
    }
}
