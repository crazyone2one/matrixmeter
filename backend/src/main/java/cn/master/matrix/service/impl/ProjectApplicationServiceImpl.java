package cn.master.matrix.service.impl;

import cn.master.matrix.entity.ProjectApplication;
import cn.master.matrix.mapper.ProjectApplicationMapper;
import cn.master.matrix.service.ProjectApplicationService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * 项目应用 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T14:12:02.509153500
 */
@Service
public class ProjectApplicationServiceImpl extends ServiceImpl<ProjectApplicationMapper, ProjectApplication> implements ProjectApplicationService {

    @Override
    public void update(ProjectApplication application, String currentUser) {
        this.doBeforeUpdate(application, currentUser);
        //配置信息入库
        this.createOrUpdateConfig(application);
    }

    @Override
    public void createOrUpdateConfig(ProjectApplication application) {
        String type = application.getType();
        String projectId = application.getProjectId();
        val queryChain = queryChain().where(ProjectApplication::getProjectId).eq(projectId)
                .and(ProjectApplication::getType).eq(type);
        val exists = queryChain.exists();
        if (exists) {
            mapper.updateByQuery(application, queryChain);
        } else {
            mapper.insert(application);
        }
    }

    private void doBeforeUpdate(ProjectApplication application, String currentUser) {
// todo
    }
}
