package cn.master.matrix.service;

import cn.master.matrix.payload.dto.ProjectDTO;
import cn.master.matrix.payload.dto.request.AddProjectRequest;
import cn.master.matrix.payload.dto.request.OrganizationProjectRequest;
import cn.master.matrix.payload.dto.request.ProjectMemberRequest;
import cn.master.matrix.payload.dto.request.UpdateProjectRequest;
import cn.master.matrix.payload.dto.user.UserExtendDTO;
import com.mybatisflex.core.paginate.Page;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
public interface OrganizationProjectService {
    ProjectDTO add(AddProjectRequest request, String userId);

    ProjectDTO get(String id);

    Page<ProjectDTO> getProjectList(OrganizationProjectRequest request);

    ProjectDTO update(UpdateProjectRequest request, String userId);

    boolean delete(String id, String deleteUser);

    boolean revoke(String id, String userId);

    boolean enable(String id, String userId);

    boolean disable(String id, String userId);

    Page<UserExtendDTO> getProjectMember(ProjectMemberRequest request);
}
