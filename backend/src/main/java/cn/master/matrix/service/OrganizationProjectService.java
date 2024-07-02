package cn.master.matrix.service;

import cn.master.matrix.payload.dto.ProjectDTO;
import cn.master.matrix.payload.dto.request.AddProjectRequest;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
public interface OrganizationProjectService {
    ProjectDTO add(AddProjectRequest request, String userId);
}
