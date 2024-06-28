package cn.master.matrix.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * 用户组权限 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-27T16:23:10.923916400
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户组权限")
@Table("user_role_permission")
public class UserRolePermission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Schema(description = "")
    private String id;

    /**
     * 用户组ID
     */
    @Schema(description = "用户组ID")
    private String roleId;

    /**
     * 权限ID
     */
    @Schema(description = "权限ID")
    private String permissionId;

}
