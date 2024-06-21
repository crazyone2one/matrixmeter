package cn.master.matrix.security;

import cn.master.matrix.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Created by 11's papa on 06/21/2024
 **/
@Data
public class UserPrinciple implements UserDetails {
    private String userId;
    private String username;
    private String email;

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;
    public UserPrinciple(String id, String username, String email, String password,
                         Collection<? extends GrantedAuthority> authorities) {
        this.userId = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserPrinciple build(User user) {
        //List<GrantedAuthority> authorities = user.getRoles().stream()
        //        .map(role -> new SimpleGrantedAuthority(role.getName().name()))
        //        .collect(Collectors.toList());
        List<GrantedAuthority> authorities = new ArrayList<>();
        return new UserPrinciple(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                authorities);
    }
}
