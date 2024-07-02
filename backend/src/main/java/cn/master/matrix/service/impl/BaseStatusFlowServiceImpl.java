package cn.master.matrix.service.impl;

import cn.master.matrix.entity.StatusFlow;
import cn.master.matrix.mapper.StatusFlowMapper;
import cn.master.matrix.service.BaseStatusFlowService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 状态流转 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T15:15:03.084699400
 */
@Service
public class BaseStatusFlowServiceImpl extends ServiceImpl<StatusFlowMapper, StatusFlow> implements BaseStatusFlowService {

    @Override
    public List<StatusFlow> getStatusFlows(List<String> statusIds) {
        if (CollectionUtils.isEmpty(statusIds)) {
            return List.of();
        }
        return queryChain().where(StatusFlow::getFromId).in(statusIds)
                .or(StatusFlow::getToId).notIn(statusIds).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAdd(List<StatusFlow> statusFlows) {
        if (CollectionUtils.isEmpty(statusFlows)) {
            return;
        }
        mapper.insertBatch(statusFlows);
    }
}
