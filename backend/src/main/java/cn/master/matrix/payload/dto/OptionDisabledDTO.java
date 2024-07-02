package cn.master.matrix.payload.dto;

import cn.master.matrix.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptionDisabledDTO extends User {
    @Schema(description =  "是否已经关联过")
    private Boolean disabled = false;
}
