package cn.master.matrix.handler.invoker;

import cn.master.matrix.service.CleanupProjectResourceService;
import cn.master.matrix.service.CreateProjectResourceService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Component
public class ProjectServiceInvoker {
    private final List<CleanupProjectResourceService> cleanupProjectResourceServices;

    private final List<CreateProjectResourceService> createProjectResourceServices;

    public ProjectServiceInvoker(List<CleanupProjectResourceService> cleanupProjectResourceServices, List<CreateProjectResourceService> createProjectResourceServices) {
        this.cleanupProjectResourceServices = cleanupProjectResourceServices;
        this.createProjectResourceServices = createProjectResourceServices;
    }

    public void invokeServices(String projectId) {
        for (CleanupProjectResourceService service : cleanupProjectResourceServices) {
            service.deleteResources(projectId);
        }
    }

    public void invokeCreateServices(String projectId) {
        for (CreateProjectResourceService service : createProjectResourceServices) {
            service.createResources(projectId);
        }
    }
}
