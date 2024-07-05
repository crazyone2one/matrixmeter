package cn.master.matrix.payload.response;

import cn.master.matrix.payload.dto.user.UserDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 06/21/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class JwtResponse extends UserDTO {

    private String accessToken;
    private String refreshToken;

    public JwtResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
