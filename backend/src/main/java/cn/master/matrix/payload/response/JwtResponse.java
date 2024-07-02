package cn.master.matrix.payload.response;

import cn.master.matrix.payload.dto.user.UserDTO;
import lombok.Data;

import java.util.List;

/**
 * @author Created by 11's papa on 06/21/2024
 **/
@Data
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private UserDTO user;

    public JwtResponse(String accessToken, String refreshToken, UserDTO userDTO) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = userDTO;
    }
}
