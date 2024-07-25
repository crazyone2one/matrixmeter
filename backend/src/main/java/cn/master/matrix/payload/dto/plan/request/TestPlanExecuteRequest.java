package cn.master.matrix.payload.dto.plan.request;

import cn.master.matrix.constants.ApiBatchRunMode;
import cn.master.matrix.constants.TaskTriggerMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
@Data
public class TestPlanExecuteRequest {

    @Schema(description = "执行ID")
    @NotBlank(message = "test_plan.not.exist")
    private String executeId;

    @Schema(description = "执行模式", allowableValues = {"SERIAL", "PARALLEL"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String runMode = ApiBatchRunMode.SERIAL.name();

    @Schema(description = "执行来源", allowableValues = {"MANUAL", "RUN", "SCHEDULE"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String executionSource = TaskTriggerMode.MANUAL.name();

}
