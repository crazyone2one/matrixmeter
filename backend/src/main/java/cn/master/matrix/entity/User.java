package cn.master.matrix.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-21T10:54:08.016115500
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Id
    private String id;

    /**
     * 用户名
     */
    private String name;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 是否启用
     */
    private Boolean enable;


    /**
     * 语言
     */
    private String language;

    /**
     * 当前组织ID
     */
    private String lastOrganizationId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 来源：LOCAL OIDC CAS OAUTH2
     */
    private String source;

    /**
     * 当前项目ID
     */
    private String lastProjectId;
    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 修改人
     */
    private String updateUser;

    /**
     * 是否删除
     */
    private Boolean deleted;

}
