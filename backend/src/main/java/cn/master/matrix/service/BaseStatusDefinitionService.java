package cn.master.matrix.service;

import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.StatusDefinition;

import java.util.List;

/**
 * 状态定义 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T15:14:38.970197400
 */
public interface BaseStatusDefinitionService extends IService<StatusDefinition> {

    List<StatusDefinition> getStatusDefinitions(List<String> orgStatusItemIds);

    void batchAdd(List<StatusDefinition> projectStatusDefinition);
}
