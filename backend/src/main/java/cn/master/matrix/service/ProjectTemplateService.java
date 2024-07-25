package cn.master.matrix.service;

import cn.master.matrix.entity.Template;
import cn.master.matrix.payload.dto.project.ProjectTemplateDTO;
import cn.master.matrix.payload.dto.request.TemplateUpdateRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 07/19/2024
 **/
public interface ProjectTemplateService extends BaseTemplateService {
    List<ProjectTemplateDTO> getList(String projectId, String scene);

    Template add(TemplateUpdateRequest request, String creator);

    Template update(TemplateUpdateRequest request);

    @Override
    void delete(String id);

    void setDefaultTemplate(String projectId, String id);

    Map<String, Boolean> getProjectTemplateEnableConfig(String projectId);
}
