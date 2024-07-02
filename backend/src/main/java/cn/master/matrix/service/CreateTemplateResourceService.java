package cn.master.matrix.service;

import cn.master.matrix.constants.TemplateScene;
import cn.master.matrix.constants.TemplateScopeType;
import cn.master.matrix.entity.*;
import cn.master.matrix.handler.resolver.AbstractCustomFieldResolver;
import cn.master.matrix.handler.resolver.CustomFieldResolverFactory;
import cn.master.matrix.mapper.ProjectMapper;
import cn.master.matrix.payload.dto.request.TemplateCustomFieldRequest;
import cn.master.matrix.payload.dto.request.TemplateSystemCustomFieldRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTemplateResourceService implements CreateProjectResourceService {
    private final ProjectMapper projectMapper;
    private final BaseTemplateService baseTemplateService;
    private final BaseTemplateCustomFieldService baseTemplateCustomFieldService;
    @Qualifier("baseCustomFieldService")
    private final BaseCustomFieldService baseCustomFieldService;
    private final BaseCustomFieldOptionService baseCustomFieldOptionService;
    private final BaseStatusItemService baseStatusItemService;
    private final BaseStatusDefinitionService baseStatusDefinitionService;
    private final BaseStatusFlowService baseStatusFlowService;
    private final BaseStatusFlowSettingService baseStatusFlowSettingService;

    @Override
    public void createResources(String projectId) {
        val project = projectMapper.selectOneById(projectId);
        if (Objects.isNull(project)) {
            return;
        }
        String organizationId = project.getOrganizationId();
        for (TemplateScene scene : TemplateScene.values()) {
            if (baseTemplateService.isOrganizationTemplateEnable(organizationId, scene.name())) {
                // 如果没有开启项目模板，则根据组织模板创建项目模板
                // 先创建字段再创建模板
                createProjectCustomField(projectId, organizationId, scene);
                createProjectTemplate(projectId, organizationId, scene);
                createProjectStatusSetting(projectId, organizationId, scene);
            } else {
                // 开启了项目模板，则初始化项目模板和字段
                initProjectTemplate(projectId, scene);
            }
        }
    }

    private void initProjectTemplate(String projectId, TemplateScene scene) {
        switch (scene) {
            case FUNCTIONAL:
                baseTemplateService.initFunctionalDefaultTemplate(projectId, TemplateScopeType.PROJECT);
                break;
            case BUG:
                baseTemplateService.initBugDefaultTemplate(projectId, TemplateScopeType.PROJECT);
                baseStatusFlowSettingService.initBugDefaultStatusFlowSetting(projectId, TemplateScopeType.PROJECT);
                break;
            case API:
                baseTemplateService.initApiDefaultTemplate(projectId, TemplateScopeType.PROJECT);
                break;
            case UI:
                baseTemplateService.initUiDefaultTemplate(projectId, TemplateScopeType.PROJECT);
                break;
            case TEST_PLAN:
                baseTemplateService.initTestPlanDefaultTemplate(projectId, TemplateScopeType.PROJECT);
                break;
            default:
                break;
        }
    }

    private void createProjectStatusSetting(String projectId, String organizationId, TemplateScene scene) {
        List<StatusItem> orgStatusItems = baseStatusItemService.getStatusItems(organizationId, scene.name());
        List<String> orgStatusItemIds = orgStatusItems.stream().map(StatusItem::getId).toList();

        // 同步创建项目级别状态项
        List<StatusItem> projectStatusItems = orgStatusItems.stream().map(orgStatusItem -> {
            StatusItem statusItem = new StatusItem();
            BeanUtils.copyProperties(orgStatusItem, statusItem);
            statusItem.setScopeType(TemplateScopeType.PROJECT.name());
            statusItem.setRefId(orgStatusItem.getId());
            statusItem.setScopeId(projectId);
            statusItem.setId(UUID.randomUUID().toString());
            return statusItem;
        }).toList();
        baseStatusItemService.batchAdd(projectStatusItems);

        // 构建组织状态与对应项目状态的映射关系
        Map<String, String> statusRefMap = projectStatusItems.stream()
                .collect(Collectors.toMap(StatusItem::getRefId, StatusItem::getId));

        // 同步创建项目级别状态定义
        List<StatusDefinition> orgStatusDefinitions = baseStatusDefinitionService.getStatusDefinitions(orgStatusItemIds);
        List<StatusDefinition> projectStatusDefinition = orgStatusDefinitions.stream().map(orgStatusDefinition -> {
            StatusDefinition statusDefinition = new StatusDefinition();
            BeanUtils.copyProperties(orgStatusDefinition, statusDefinition);
            statusDefinition.setStatusId(statusRefMap.get(orgStatusDefinition.getStatusId()));
            return statusDefinition;
        }).toList();
        baseStatusDefinitionService.batchAdd(projectStatusDefinition);

        // 同步创建项目级别状态流
        List<StatusFlow> orgStatusFlows = baseStatusFlowService.getStatusFlows(orgStatusItemIds);
        List<StatusFlow> projectStatusFlows = orgStatusFlows.stream().map(orgStatusFlow -> {
            StatusFlow statusFlow = new StatusFlow();
            BeanUtils.copyProperties(orgStatusFlow, statusFlow);
            statusFlow.setToId(statusRefMap.get(orgStatusFlow.getToId()));
            statusFlow.setFromId(statusRefMap.get(orgStatusFlow.getFromId()));
            statusFlow.setId(UUID.randomUUID().toString());
            return statusFlow;
        }).toList();
        baseStatusFlowService.batchAdd(projectStatusFlows);
    }

    private void createProjectTemplate(String projectId, String organizationId, TemplateScene scene) {
        List<Template> orgTemplates = baseTemplateService.getTemplates(organizationId, scene.name());
        List<String> orgTemplateIds = orgTemplates.stream().map(Template::getId).toList();
        Map<String, List<TemplateCustomField>> templateCustomFieldMap = baseTemplateCustomFieldService.getByTemplateIds(orgTemplateIds)
                .stream()
                .collect(Collectors.groupingBy(TemplateCustomField::getTemplateId));
        Map<String, CustomField> customFieldMap = baseCustomFieldService.getByScopeIdAndScene(organizationId, scene.name()).stream()
                .collect(Collectors.toMap(CustomField::getId, Function.identity()));
        // 忽略默认值校验，可能有多选框的选项被删除，造成不合法数据
        baseTemplateCustomFieldService.setValidateDefaultValue(false);
        orgTemplates.forEach((template) -> {
            List<TemplateCustomField> templateCustomFields = templateCustomFieldMap.get(template.getId());
            templateCustomFields = templateCustomFields == null ? List.of() : templateCustomFields;
            List<TemplateCustomFieldRequest> templateCustomFieldRequests = templateCustomFields.stream()
                    .map(templateCustomField -> {
                        TemplateCustomFieldRequest templateCustomFieldRequest = new TemplateCustomFieldRequest();
                        BeanUtils.copyProperties(templateCustomField, templateCustomFieldRequest);
                        CustomField customField = customFieldMap.get(templateCustomField.getFieldId());
                        try {
                            if (StringUtils.isNotBlank(templateCustomField.getDefaultValue())) {
                                // 将字符串转成对应的对象，方便调用统一的创建方法
                                AbstractCustomFieldResolver customFieldResolver = CustomFieldResolverFactory.getResolver(customField.getType());
                                templateCustomFieldRequest.setDefaultValue(customFieldResolver.parse2Value(templateCustomField.getDefaultValue()));
                            }
                        } catch (Exception e) {
                            baseTemplateCustomFieldService.removeValidateDefaultValue();
                            log.error(e.getMessage());
                            templateCustomFieldRequest.setDefaultValue(null);
                        }
                        return templateCustomFieldRequest;
                    })
                    .toList();
            addRefProjectTemplate(projectId, template, templateCustomFieldRequests, null);
        });
        baseTemplateCustomFieldService.removeValidateDefaultValue();
    }

    private void addRefProjectTemplate(String projectId, Template orgTemplate, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemCustomFields) {
        Template template = new Template();
        BeanUtils.copyProperties(orgTemplate, template);
        template.setScopeId(projectId);
        template.setRefId(orgTemplate.getId());
        template.setScopeType(TemplateScopeType.PROJECT.name());
        List<TemplateCustomFieldRequest> refCustomFields = baseTemplateService.getRefTemplateCustomFieldRequest(projectId, customFields);
        baseTemplateService.baseAdd(template, refCustomFields, systemCustomFields);
    }

    private void createProjectCustomField(String projectId, String organizationId, TemplateScene scene) {
        List<CustomField> orgFields = baseCustomFieldService.getByScopeIdAndScene(organizationId, scene.name());
        List<String> orgFieldIds = orgFields.stream().map(CustomField::getId).toList();
        Map<String, List<CustomFieldOption>> customFieldOptionMap = baseCustomFieldOptionService.getByFieldIds(orgFieldIds)
                .stream()
                .collect(Collectors.groupingBy(CustomFieldOption::getFieldId));

        orgFields.forEach((field) -> {
            List<CustomFieldOption> options = customFieldOptionMap.get(field.getId());
            addRefProjectCustomField(projectId, field, options);
        });
    }

    private void addRefProjectCustomField(String projectId, CustomField customField, List<CustomFieldOption> options) {
        CustomField projectField = new CustomField();
        BeanUtils.copyProperties(customField, projectField);
        projectField.setScopeId(projectId);
        projectField.setScene(TemplateScopeType.PROJECT.name());
        projectField.setRefId(customField.getId());
        baseCustomFieldService.baseAdd(customField, options);
    }
}
