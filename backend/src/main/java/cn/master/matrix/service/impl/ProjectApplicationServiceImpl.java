package cn.master.matrix.service.impl;

import cn.master.matrix.constants.ProjectApplicationType;
import cn.master.matrix.entity.ProjectApplication;
import cn.master.matrix.entity.ProjectTestResourcePool;
import cn.master.matrix.entity.TestResourcePool;
import cn.master.matrix.mapper.ProjectApplicationMapper;
import cn.master.matrix.payload.dto.project.request.ProjectApplicationRequest;
import cn.master.matrix.service.ProjectApplicationService;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.master.matrix.entity.table.ProjectApplicationTableDef.PROJECT_APPLICATION;
import static cn.master.matrix.entity.table.ProjectTestResourcePoolTableDef.PROJECT_TEST_RESOURCE_POOL;
import static cn.master.matrix.entity.table.TestResourcePoolOrganizationTableDef.TEST_RESOURCE_POOL_ORGANIZATION;
import static cn.master.matrix.entity.table.TestResourcePoolTableDef.TEST_RESOURCE_POOL;

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

    @Override
    public ProjectApplication getByType(String projectId, String type) {
        val list = queryChain().where(PROJECT_APPLICATION.PROJECT_ID.eq(projectId)
                .and(PROJECT_APPLICATION.TYPE.eq(type))).list();
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    @Override
    public Map<String, Object> get(ProjectApplicationRequest request, List<String> collect) {
        Map<String, Object> configMap = new HashMap<>();
        val list = queryChain().where(PROJECT_APPLICATION.PROJECT_ID.eq(request.getProjectId())
                .and(PROJECT_APPLICATION.TYPE.in(collect))).list();
        if (!list.isEmpty()) {
            configMap = list.stream().collect(Collectors.toMap(ProjectApplication::getType, ProjectApplication::getTypeValue));
            putResourcePool(request.getProjectId(), configMap, request.getType());
            return configMap;
        }
        putResourcePool(request.getProjectId(), configMap, request.getType());
        return configMap;
    }

    @Override
    public void putResourcePool(String projectId, Map<String, Object> configMap, String type) {
        String poolType = null;
        String moduleType = null;
        if (StringUtils.isBlank(type)) {
            return;
        }
        if ("apiTest".equals(type)) {
            poolType = ProjectApplicationType.API.API_RESOURCE_POOL_ID.name();
            moduleType = "api_test";
        }
        if (StringUtils.isNotBlank(poolType) && StringUtils.isNotBlank(moduleType)) {
            if (configMap.containsKey(poolType)) {
                //如果是适用于所有的组织
                long count = 0;
                val count1 = QueryChain.of(TestResourcePool.class).where(TestResourcePool::getId).eq(configMap.get(poolType).toString())
                        .and(TestResourcePool::getAllOrg).eq(true).count();
                if (count1 > 0) {
                    count = QueryChain.of(TestResourcePool.class).select(QueryMethods.count(TEST_RESOURCE_POOL.ID))
                            .from(TEST_RESOURCE_POOL)
                            .leftJoin(PROJECT_TEST_RESOURCE_POOL).on(PROJECT_TEST_RESOURCE_POOL.TEST_RESOURCE_POOL_ID.eq(TEST_RESOURCE_POOL.ID))
                            .where(PROJECT_TEST_RESOURCE_POOL.PROJECT_ID.eq(projectId)
                                    .and(TEST_RESOURCE_POOL.ID.eq(configMap.get(poolType).toString()))
                                    .and(TEST_RESOURCE_POOL.ENABLE.eq(true)))
                            .count();
                } else {
                    //指定组织  则需要关联组织-资源池的关系表  看看是否再全部存在
                    count = QueryChain.of(TestResourcePool.class).select(QueryMethods.count(TEST_RESOURCE_POOL.ID))
                            .from(TEST_RESOURCE_POOL_ORGANIZATION)
                            .leftJoin(TEST_RESOURCE_POOL).on(TEST_RESOURCE_POOL_ORGANIZATION.TEST_RESOURCE_POOL_ID.eq(TEST_RESOURCE_POOL.ID))
                            .leftJoin(PROJECT_TEST_RESOURCE_POOL).on(PROJECT_TEST_RESOURCE_POOL.TEST_RESOURCE_POOL_ID.eq(TEST_RESOURCE_POOL.ID))
                            .where(PROJECT_TEST_RESOURCE_POOL.PROJECT_ID.eq(projectId)
                                    .and(TEST_RESOURCE_POOL.ID.eq(configMap.get(poolType).toString()))
                                    .and(TEST_RESOURCE_POOL.ENABLE.eq(true)))
                            .count();
                }
                if (count == 0) {
                    configMap.remove(poolType);
                }
            }
            if (!configMap.containsKey(poolType)) {
                List<ProjectTestResourcePool> projectTestResourcePools = QueryChain.of(ProjectTestResourcePool.class)
                        .where(ProjectTestResourcePool::getProjectId).eq(projectId).list();
                if (CollectionUtils.isNotEmpty(projectTestResourcePools)) {
                    projectTestResourcePools.sort(Comparator.comparing(ProjectTestResourcePool::getTestResourcePoolId));
                    configMap.put(poolType, projectTestResourcePools.get(0).getTestResourcePoolId());
                }
            }
        }
    }

    private void doBeforeUpdate(ProjectApplication application, String currentUser) {
// todo
    }
}
