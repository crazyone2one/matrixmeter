package cn.master.matrix.security;

import cn.master.matrix.entity.User;
import cn.master.matrix.entity.UserRole;
import cn.master.matrix.service.UserRolePermissionService;
import com.mybatisflex.core.query.QueryChain;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author Created by 11's papa on 06/21/2024
 **/
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRolePermissionService userRolePermissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        val user = QueryChain.of(User.class)
                .where(User::getName).eq(username)
                .oneOpt()
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with -> username or email: " + username));
        val userRolePermission = userRolePermissionService.getUserRolePermission(user.getId());
        val userRoles = userRolePermission.getUserRoles();
        val userRoleIds = userRoles.stream().map(UserRole::getId).toList();
        return new UserPrinciple(user, userRoleIds);
    }
}
