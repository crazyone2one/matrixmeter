package cn.master.matrix.payload.dto.plan.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Created by 11's papa on 07/24/2024
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestPlanOperationResponse {
    @Schema(description = "处理成功的数量")
    private long operationCount;
}
