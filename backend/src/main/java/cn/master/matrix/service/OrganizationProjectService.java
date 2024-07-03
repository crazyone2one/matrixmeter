package cn.master.matrix.service;

import cn.master.matrix.payload.dto.OptionDTO;
import cn.master.matrix.payload.dto.ProjectDTO;
import cn.master.matrix.payload.dto.request.*;
import cn.master.matrix.payload.dto.user.UserExtendDTO;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

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

    void addProjectMember(ProjectAddMemberBatchRequest batchRequest, String userId);

    void removeProjectMember(String projectId, String userId, String createUser);

    List<UserExtendDTO> getUserAdminList(String organizationId, String keyword);

    List<UserExtendDTO> getUserMemberList(String organizationId, String projectId, String keyword);

    List<OptionDTO> getTestResourcePoolOptions(ProjectPoolRequest request);

    void rename(UpdateProjectNameRequest request, String userId);
}
