package cn.master.matrix.security;

import cn.master.matrix.entity.UserKey;
import com.mybatisflex.core.update.UpdateChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author Created by 11's papa on 06/21/2024
 **/
@Component
public class CustomLogoutHandler implements LogoutHandler {
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String headerAuth = request.getHeader("Authorization");

        if (Objects.isNull(headerAuth) || !headerAuth.startsWith("Bearer ")) {
            return;
        }
        String token = headerAuth.substring(7);
        UpdateChain.of(UserKey.class)
                .set(UserKey::getEnable, false)
                .where(UserKey::getAccessKey).eq(token)
                .update();
    }
}
