package cn.master.matrix.service;

import cn.master.matrix.constants.InternalUserRole;
import cn.master.matrix.constants.ProjectApplicationType;
import cn.master.matrix.entity.ProjectApplication;
import cn.master.matrix.entity.ProjectVersion;
import cn.master.matrix.mapper.ProjectVersionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateVersionResourceService implements CreateProjectResourceService {
    private final ProjectVersionMapper projectVersionMapper;
    private final ProjectApplicationService projectApplicationService;
    public static final String DEFAULT_VERSION = "v1.0";
    public static final String DEFAULT_VERSION_STATUS = "open";

    @Override
    public void createResources(String projectId) {
// 初始化版本V1.0, 初始化版本配置项
        ProjectVersion defaultVersion = new ProjectVersion();
        defaultVersion.setProjectId(projectId);
        defaultVersion.setName(DEFAULT_VERSION);
        defaultVersion.setStatus(DEFAULT_VERSION_STATUS);
        defaultVersion.setLatest(true);
        defaultVersion.setCreateTime(System.currentTimeMillis());
        defaultVersion.setCreateUser(InternalUserRole.ADMIN.getValue());
        projectVersionMapper.insert(defaultVersion);
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setProjectId(projectId);
        projectApplication.setType(ProjectApplicationType.VERSION.VERSION_ENABLE.name());
        projectApplication.setTypeValue("FALSE");
        projectApplicationService.update(projectApplication, "");
        log.info("初始化当前项目[{}]相关版本资源", projectId);
    }
}
