package cn.master.matrix.service;

import cn.master.matrix.payload.dto.project.request.ProjectApplicationRequest;
import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.ProjectApplication;

import java.util.List;
import java.util.Map;

/**
 * 项目应用 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T14:12:02.509153500
 */
public interface ProjectApplicationService extends IService<ProjectApplication> {

    void update(ProjectApplication projectApplication, String currentUser);

    void createOrUpdateConfig(ProjectApplication application);

    ProjectApplication getByType(String projectId, String type);

    Map<String, Object> get(ProjectApplicationRequest projectApplicationRequest, List<String> collect);

    void putResourcePool(String projectId, Map<String, Object> configMap, String type);
}
