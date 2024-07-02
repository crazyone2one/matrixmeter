package cn.master.matrix.payload.dto.request.user;

import cn.master.matrix.payload.dto.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Created by 11's papa on 06/28/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class UserRoleBatchRelationRequest extends TableBatchProcessDTO {
    /**
     * 权限ID集合
     */
    @Schema(description = "权限ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{organization.id.not_blank}")
    private List<String> roleIds;
}
