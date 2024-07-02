package cn.master.matrix.payload.dto.request.user;

import cn.master.matrix.payload.dto.ExcludeOptionDTO;
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
public class UserExcludeOptionDTO extends ExcludeOptionDTO {
    @Schema(description =  "邮箱")
    private String email;
}
