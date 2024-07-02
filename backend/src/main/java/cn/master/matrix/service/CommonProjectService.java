package cn.master.matrix.service;

import cn.master.matrix.payload.dto.ProjectDTO;
import cn.master.matrix.payload.dto.request.AddProjectRequest;
import cn.master.matrix.payload.dto.request.ProjectAddMemberBatchRequest;
import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.Project;

/**
 * 项目 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-28T16:11:33.913527
 */
public interface CommonProjectService extends IService<Project> {

    void addProjectMember(ProjectAddMemberBatchRequest request, String createUser, String path, String type, String content, String module);

    ProjectDTO add(AddProjectRequest request, String userId, String path, String module);
}
