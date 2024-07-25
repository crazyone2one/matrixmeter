package cn.master.matrix.payload.dto.plan.request;

import cn.master.matrix.constants.ModuleConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Created by 11's papa on 07/24/2024
 **/
@Data
public class TestPlanBatchRequest extends TestPlanBatchProcessRequest {

    @Schema(description = "目标ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.target_id.not_blank}")
    private String targetId;

    @Schema(description = "移动类型 （MODULE / GROUP)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moveType = ModuleConstants.NODE_TYPE_DEFAULT;
}
