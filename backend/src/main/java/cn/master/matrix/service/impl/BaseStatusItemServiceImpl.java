package cn.master.matrix.service.impl;

import cn.master.matrix.entity.StatusItem;
import cn.master.matrix.mapper.StatusItemMapper;
import cn.master.matrix.service.BaseStatusItemService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 状态流的状态项 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T15:12:57.148264800
 */
@Service
public class BaseStatusItemServiceImpl extends ServiceImpl<StatusItemMapper, StatusItem> implements BaseStatusItemService {

    @Override
    public List<StatusItem> getStatusItems(String scopeId, String scene) {
        return queryChain()
                .where(StatusItem::getScopeId).eq(scopeId)
                .and(StatusItem::getScene).eq(scene)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<StatusItem> batchAdd(List<StatusItem> statusItems) {
        if (CollectionUtils.isEmpty(statusItems)) {
            return List.of();
        }
        val byScopeIdAndScene = getStatusItems(statusItems.get(0).getScopeId(), statusItems.get(0).getScene());
        assert byScopeIdAndScene != null;
        int pos = byScopeIdAndScene.size();
        for (StatusItem statusItem : statusItems) {
            // 设置排序
            statusItem.setPos(pos++);
        }
        mapper.insertBatch(statusItems);
        return statusItems;
    }
}
