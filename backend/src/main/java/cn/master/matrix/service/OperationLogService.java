package cn.master.matrix.service;

import cn.master.matrix.payload.dto.LogDTO;
import cn.master.matrix.payload.dto.request.SystemOperationLogRequest;
import cn.master.matrix.payload.response.OperationLogResponse;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.OperationLog;

import java.util.List;

/**
 * 操作日志 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-28T14:28:18.206553800
 */
public interface OperationLogService extends IService<OperationLog> {
    void add(LogDTO log);

    void batchAdd(List<LogDTO> logs);

    Page<OperationLogResponse> page(SystemOperationLogRequest request);
}
