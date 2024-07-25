package cn.master.matrix.controller;

import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.constants.PermissionConstants;
import cn.master.matrix.entity.Template;
import cn.master.matrix.handler.annotation.HasAuthorize;
import cn.master.matrix.handler.annotation.Log;
import cn.master.matrix.handler.validation.Created;
import cn.master.matrix.handler.validation.Updated;
import cn.master.matrix.payload.dto.project.ProjectTemplateDTO;
import cn.master.matrix.payload.dto.request.TemplateUpdateRequest;
import cn.master.matrix.service.ProjectTemplateService;
import cn.master.matrix.service.log.ProjectTemplateLogService;
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
 * @author Created by 11's papa on 07/19/2024
 **/
@RestController
@Tag(name = "项目管理-模版")
@RequiredArgsConstructor
@RequestMapping("/project/template")
public class ProjectTemplateController {
    private final ProjectTemplateService projectTemplateService;

    @GetMapping("/list/{projectId}/{scene}")
    @Operation(summary = "获取模版列表")
    @HasAuthorize(PermissionConstants.PROJECT_TEMPLATE_READ)
    public List<ProjectTemplateDTO> list(@Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
                                         @PathVariable String projectId,
                                         @Schema(description = "模板的使用场景（FUNCTIONAL,BUG,API,UI,TEST_PLAN）", requiredMode = Schema.RequiredMode.REQUIRED)
                                         @PathVariable String scene) {
        return projectTemplateService.getList(projectId, scene);
    }

    @PostMapping("/add")
    @Operation(summary = "创建模版")
    @HasAuthorize(PermissionConstants.PROJECT_TEMPLATE_ADD)
    @Log(type = OperationLogType.ADD, expression = "#mmClass.addLog(#request)", mmClass = ProjectTemplateLogService.class)
    public Template add(@Validated({Created.class}) @RequestBody TemplateUpdateRequest request) {
        return projectTemplateService.add(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "更新模版")
    @HasAuthorize(PermissionConstants.PROJECT_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.updateLog(#request)", mmClass = ProjectTemplateLogService.class)
    public Template update(@Validated({Updated.class}) @RequestBody TemplateUpdateRequest request) {
        return projectTemplateService.update(request);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除模版")
    @HasAuthorize(PermissionConstants.PROJECT_TEMPLATE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#mmClass.deleteLog(#id)", mmClass = ProjectTemplateLogService.class)
    public void delete(@PathVariable String id) {
        projectTemplateService.delete(id);
    }

    @GetMapping("/set-default/{projectId}/{id}")
    @Operation(summary = "设置默认模板")
    @HasAuthorize(PermissionConstants.PROJECT_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.setDefaultTemplateLog(#id)", mmClass = ProjectTemplateLogService.class)
    public void setDefaultTemplate(@PathVariable String projectId, @PathVariable String id) {
        projectTemplateService.setDefaultTemplate(projectId, id);
    }

    @GetMapping("/enable/config/{projectId}")
    @Operation(summary = "是否启用组织模版")
    @HasAuthorize(PermissionConstants.PROJECT_TEMPLATE_READ)
    public Map<String, Boolean> getProjectTemplateEnableConfig(@PathVariable String projectId) {
        return projectTemplateService.getProjectTemplateEnableConfig(projectId);
    }
}
