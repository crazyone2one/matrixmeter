package cn.master.matrix.service.log;

import cn.master.matrix.constants.OperationLogConstants;
import cn.master.matrix.constants.OperationLogModule;
import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.constants.TemplateScene;
import cn.master.matrix.entity.Template;
import cn.master.matrix.payload.dto.LogDTO;
import cn.master.matrix.payload.dto.request.TemplateUpdateRequest;
import cn.master.matrix.service.OrganizationTemplateService;
import cn.master.matrix.util.EnumValidator;
import cn.master.matrix.util.JsonUtils;
import cn.master.matrix.util.Translator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Created by 11's papa on 07/03/2024
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationTemplateLogService {
    @Resource
    private OrganizationTemplateService organizationTemplateService;
    public LogDTO addLog(TemplateUpdateRequest request) {
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

    public String getOperationLogModule(String scene) {
        TemplateScene templateScene = EnumValidator.validateEnum(TemplateScene.class, scene);
        switch (templateScene) {
            case API:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_API_TEMPLATE;
            case FUNCTIONAL:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_FUNCTIONAL_TEMPLATE;
            case UI:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_UI_TEMPLATE;
            case BUG:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_BUG_TEMPLATE;
            case TEST_PLAN:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_TEST_PLAN_TEMPLATE;
            default:
                return null;
        }
    }


    public LogDTO disableOrganizationTemplateLog(String organizationId, String scene) {
        return new LogDTO(
                OperationLogConstants.ORGANIZATION,
                organizationId,
                scene,
                null,
                OperationLogType.UPDATE.name(),
                getDisableOrganizationTemplateModule(scene),
                Translator.get("project_template_enable"));
    }

    /**
     * 获取启用项目模板的操作对象
     * @param scene
     * @return
     */
    public String getDisableOrganizationTemplateModule(String scene) {
        TemplateScene templateScene = EnumValidator.validateEnum(TemplateScene.class, scene);
        switch (templateScene) {
            case API:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_API;
            case FUNCTIONAL:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_FUNCTIONAL;
            case UI:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_UI;
            case BUG:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_BUG;
            case TEST_PLAN:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_TEST_PLAN;
            default:
                return null;
        }
    }

    public LogDTO updateLog(TemplateUpdateRequest request) {
        Template template = organizationTemplateService.getWithCheck(request.getId());
        LogDTO dto = null;
        if (template != null) {
            dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    null,
                    template.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    getOperationLogModule(template.getScene()),
                    BooleanUtils.isTrue(template.getInternal()) ? Translator.get("template.default") : template.getName());
            dto.setOriginalValue(JsonUtils.toJsonBytes(template));
        }
        return dto;
    }

    public LogDTO deleteLog(String id) {
        Template template = organizationTemplateService.getWithCheck(id);
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                null,
                template.getId(),
                null,
                OperationLogType.DELETE.name(),
                getOperationLogModule(template.getScene()),
                template.getName());
        dto.setOriginalValue(JsonUtils.toJsonBytes(template));
        return dto;
    }
}
