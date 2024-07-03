package cn.master.matrix.service.impl;

import cn.master.matrix.constants.TemplateScene;
import cn.master.matrix.constants.TemplateScopeType;
import cn.master.matrix.entity.OrganizationParameter;
import cn.master.matrix.entity.Template;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.payload.dto.TemplateDTO;
import cn.master.matrix.payload.dto.request.TemplateCustomFieldRequest;
import cn.master.matrix.payload.dto.request.TemplateSystemCustomFieldRequest;
import cn.master.matrix.payload.dto.request.TemplateUpdateRequest;
import cn.master.matrix.service.*;
import cn.master.matrix.util.SubListUtils;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.master.matrix.exception.SystemResultCode.ORGANIZATION_TEMPLATE_PERMISSION;

/**
 * @author Created by 11's papa on 07/03/2024
 **/
@Service
public class OrganizationTemplateServiceImpl extends BaseTemplateServiceImpl implements OrganizationTemplateService {
    private final OrganizationService organizationService;
    private final CommonProjectService projectService;
    private final BaseOrganizationParameterService baseOrganizationParameterService;

    public OrganizationTemplateServiceImpl(BaseCustomFieldService baseCustomFieldService,
                                           BaseTemplateCustomFieldService baseTemplateCustomFieldService,
                                           OrganizationService organizationService,
                                           BaseCustomFieldOptionService baseCustomFieldOptionService,
                                           CommonProjectService commonProjectService,
                                           BaseOrganizationParameterService baseOrganizationParameterService) {
        super(baseCustomFieldService, baseTemplateCustomFieldService, baseCustomFieldOptionService);
        this.organizationService = organizationService;
        this.projectService = commonProjectService;
        this.baseOrganizationParameterService = baseOrganizationParameterService;
    }

    @Override
    public List<Template> list(String organizationId, String scene) {
        organizationService.checkResourceExist(organizationId);
        return super.list(organizationId, scene);
    }

    @Override
    public TemplateDTO getDtoWithCheck(String id) {
        Template template = super.getWithCheck(id);
        checkOrgResourceExist(template);
        TemplateDTO templateDTO = super.getTemplateDto(template);
        translateInternalTemplate(List.of(templateDTO));
        return templateDTO;
    }

    @Override
    public Template add(TemplateUpdateRequest request, String userId) {
        Template template = new Template();
        BeanUtils.copyProperties(request, template);
        template.setCreateUser(userId);
        checkOrgResourceExist(template);
        checkOrganizationTemplateEnable(template.getScopeId(), template.getScene());
        template.setScopeType(TemplateScopeType.ORGANIZATION.name());
        template.setRefId(null);
        template = super.add(template, request.getCustomFields(), request.getSystemFields());
        // 同步创建项目级别模板
        addRefProjectTemplate(template, request.getCustomFields(), request.getSystemFields());
        return template;
    }

    private void addRefProjectTemplate(Template orgTemplate, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields) {
        String orgId = orgTemplate.getScopeId();
        List<String> projectIds = projectService.getProjectIdByOrgId(orgId);
        Template template = new Template();
        BeanUtils.copyProperties(orgTemplate, template);
        projectIds.forEach(projectId -> {
            template.setScopeId(projectId);
            template.setRefId(orgTemplate.getId());
            template.setScopeType(TemplateScopeType.PROJECT.name());
            List<TemplateCustomFieldRequest> refCustomFields = getRefTemplateCustomFieldRequest(projectId, customFields);
            super.baseAdd(template, refCustomFields, systemFields);
        });
    }

    @Override
    public void checkOrganizationTemplateEnable(String orgId, String scene) {
        if (!isOrganizationTemplateEnable(orgId, scene)) {
            throw new CustomException(ORGANIZATION_TEMPLATE_PERMISSION);
        }
    }

    @Override
    public Template update(TemplateUpdateRequest request) {
        Template template = new Template();
        BeanUtils.copyProperties(request, template);
        Template originTemplate = super.getWithCheck(template.getId());
        if (originTemplate.getInternal()) {
            // 内置模板不能修改名字
            template.setName(null);
        }
        checkOrganizationTemplateEnable(originTemplate.getScopeId(), originTemplate.getScene());
        template.setScopeId(originTemplate.getScopeId());
        template.setScene(originTemplate.getScene());
        checkOrgResourceExist(originTemplate);
        updateRefProjectTemplate(template, request.getCustomFields(), request.getSystemFields());
        template.setRefId(null);
        return super.update(template, request.getCustomFields(), request.getSystemFields());
    }

    @Override
    public void delete(String id) {
        Template template = getWithCheck(id);
        checkOrganizationTemplateEnable(template.getScopeId(), template.getScene());
        deleteRefProjectTemplate(id);
        super.delete(id);
    }

    @Override
    public void disableOrganizationTemplate(String orgId, String scene) {
        if (StringUtils.isBlank(baseOrganizationParameterService.getValue(orgId, scene))) {
            OrganizationParameter organizationParameter = new OrganizationParameter();
            organizationParameter.setOrganizationId(orgId);
            organizationParameter.setParamKey(baseOrganizationParameterService.getOrgTemplateEnableKeyByScene(scene));
            organizationParameter.setParamValue(BooleanUtils.toStringTrueFalse(false));
            baseOrganizationParameterService.save(organizationParameter);
        }
    }

    @Override
    public Map<String, Boolean> getOrganizationTemplateEnableConfig(String organizationId) {
        organizationService.checkResourceExist(organizationId);
        HashMap<String, Boolean> templateEnableConfig = new HashMap<>();
        Arrays.stream(TemplateScene.values())
                .forEach(scene ->
                        templateEnableConfig.put(scene.name(), isOrganizationTemplateEnable(organizationId, scene.name())));
        return templateEnableConfig;
    }

    private void deleteRefProjectTemplate(String orgTemplateId) {
        // 删除关联的项目模板
        val wrapper = query().where(Template::getRefId).eq(orgTemplateId);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(wrapper));
        // 删除项目模板和字段的关联关系
        List<String> projectTemplateIds = queryChain().select("id").from("template")
                .where(Template::getRefId).eq(orgTemplateId).listAs(String.class);
        // 分批删除
        SubListUtils.dealForSubList(projectTemplateIds, 100, super.baseTemplateCustomFieldService::deleteByTemplateIds);
    }

    private void updateRefProjectTemplate(Template orgTemplate, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields) {
        List<Template> projectTemplates = getByRefId(orgTemplate.getId());
        Template template = new Template();
        BeanUtils.copyProperties(orgTemplate, template);
        projectTemplates.forEach(projectTemplate -> {
            template.setId(projectTemplate.getId());
            template.setScopeId(projectTemplate.getScopeId());
            template.setRefId(orgTemplate.getId());
            template.setScene(orgTemplate.getScene());
            List<TemplateCustomFieldRequest> refCustomFields = getRefTemplateCustomFieldRequest(projectTemplate.getScopeId(), customFields);
            super.update(template, refCustomFields, systemFields);
        });
    }

    private List<Template> getByRefId(String refId) {
        return queryChain().where(Template::getRefId).eq(refId).list();
    }

    private void checkOrgResourceExist(Template template) {
        organizationService.checkResourceExist(template.getScopeId());
    }
}
