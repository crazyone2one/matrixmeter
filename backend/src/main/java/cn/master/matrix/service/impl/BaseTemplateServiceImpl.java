package cn.master.matrix.service.impl;

import cn.master.matrix.constants.TemplateScene;
import cn.master.matrix.constants.TemplateScopeType;
import cn.master.matrix.entity.CustomField;
import cn.master.matrix.entity.Template;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.mapper.TemplateMapper;
import cn.master.matrix.payload.dto.request.TemplateCustomFieldRequest;
import cn.master.matrix.payload.dto.request.TemplateSystemCustomFieldRequest;
import cn.master.matrix.service.BaseCustomFieldService;
import cn.master.matrix.service.BaseTemplateCustomFieldService;
import cn.master.matrix.service.BaseTemplateService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.master.matrix.entity.table.TemplateTableDef.TEMPLATE;
import static cn.master.matrix.exception.CommonResultCode.TEMPLATE_EXIST;

/**
 * 模版 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T14:28:51.717977100
 */
@Service
@RequiredArgsConstructor
public class BaseTemplateServiceImpl extends ServiceImpl<TemplateMapper, Template> implements BaseTemplateService {
    @Qualifier("baseCustomFieldService")
    private final BaseCustomFieldService baseCustomFieldService;
    private final BaseTemplateCustomFieldService baseTemplateCustomFieldService;

    @Override
    public boolean isOrganizationTemplateEnable(String orgId, String scene) {
        return baseCustomFieldService.isOrganizationTemplateEnable(orgId, scene);
    }

    @Override
    public List<Template> getTemplates(String scopeId, String name) {
        return queryChain().where(Template::getScene).eq(name).and(Template::getScopeId).eq(scopeId).list();
    }

    @Override
    public List<TemplateCustomFieldRequest> getRefTemplateCustomFieldRequest(String projectId, List<TemplateCustomFieldRequest> customFields) {
        if (customFields == null) {
            return null;
        }
        List<String> fieldIds = customFields.stream().map(TemplateCustomFieldRequest::getFieldId).toList();
        // 查询当前组织字段所对应的项目字段，构建map，键为组织字段ID，值为项目字段ID
        Map<String, String> refFieldMap = baseCustomFieldService.getByRefIdsAndScopeId(fieldIds, projectId)
                .stream()
                .collect(Collectors.toMap(CustomField::getRefId, CustomField::getId));
        // 根据组织字段ID，替换为项目字段ID
        return customFields.stream()
                .map(item -> {
                    TemplateCustomFieldRequest request = new TemplateCustomFieldRequest();
                    BeanUtils.copyProperties(item, request);
                    request.setFieldId(refFieldMap.get(item.getFieldId()));
                    return request;
                })
                .filter(item -> StringUtils.isNotBlank(item.getFieldId()))
                .toList();
    }

    @Override
    public Template baseAdd(Template template, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields) {
        checkAddExist(template);
        mapper.insert(template);
        baseTemplateCustomFieldService.addCustomFieldByTemplateId(template.getId(), customFields);
        baseTemplateCustomFieldService.addSystemFieldByTemplateId(template.getId(), parse2TemplateCustomFieldRequests(systemFields));
        return template;
    }

    @Override
    public void initFunctionalDefaultTemplate(String scopeId, TemplateScopeType scopeType) {
        // 初始化字段
        List<CustomField> customFields = baseCustomFieldService.initFunctionalDefaultCustomField(scopeType, scopeId);
        // 初始化模板
        Template template = this.initDefaultTemplate(scopeId, "functional_default", scopeType, TemplateScene.FUNCTIONAL);
        // 初始化模板和字段的关联关系
        List<TemplateCustomFieldRequest> templateCustomFieldRequests = customFields.stream().map(customField -> {
            TemplateCustomFieldRequest templateCustomFieldRequest = new TemplateCustomFieldRequest();
            templateCustomFieldRequest.setRequired(true);
            templateCustomFieldRequest.setFieldId(customField.getId());
            return templateCustomFieldRequest;
        }).toList();
        baseTemplateCustomFieldService.addCustomFieldByTemplateId(template.getId(), templateCustomFieldRequests);
    }

    @Override
    public void initBugDefaultTemplate(String scopeId, TemplateScopeType scopeType) {
        // 初始化字段
        List<CustomField> customFields = baseCustomFieldService.initBugDefaultCustomField(scopeType, scopeId);
        // 初始化模板
        Template template = this.initDefaultTemplate(scopeId, "bug_default", scopeType, TemplateScene.BUG);
        // 初始化模板和字段的关联关系
        List<TemplateCustomFieldRequest> templateCustomFieldRequests = customFields.stream().map(customField -> {
            TemplateCustomFieldRequest templateCustomFieldRequest = new TemplateCustomFieldRequest();
            templateCustomFieldRequest.setRequired(true);
            templateCustomFieldRequest.setFieldId(customField.getId());
            return templateCustomFieldRequest;
        }).toList();
        baseTemplateCustomFieldService.addCustomFieldByTemplateId(template.getId(), templateCustomFieldRequests);
    }

    @Override
    public void initApiDefaultTemplate(String scopeId, TemplateScopeType scopeType) {
        initDefaultTemplate(scopeId, "api_default", scopeType, TemplateScene.API);
    }

    @Override
    public void initUiDefaultTemplate(String scopeId, TemplateScopeType scopeType) {
        initDefaultTemplate(scopeId, "ui_default", scopeType, TemplateScene.UI);
    }

    @Override
    public void initTestPlanDefaultTemplate(String scopeId, TemplateScopeType scopeType) {
        initDefaultTemplate(scopeId, "test_plan_default", scopeType, TemplateScene.TEST_PLAN);
    }

    private Template initDefaultTemplate(String scopeId, String name, TemplateScopeType scopeType, TemplateScene scene) {
        Template template = new Template();
        template.setName(name);
        template.setInternal(true);
        template.setCreateUser("admin");
        template.setScopeType(scopeType.name());
        template.setScopeId(scopeId);
        template.setEnableThirdPart(false);
        template.setScene(scene.name());
        mapper.insert(template);
        return template;
    }

    private List<TemplateCustomFieldRequest> parse2TemplateCustomFieldRequests(List<TemplateSystemCustomFieldRequest> systemFields) {
        if (CollectionUtils.isEmpty(systemFields)) {
            return List.of();
        }
        return systemFields.stream().map(systemFiled -> {
            TemplateCustomFieldRequest templateCustomFieldRequest = new TemplateCustomFieldRequest();
            BeanUtils.copyProperties(systemFiled, templateCustomFieldRequest);
            templateCustomFieldRequest.setRequired(false);
            return templateCustomFieldRequest;
        }).toList();
    }

    private void checkAddExist(Template template) {
        val exists = queryChain()
                .where(TEMPLATE.SCOPE_ID.eq(template.getScopeId())
                        .and(TEMPLATE.SCENE.eq(template.getScene()))
                        .and(TEMPLATE.NAME.eq(template.getName())))
                .exists();
        if (exists) {
            throw new CustomException(TEMPLATE_EXIST);
        }
    }
}
