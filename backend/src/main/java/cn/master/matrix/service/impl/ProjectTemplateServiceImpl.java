package cn.master.matrix.service.impl;

import cn.master.matrix.constants.ProjectApplicationType;
import cn.master.matrix.constants.TemplateScene;
import cn.master.matrix.constants.TemplateScopeType;
import cn.master.matrix.entity.ProjectApplication;
import cn.master.matrix.entity.Template;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.handler.converter.ProjectConvertMapper;
import cn.master.matrix.payload.dto.ProjectDTO;
import cn.master.matrix.payload.dto.project.ProjectTemplateDTO;
import cn.master.matrix.payload.dto.request.TemplateUpdateRequest;
import cn.master.matrix.service.*;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

import static cn.master.matrix.exception.CommonResultCode.DEFAULT_TEMPLATE_PERMISSION;
import static cn.master.matrix.exception.ProjectResultCode.PROJECT_TEMPLATE_PERMISSION;

/**
 * @author Created by 11's papa on 07/19/2024
 **/
@Service
public class ProjectTemplateServiceImpl extends BaseTemplateServiceImpl implements ProjectTemplateService {
    private final ProjectService projectService;
    private final ProjectApplicationService projectApplicationService;

    public ProjectTemplateServiceImpl(BaseCustomFieldService baseCustomFieldService,
                                      BaseTemplateCustomFieldService baseTemplateCustomFieldService,
                                      BaseCustomFieldOptionService baseCustomFieldOptionService,
                                      ProjectService projectService, ProjectApplicationService projectApplicationService) {
        super(baseCustomFieldService, baseTemplateCustomFieldService, baseCustomFieldOptionService);
        this.projectService = projectService;
        this.projectApplicationService = projectApplicationService;
    }

    @Override
    public List<ProjectTemplateDTO> getList(String projectId, String scene) {
        projectService.checkResourceExist(projectId);
        List<Template> templates = super.list(projectId, scene);
        List<ProjectTemplateDTO> templateDTOS = templates.stream().map(item -> {
            //val templateDTO = new ProjectTemplateDTO();
            ProjectConvertMapper projectConvertMapper = ProjectConvertMapper.INSTANCE;
            //projectConvertMapper.templateToProjectTemplateDTO(item);
            return projectConvertMapper.templateToProjectTemplateDTO(item);
        }).toList();
        // 标记默认模板
        // 查询项目下设置中配置的默认模板
        String defaultProjectId = getDefaultTemplateId(projectId, scene);
        ProjectTemplateDTO defaultTemplate = templateDTOS.stream()
                .filter(t -> StringUtils.equals(defaultProjectId, t.getId()))
                .findFirst()
                .orElse(null);

        // 如果查询不到默认模板，设置内置模板为默认模板
        if (defaultTemplate == null) {
            Optional<ProjectTemplateDTO> internalTemplate = templateDTOS.stream()
                    .filter(ProjectTemplateDTO::getInternal).findFirst();
            if (internalTemplate.isPresent()) {
                defaultTemplate = internalTemplate.get();
            }
        }
        if (defaultTemplate != null) {
            defaultTemplate.setEnableDefault(true);
        }
        return templateDTOS;
    }

    @Override
    public Template add(TemplateUpdateRequest request, String creator) {
        Template template = new Template();
        BeanUtils.copyProperties(request, template);
        template.setCreateUser(creator);
        checkProjectResourceExist(template);
        checkProjectTemplateEnable(template.getScopeId(), template.getScene());
        template.setScopeType(TemplateScopeType.PROJECT.name());
        template.setRefId(null);
        return super.add(template, request.getCustomFields(), request.getSystemFields());
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
        checkProjectTemplateEnable(originTemplate.getScopeId(), originTemplate.getScene());
        template.setScopeId(originTemplate.getScopeId());
        template.setScene(originTemplate.getScene());
        checkProjectResourceExist(originTemplate);
        return super.update(template, request.getCustomFields(), request.getSystemFields());
    }

    private void checkProjectTemplateEnable(String scopeId, String scene) {
        val project = projectService.getProjectById(scopeId);
        if (isOrganizationTemplateEnable(project.getOrganizationId(), scene)) {
            throw new CustomException(PROJECT_TEMPLATE_PERMISSION);
        }
    }

    private void checkProjectResourceExist(Template template) {
        projectService.checkResourceExist(template.getScopeId());
    }

    private String getDefaultTemplateId(String projectId, String scene) {
        ProjectApplicationType.DEFAULT_TEMPLATE defaultTemplateParam = ProjectApplicationType.DEFAULT_TEMPLATE.getByTemplateScene(scene);
        val projectApplication = projectApplicationService.getByType(projectId, defaultTemplateParam.name());
        return projectApplication == null ? null : projectApplication.getTypeValue();
    }

    @Override
    public void delete(String id) {
        Template template = getWithCheck(id);
        checkProjectTemplateEnable(template.getScopeId(), template.getScene());
        checkDefault(template);
        super.delete(id);
    }

    @Override
    public void setDefaultTemplate(String projectId, String id) {
        Template template = get(id);
        //if (template == null) {
        //    Template pluginBugTemplate = getPluginBugTemplate(projectId);
        //    if (pluginBugTemplate != null && StringUtils.equals(pluginBugTemplate.getId(), id)) {
        //        template = pluginBugTemplate;
        //    }
        //}
        if (template == null) {
            // 为空check抛出异常
            template = getWithCheck(id);
        }
        String paramType = ProjectApplicationType.DEFAULT_TEMPLATE.getByTemplateScene(template.getScene()).name();
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setProjectId(projectId);
        projectApplication.setTypeValue(id);
        projectApplication.setType(paramType);
        projectApplicationService.createOrUpdateConfig(projectApplication);
    }

    @Override
    public Map<String, Boolean> getProjectTemplateEnableConfig(String projectId) {
        projectService.checkResourceExist(projectId);
        ProjectDTO project = projectService.getProjectById(projectId);
        HashMap<String, Boolean> templateEnableConfig = new HashMap<>();
        Arrays.stream(TemplateScene.values())
                .forEach(scene ->
                        templateEnableConfig.put(scene.name(), !isOrganizationTemplateEnable(project.getOrganizationId(), scene.name())));
        return templateEnableConfig;
    }

    private void checkDefault(Template template) {
        String defaultTemplateId = getDefaultTemplateId(template.getScopeId(), template.getScene());
        if (StringUtils.equals(template.getId(), defaultTemplateId)) {
            throw new CustomException(DEFAULT_TEMPLATE_PERMISSION);
        }
    }
}
