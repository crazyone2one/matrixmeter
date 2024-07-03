package cn.master.matrix.service;

import cn.master.matrix.entity.Template;
import cn.master.matrix.payload.dto.TemplateDTO;
import cn.master.matrix.payload.dto.request.TemplateUpdateRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 07/03/2024
 **/
public interface OrganizationTemplateService extends BaseTemplateService {
    @Override
    List<Template> list(String organizationId, String scene);

    TemplateDTO getDtoWithCheck(String id);

    Template add(TemplateUpdateRequest request, String userId);

    void checkOrganizationTemplateEnable(String orgId, String scene);

    Template update(TemplateUpdateRequest request);

    @Override
    void delete(String id);

    void disableOrganizationTemplate(String organizationId, String scene);

    Map<String, Boolean> getOrganizationTemplateEnableConfig(String organizationId);
}
