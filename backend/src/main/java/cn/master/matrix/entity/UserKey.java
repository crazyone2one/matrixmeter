package cn.master.matrix.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户api key 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-21T15:31:28.446710600
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_key")
public class UserKey implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * user_key ID
     */
    @Id
    @Schema(description = "user_key ID")
    private String id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private String userId;

    /**
     * access_key
     */
    private String accessKey;

    /**
     * secret key
     */

    private String secretKey;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    /**
     * 状态
     */
    private Boolean enable;

    /**
     * 是否永久有效
     */
    private Boolean forever;

    /**
     * 到期时间
     */
    private LocalDateTime expireTime;
    private String description;

}
