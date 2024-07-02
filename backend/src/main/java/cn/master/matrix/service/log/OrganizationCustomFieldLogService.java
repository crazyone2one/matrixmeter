package cn.master.matrix.service.log;

import cn.master.matrix.constants.OperationLogConstants;
import cn.master.matrix.constants.OperationLogModule;
import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.constants.TemplateScene;
import cn.master.matrix.entity.CustomField;
import cn.master.matrix.payload.dto.LogDTO;
import cn.master.matrix.payload.dto.request.CustomFieldUpdateRequest;
import cn.master.matrix.service.OrganizationCustomFieldService;
import cn.master.matrix.util.EnumValidator;
import cn.master.matrix.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Service
@RequiredArgsConstructor
public class OrganizationCustomFieldLogService {
    private final OrganizationCustomFieldService organizationCustomFieldService;

    public LogDTO addLog(CustomFieldUpdateRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                null,
                null,
                null,
                OperationLogType.ADD.name(),
                getOperationLogModule(request.getScene()),
                request.getName());
        dto.setOriginalValue(JsonUtils.toJsonBytes(request));
        return dto;
    }

    public LogDTO updateLog(CustomFieldUpdateRequest request) {
        CustomField customField = organizationCustomFieldService.getWithCheck(request.getId());
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                null,
                customField.getId(),
                null,
                OperationLogType.UPDATE.name(),
                getOperationLogModule(customField.getScene()),
                customField.getName());
        dto.setOriginalValue(JsonUtils.toJsonBytes(customField));
        return dto;
    }

    public LogDTO deleteLog(String id) {
        CustomField customField = organizationCustomFieldService.getWithCheck(id);
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                null,
                customField.getId(),
                null,
                OperationLogType.DELETE.name(),
                getOperationLogModule(customField.getScene()),
                customField.getName());
        dto.setOriginalValue(JsonUtils.toJsonBytes(customField));
        return dto;
    }

    private String getOperationLogModule(String scene) {
        TemplateScene templateScene = EnumValidator.validateEnum(TemplateScene.class, scene);
        assert templateScene != null;
        return switch (templateScene) {
            case API -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_API_FIELD;
            case FUNCTIONAL -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_FUNCTIONAL_FIELD;
            case UI -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_UI_FIELD;
            case BUG -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_BUG_FIELD;
            case TEST_PLAN -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_TEST_PLAN_FIELD;
        };
    }
}
