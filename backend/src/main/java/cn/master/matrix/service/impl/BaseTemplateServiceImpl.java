package cn.master.matrix.service.impl;

import cn.master.matrix.constants.TemplateScene;
import cn.master.matrix.constants.TemplateScopeType;
import cn.master.matrix.entity.CustomField;
import cn.master.matrix.entity.CustomFieldOption;
import cn.master.matrix.entity.Template;
import cn.master.matrix.entity.TemplateCustomField;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.handler.resolver.AbstractCustomFieldResolver;
import cn.master.matrix.handler.resolver.CustomFieldResolverFactory;
import cn.master.matrix.mapper.TemplateMapper;
import cn.master.matrix.payload.dto.TemplateCustomFieldDTO;
import cn.master.matrix.payload.dto.TemplateDTO;
import cn.master.matrix.payload.dto.request.TemplateCustomFieldRequest;
import cn.master.matrix.payload.dto.request.TemplateSystemCustomFieldRequest;
import cn.master.matrix.service.BaseCustomFieldOptionService;
import cn.master.matrix.service.BaseCustomFieldService;
import cn.master.matrix.service.BaseTemplateCustomFieldService;
import cn.master.matrix.service.BaseTemplateService;
import cn.master.matrix.util.ServiceUtils;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.master.matrix.entity.table.TemplateTableDef.TEMPLATE;
import static cn.master.matrix.exception.CommonResultCode.*;

/**
 * 模版 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T14:28:51.717977100
 */
@Slf4j
@RequiredArgsConstructor
@Service("baseTemplateService")
public class BaseTemplateServiceImpl extends ServiceImpl<TemplateMapper, Template> implements BaseTemplateService {
    @Qualifier("baseCustomFieldService")
    private final BaseCustomFieldService baseCustomFieldService;
    final BaseTemplateCustomFieldService baseTemplateCustomFieldService;
    private final BaseCustomFieldOptionService baseCustomFieldOptionService;

    @Override
    public boolean isOrganizationTemplateEnable(String orgId, String scene) {
        return baseCustomFieldService.isOrganizationTemplateEnable(orgId, scene);
    }

    @Override
    public List<Template> getTemplates(String scopeId, String name) {
        val queryChain = queryChain().where(Template::getScene).eq(name).and(Template::getScopeId).eq(scopeId);
        return mapper.selectListWithRelationsByQuery(queryChain);
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
    @Transactional(rollbackFor = Exception.class)
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

    @Override
    public List<Template> list(String scopeId, String scene) {
        checkScene(scene);
        List<Template> templates = getTemplates(scopeId, scene);
        translateInternalTemplate(templates);
        return templates;
    }

    @Override
    public List<Template> translateInternalTemplate(List<Template> templates) {
        templates.forEach(item -> {
            if (item.getInternal()) {
                item.setName(translateInternalTemplate());
            }
        });
        return templates;
    }

    @Override
    public String translateInternalTemplate() {
        return Translator.get("template.default");
    }

    @Override
    public Template getWithCheck(String id) {
        return checkResourceExist(id);
    }

    @Override
    public TemplateDTO getTemplateDto(Template template) {
        List<TemplateCustomField> templateCustomFields = baseTemplateCustomFieldService.getByTemplateId(template.getId());
        // 查找字段名称
        List<String> fieldIds = templateCustomFields.stream().map(TemplateCustomField::getFieldId).toList();
        List<CustomField> customFields = baseCustomFieldService.getByIds(fieldIds);
        Map<String, CustomField> fieldMap = customFields
                .stream()
                .collect(Collectors.toMap(CustomField::getId, Function.identity()));
        val fieldDTOS = templateCustomFields.stream()
                .filter(item -> !BooleanUtils.isTrue(item.getSystemField()) && fieldMap.containsKey(item.getFieldId()))
                .sorted(Comparator.comparingInt(TemplateCustomField::getPos))
                .map(item -> {
                    CustomField customField = fieldMap.get(item.getFieldId());
                    TemplateCustomFieldDTO templateCustomFieldDTO = new TemplateCustomFieldDTO();
                    BeanUtils.copyProperties(item, templateCustomFieldDTO);
                    templateCustomFieldDTO.setFieldName(customField.getName());
                    if (BooleanUtils.isTrue(customField.getInternal())) {
                        templateCustomFieldDTO.setInternalFieldKey(customField.getName());
                        templateCustomFieldDTO.setFieldName(baseCustomFieldService.translateInternalField(customField.getName()));
                    }
                    templateCustomFieldDTO.setType(customField.getType());
                    templateCustomFieldDTO.setInternal(customField.getInternal());
                    AbstractCustomFieldResolver customFieldResolver = CustomFieldResolverFactory.getResolver(customField.getType());
                    Object defaultValue = null;
                    try {
                        defaultValue = customFieldResolver.parse2Value(item.getDefaultValue());
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                    templateCustomFieldDTO.setDefaultValue(defaultValue);
                    return templateCustomFieldDTO;
                }).toList();
        List<String> ids = fieldDTOS.stream().map(TemplateCustomFieldDTO::getFieldId).toList();
        List<CustomFieldOption> fieldOptions = baseCustomFieldOptionService.getByFieldIds(ids);
        Map<String, List<CustomFieldOption>> collect = fieldOptions.stream().collect(Collectors.groupingBy(CustomFieldOption::getFieldId));

        fieldDTOS.forEach(item -> {
            item.setOptions(collect.get(item.getFieldId()));
        });
        // 封装系统字段信息
        List<TemplateCustomFieldDTO> systemFieldDTOS = templateCustomFields.stream()
                .filter(i -> BooleanUtils.isTrue(i.getSystemField()))
                .map(i -> {
                    TemplateCustomFieldDTO templateCustomFieldDTO = new TemplateCustomFieldDTO();
                    templateCustomFieldDTO.setFieldId(i.getFieldId());
                    templateCustomFieldDTO.setDefaultValue(i.getDefaultValue());
                    return templateCustomFieldDTO;
                }).toList();
        List<String> sysIds = systemFieldDTOS.stream().map(TemplateCustomFieldDTO::getFieldId).toList();
        List<CustomFieldOption> sysFieldOptions = baseCustomFieldOptionService.getByFieldIds(sysIds);
        Map<String, List<CustomFieldOption>> sysCollect = sysFieldOptions.stream().collect(Collectors.groupingBy(CustomFieldOption::getFieldId));

        systemFieldDTOS.forEach(item -> {
            item.setOptions(sysCollect.get(item.getFieldId()));
        });

        TemplateDTO templateDTO = new TemplateDTO();
        BeanUtils.copyProperties(template, templateDTO);
        templateDTO.setCustomFields(fieldDTOS);
        templateDTO.setSystemFields(systemFieldDTOS);
        return templateDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Template add(Template template, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields) {
        template.setInternal(false);
        return this.baseAdd(template, customFields, systemFields);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Template update(Template template, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields) {
        checkResourceExist(template.getId());
        checkUpdateExist(template);
        // customFields 为 null 则不修改
        if (customFields != null) {
            baseTemplateCustomFieldService.deleteByTemplateIdAndSystem(template.getId(), false);
            baseTemplateCustomFieldService.addCustomFieldByTemplateId(template.getId(), customFields);
        }
        if (systemFields != null) {
            // 系统字段
            baseTemplateCustomFieldService.deleteByTemplateIdAndSystem(template.getId(), true);
            baseTemplateCustomFieldService.addSystemFieldByTemplateId(template.getId(), parse2TemplateCustomFieldRequests(systemFields));
        }
        mapper.update(template);
        return template;
    }

    @Override
    public void delete(String id) {
        Template template = checkResourceExist(id);
        checkInternal(template);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteById(id));
        baseTemplateCustomFieldService.deleteByTemplateId(id);
    }

    @Override
    public Template get(String id) {
        return mapper.selectOneById(id);
    }

    protected void checkInternal(Template template) {
        if (template.getInternal()) {
            throw new CustomException(INTERNAL_TEMPLATE_PERMISSION);
        }
    }

    private void checkUpdateExist(Template template) {
        if (StringUtils.isBlank(template.getName())) {
            return;
        }
        val exists = queryChain().where(TEMPLATE.SCOPE_ID.eq(template.getScopeId())
                .and(TEMPLATE.SCENE.eq(template.getScene()))
                .and(TEMPLATE.NAME.eq(template.getName()))
                .and(TEMPLATE.ID.ne(template.getId()))).exists();
        if (exists) {
            throw new CustomException(TEMPLATE_EXIST);
        }
    }

    private Template checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(mapper.selectOneById(id), "permission.organization_template.name");
    }

    private void checkScene(String scene) {
        Arrays.stream(TemplateScene.values()).map(TemplateScene::name)
                .filter(item -> item.equals(scene))
                .findFirst()
                .orElseThrow(() -> new CustomException(TEMPLATE_SCENE_ILLEGAL));
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
