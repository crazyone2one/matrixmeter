package cn.master.matrix.service.impl;

import cn.master.matrix.entity.OperationHistory;
import cn.master.matrix.entity.OperationLogBlob;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.mapper.OperationHistoryMapper;
import cn.master.matrix.mapper.OperationLogBlobMapper;
import cn.master.matrix.payload.dto.LogDTO;
import cn.master.matrix.payload.dto.request.SystemOperationLogRequest;
import cn.master.matrix.payload.response.OperationLogResponse;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.matrix.entity.OperationLog;
import cn.master.matrix.mapper.OperationLogMapper;
import cn.master.matrix.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.master.matrix.entity.table.OperationLogTableDef.OPERATION_LOG;
/**
 * 操作日志 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-28T14:28:18.206553800
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
    private final OperationHistoryMapper operationHistoryMapper;
    private final OperationLogBlobMapper operationLogBlobMapper;

    @Override
    public void add(LogDTO log) {
        if (StringUtils.isBlank(log.getProjectId())) {
            log.setProjectId("none");
        }
        if (StringUtils.isBlank(log.getCreateUser())) {
            log.setCreateUser("admin");
        }
        log.setContent(subStrContent(log.getContent()));
        mapper.insert(log);
        if (log.getHistory()) {
            operationHistoryMapper.insert(getHistory(log));
        }
        operationLogBlobMapper.insert(getBlob(log));
    }

    @Override
    public void batchAdd(List<LogDTO> logs) {
        if (CollectionUtils.isEmpty(logs)) {
            return;
        }
        logs.forEach(item -> {
            item.setContent(subStrContent(item.getContent()));
            // 限制长度
            mapper.insert(item);
            if (item.getHistory()) {
                operationHistoryMapper.insert(getHistory(item));
            }
            operationLogBlobMapper.insert(getBlob(item));
        });
    }

    @Override
    public Page<OperationLogResponse> page(SystemOperationLogRequest request) {
        int compare = Long.compare(request.getStartTime(), request.getEndTime());
        if (compare > 0) {
            throw new CustomException(Translator.get("startTime_must_be_less_than_endTime"));
        }
        val queryChain = queryChain()
                .where(OPERATION_LOG.CREATE_USER.eq(request.getOperUser()))
                .and(OPERATION_LOG.CREATE_TIME.between(request.getStartTime(), request.getEndTime()));
        if (!"LOGOUT".equals(request.getLevel())&& !"LOGIN".equals(request.getLevel())) {
            queryChain.and(OPERATION_LOG.PROJECT_ID.ne("SYSTEM").when(!"SYSTEM".equals(request.getLevel())))
                    .and(OPERATION_LOG.PROJECT_ID.ne("ORGANIZATION").when("PROJECT".equals(request.getLevel())))
                    .and(OPERATION_LOG.PROJECT_ID.in(request.getProjectIds()))
                    .and(OPERATION_LOG.ORGANIZATION_ID.in(request.getOrganizationIds()))
            ;
        }
        queryChain.and(OPERATION_LOG.TYPE.eq(request.getType()));
        queryChain.and(OPERATION_LOG.MODULE.like(request.getModule()));
        queryChain.and(OPERATION_LOG.CONTENT.like(request.getContent()));
        queryChain.orderBy(OPERATION_LOG.CREATE_TIME.desc());
        return queryChain.pageAs(Page.of(request.getPageNum(), request.getPageSize()), OperationLogResponse.class);
    }

    private OperationLogBlob getBlob(LogDTO log) {
        OperationLogBlob blob = new OperationLogBlob();
        blob.setId(log.getId());
        blob.setOriginalValue(log.getOriginalValue());
        blob.setModifiedValue(log.getModifiedValue());
        return blob;
    }

    private OperationHistory getHistory(LogDTO log) {
        OperationHistory history = new OperationHistory();
        BeanUtils.copyProperties(log, history);
        return history;
    }

    private String subStrContent(String content) {
        if (StringUtils.isNotBlank(content) && content.length() > 500) {
            return content.substring(0, 499);
        }
        return content;
    }
}
