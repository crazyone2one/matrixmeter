package cn.master.matrix.payload.response;

import lombok.Data;

import java.util.List;

/**
 * @author Created by 11's papa on 06/21/2024
 **/
@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String id;
    private String username;
    private String email;
    private List<String> roles;

    public JwtResponse(String accessToken, String id, String username, String email, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}
