package cn.master.matrix.service;

import cn.master.matrix.constants.TemplateScopeType;
import cn.master.matrix.payload.dto.request.TemplateCustomFieldRequest;
import cn.master.matrix.payload.dto.request.TemplateSystemCustomFieldRequest;
import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.Template;

import java.util.List;

/**
 * 模版 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T14:28:51.717977100
 */
public interface BaseTemplateService extends IService<Template> {

    boolean isOrganizationTemplateEnable(String orgId, String scene);

    List<Template> getTemplates(String scopeId, String name);

    List<TemplateCustomFieldRequest> getRefTemplateCustomFieldRequest(String projectId, List<TemplateCustomFieldRequest> customFields);

    Template baseAdd(Template template, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields);

    void initFunctionalDefaultTemplate(String scopeId, TemplateScopeType scopeType);

    void initBugDefaultTemplate(String scopeId, TemplateScopeType scopeType);

    void initApiDefaultTemplate(String scopeId, TemplateScopeType scopeType);

    void initUiDefaultTemplate(String scopeId, TemplateScopeType scopeType);

    void initTestPlanDefaultTemplate(String scopeId, TemplateScopeType scopeType);
}
