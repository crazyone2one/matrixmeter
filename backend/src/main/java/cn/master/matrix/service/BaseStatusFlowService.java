package cn.master.matrix.service;

import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.StatusFlow;

import java.util.List;

/**
 * 状态流转 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T15:15:03.084699400
 */
public interface BaseStatusFlowService extends IService<StatusFlow> {

    List<StatusFlow> getStatusFlows(List<String> statusIds);

    void batchAdd(List<StatusFlow> statusFlows);
}
