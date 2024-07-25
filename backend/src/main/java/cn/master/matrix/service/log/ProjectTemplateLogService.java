package cn.master.matrix.service.log;

import cn.master.matrix.constants.OperationLogModule;
import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.constants.TemplateScene;
import cn.master.matrix.entity.Template;
import cn.master.matrix.payload.dto.LogDTO;
import cn.master.matrix.payload.dto.request.TemplateUpdateRequest;
import cn.master.matrix.service.ProjectTemplateService;
import cn.master.matrix.util.EnumValidator;
import cn.master.matrix.util.JsonUtils;
import cn.master.matrix.util.Translator;
import com.alibaba.excel.util.BooleanUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @author Created by 11's papa on 07/19/2024
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectTemplateLogService {
    @Resource
    ProjectTemplateService projectTemplateService;

    public LogDTO addLog(TemplateUpdateRequest request) {
        LogDTO dto = new LogDTO(
                null,
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
        return switch (Objects.requireNonNull(templateScene)) {
            case API -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_API_TEMPLATE;
            case FUNCTIONAL -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_FUNCTIONAL_TEMPLATE;
            case UI -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_UI_TEMPLATE;
            case BUG -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_BUG_TEMPLATE;
            case TEST_PLAN -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_TEST_PLAN_TEMPLATE;
        };
    }

    public LogDTO updateLog(TemplateUpdateRequest request) {
        Template template = projectTemplateService.getWithCheck(request.getId());
        LogDTO dto = null;
        if (template != null) {
            dto = new LogDTO(
                    null,
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

    public LogDTO setDefaultTemplateLog(String id) {
        Template template = projectTemplateService.getWithCheck(id);
        LogDTO dto = null;
        if (template != null) {
            dto = new LogDTO(
                    null,
                    null,
                    template.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    getOperationLogModule(template.getScene()),
                    StringUtils.join(Translator.get("set_default_template"), ":",
                            template.getInternal() ? projectTemplateService.translateInternalTemplate() : template.getName()));
            dto.setOriginalValue(JsonUtils.toJsonBytes(template));
        }
        return dto;
    }

    public LogDTO deleteLog(String id) {
        Template template = projectTemplateService.getWithCheck(id);
        LogDTO dto = new LogDTO(
                null,
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
