package cn.master.matrix.service.impl;

import cn.master.matrix.entity.StatusDefinition;
import cn.master.matrix.mapper.StatusDefinitionMapper;
import cn.master.matrix.service.BaseStatusDefinitionService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 状态定义 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T15:14:38.970197400
 */
@Service
public class BaseStatusDefinitionServiceImpl extends ServiceImpl<StatusDefinitionMapper, StatusDefinition> implements BaseStatusDefinitionService {

    @Override
    public List<StatusDefinition> getStatusDefinitions(List<String> statusIds) {
        if (CollectionUtils.isEmpty(statusIds)) {
            return List.of();
        }
        return queryChain().where(StatusDefinition::getStatusId).in(statusIds).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAdd(List<StatusDefinition> statusDefinitions) {
        if (CollectionUtils.isEmpty(statusDefinitions)) {
            return;
        }
        mapper.insertBatch(statusDefinitions);
    }
}
