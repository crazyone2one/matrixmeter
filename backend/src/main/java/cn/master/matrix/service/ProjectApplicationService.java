package cn.master.matrix.service;

import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.ProjectApplication;

/**
 * 项目应用 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T14:12:02.509153500
 */
public interface ProjectApplicationService extends IService<ProjectApplication> {

    void update(ProjectApplication projectApplication, String currentUser);

    void createOrUpdateConfig(ProjectApplication application);
}
