package cn.master.matrix.payload.dto.request.user;

import cn.master.matrix.payload.dto.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 06/28/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class UserChangeEnableRequest extends TableBatchProcessDTO {
    @Schema(description = "禁用/启用", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean enable;
}
