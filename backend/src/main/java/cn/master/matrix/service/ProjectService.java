package cn.master.matrix.service;

import cn.master.matrix.entity.Project;
import cn.master.matrix.payload.dto.ProjectDTO;
import cn.master.matrix.payload.dto.request.project.ProjectSwitchRequest;
import cn.master.matrix.payload.dto.user.UserDTO;

import java.util.List;

/**
 * @author Created by 11's papa on 07/18/2024
 **/
public interface ProjectService {
    ProjectDTO getProjectById(String id);

    List<Project> getUserProject(String organizationId, String userId);

    List<Project> getUserProjectWidthModule(String organizationId, String module, String userId);

    UserDTO switchProject(ProjectSwitchRequest request, String userId);

    Project checkResourceExist(String projectId);
}
