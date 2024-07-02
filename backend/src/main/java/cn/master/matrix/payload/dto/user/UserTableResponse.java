package cn.master.matrix.payload.dto.user;

import cn.master.matrix.entity.Organization;
import cn.master.matrix.entity.User;
import cn.master.matrix.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by 11's papa on 06/28/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class UserTableResponse extends User {
    @Schema(description = "用户所属组织")
    private List<Organization> organizationList = new ArrayList<>();
    @Schema(description = "用户所属用户组")
    private List<UserRole> userRoleList = new ArrayList<>();
}
