package cn.master.matrix.controller;

import cn.master.matrix.payload.dto.request.SystemOperationLogRequest;
import cn.master.matrix.payload.response.OperationLogResponse;
import com.mybatisflex.core.paginate.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import cn.master.matrix.entity.OperationLog;
import cn.master.matrix.service.OperationLogService;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.io.Serializable;
import java.util.List;

/**
 * 操作日志 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-28T14:28:18.206553800
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "系统设置-系统-日志")
@RequestMapping("/operation/log")
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping("page")
    @Operation(description="分页查询操作日志")
    public Page<OperationLogResponse> page(@Validated @RequestBody SystemOperationLogRequest request) {
        return operationLogService.page(request);
    }

}
