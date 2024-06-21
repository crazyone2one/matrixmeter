package cn.master.matrix.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Created by 11's papa on 06/21/2024
 **/
@Data
public class LoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
