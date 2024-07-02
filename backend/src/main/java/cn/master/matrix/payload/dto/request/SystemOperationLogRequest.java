package cn.master.matrix.payload.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class SystemOperationLogRequest extends BaseOperationLogRequest {

    @Schema(description =  "项目id")
    private List<String> projectIds;

    @Schema(description =  "组织id")
    private List<String> organizationIds;

}
