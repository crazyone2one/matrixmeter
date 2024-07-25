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
 * 测试计划模块 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-19T15:14:20.016505100
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试计划模块")
@Table("test_plan_module")
public class TestPlanModule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 项目名称
     */
    @Schema(description = "项目名称")
    private String projectId;

    @Schema(description = "")
    private String name;

    /**
     * 模块ID
     */
    @Schema(description = "模块ID")
    private String parentId;

    /**
     * 排序标识
     */
    @Schema(description = "排序标识")
    private Long pos;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    private String updateUser;

}
