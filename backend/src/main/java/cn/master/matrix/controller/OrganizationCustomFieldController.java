package cn.master.matrix.controller;

import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.constants.PermissionConstants;
import cn.master.matrix.handler.annotation.HasAuthorize;
import cn.master.matrix.handler.annotation.Log;
import cn.master.matrix.handler.validation.Created;
import cn.master.matrix.handler.validation.Updated;
import cn.master.matrix.payload.dto.CustomFieldDTO;
import cn.master.matrix.payload.dto.request.CustomFieldUpdateRequest;
import cn.master.matrix.service.OrganizationCustomFieldService;
import cn.master.matrix.service.log.OrganizationCustomFieldLogService;
import cn.master.matrix.util.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import cn.master.matrix.entity.CustomField;
import cn.master.matrix.service.BaseCustomFieldService;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.io.Serializable;
import java.util.List;

/**
 * 自定义字段 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T11:44:33.956260700
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "系统设置-组织-自定义字段")
@RequestMapping("/organization/custom/field")
public class OrganizationCustomFieldController {

    private final OrganizationCustomFieldService organizationCustomFieldService;

    @PostMapping("save")
    @Operation(description = "保存自定义字段")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_ADD)
    @Log(type = OperationLogType.ADD, expression = "#mmClass.addLog(#request)", mmClass = OrganizationCustomFieldLogService.class)
    public CustomField save(@Validated({Created.class}) @RequestBody @Parameter(description = "自定义字段") CustomFieldUpdateRequest request) {
        CustomField customField = new CustomField();
        BeanUtils.copyProperties(request, customField);
        customField.setCreateUser(SessionUtils.getUserId());
        return organizationCustomFieldService.add(customField, request.getOptions());
    }

    @PostMapping("/update")
    @Operation(summary = "更新自定义字段")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.updateLog(#request)", mmClass = OrganizationCustomFieldLogService.class)
    public CustomField update(@Validated({Updated.class}) @RequestBody CustomFieldUpdateRequest request) {
        CustomField customField = new CustomField();
        BeanUtils.copyProperties(request, customField);
        return organizationCustomFieldService.update(customField, request.getOptions());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除自定义字段")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#mmClass.deleteLog(#id)", mmClass = OrganizationCustomFieldLogService.class)
    public void delete(@PathVariable String id) {
        organizationCustomFieldService.delete(id);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取自定义字段详情")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_READ)
    public CustomFieldDTO get(@PathVariable String id) {
        return organizationCustomFieldService.getCustomFieldWithCheck(id);
    }
    @GetMapping("/list/{organizationId}/{scene}")
    @Operation(summary = "获取自定义字段列表")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_READ)
    public List<CustomFieldDTO> list(@Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
                                     @PathVariable String organizationId,
                                     @Schema(description = "模板的使用场景（FUNCTIONAL,BUG,API,UI,TEST_PLAN）", requiredMode = Schema.RequiredMode.REQUIRED)
                                     @PathVariable String scene) {
        return organizationCustomFieldService.list(organizationId, scene);
    }

}
