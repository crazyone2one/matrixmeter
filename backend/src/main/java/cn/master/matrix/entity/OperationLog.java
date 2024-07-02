package cn.master.matrix.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * 操作日志 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-28T14:28:18.206553800
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "操作日志")
@Table("operation_log")
public class OperationLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id
    @Schema(description = "主键")
    private String id;

    /**
     * 项目id
     */
    @Schema(description = "项目id")
    private String projectId;

    /**
     * 组织id
     */
    @Schema(description = "组织id")
    private String organizationId;

    /**
     * 操作时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "操作时间")
    private LocalDateTime createTime;

    /**
     * 操作人
     */
    @Schema(description = "操作人")
    private String createUser;

    /**
     * 资源id
     */
    @Schema(description = "资源id")
    private String sourceId;

    /**
     * 操作方法
     */
    @Schema(description = "操作方法")
    private String method;

    /**
     * 操作类型/add/update/delete
     */
    @Schema(description = "操作类型/add/update/delete")
    private String type;

    @Schema(description = "")
    private String module;

    /**
     * 操作详情
     */
    @Schema(description = "操作详情")
    private String content;

    /**
     * 操作路径
     */
    @Schema(description = "操作路径")
    private String path;

}
