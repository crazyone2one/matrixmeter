package cn.master.matrix.payload.dto.user;

import cn.master.matrix.entity.UserRole;
import cn.master.matrix.entity.UserRoleRelation;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by 11's papa on 06/28/2024
 **/
@Data
public class UserDTO {
    private String id;
    private String name;
    private String email;
    private String lastOrganizationId;
    private String lastProjectId;
    private Boolean enable;
    private List<UserRole> userRoles = new ArrayList<>();
    private List<UserRoleRelation> userRoleRelations = new ArrayList<>();
    private List<UserRoleResourceDTO> userRolePermissions = new ArrayList<>();
}
