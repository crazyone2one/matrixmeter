package cn.master.matrix.entity.dto.user;

import cn.master.matrix.entity.UserRole;
import cn.master.matrix.entity.UserRoleRelation;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by 11's papa on 06/27/2024
 **/
@Data
public class UserRolePermissionDTO {
    List<UserRoleResourceDTO> list = new ArrayList<>();
    List<UserRole> userRoles = new ArrayList<>();
    List<UserRoleRelation> userRoleRelations = new ArrayList<>();
}
