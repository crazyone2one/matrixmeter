package cn.master.matrix.payload.dto.project.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectApplicationRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project_application.project_id.not_blank}")
    private String projectId;

    @Schema(description = "配置类型列表", allowableValues = {"workstation", "testPlan", "bugManagement", "caseManagement", "apiTest", "uiTest", "loadTest"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;
}
