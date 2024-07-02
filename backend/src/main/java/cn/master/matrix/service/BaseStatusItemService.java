package cn.master.matrix.service;

import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.StatusItem;

import java.util.List;

/**
 * 状态流的状态项 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T15:12:57.148264800
 */
public interface BaseStatusItemService extends IService<StatusItem> {

    List<StatusItem> getStatusItems(String scopeId, String scene);

    List<StatusItem> batchAdd(List<StatusItem> projectStatusItems);
}
