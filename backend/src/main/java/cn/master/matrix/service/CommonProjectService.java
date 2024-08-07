package cn.master.matrix.service;

import cn.master.matrix.payload.dto.OptionDTO;
import cn.master.matrix.payload.dto.ProjectDTO;
import cn.master.matrix.payload.dto.request.*;
import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.Project;

import java.util.List;

/**
 * 项目 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-28T16:11:33.913527
 */
public interface CommonProjectService extends IService<Project> {

    void addProjectMember(ProjectAddMemberBatchRequest request, String createUser, String path, String type, String content, String module);

    ProjectDTO add(AddProjectRequest request, String userId, String path, String module);

    ProjectDTO get(String id);

    ProjectDTO update(UpdateProjectRequest request, String userId, String path, String module);

    boolean delete(String id, String deleteUser);

    boolean revoke(String id, String userId);

    boolean enable(String id, String userId);

    boolean disable(String id, String userId);

    void removeProjectMember(String projectId, String userId, String createUser, String module, String path);

    void checkProjectNotExist(String projectId);

    List<OptionDTO> getTestResourcePoolOptions(ProjectPoolRequest request);

    void rename(UpdateProjectNameRequest request, String userId);

    List<String> getProjectIdByOrgId(String orgId);

    List<ProjectDTO> buildUserInfo(List<ProjectDTO> records);
}
