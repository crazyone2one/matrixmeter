package cn.master.matrix.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * 测试计划报告 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-22T11:36:13.643773700
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试计划报告")
@Table("test_plan_report")
public class TestPlanReport implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 测试计划ID
     */
    @Schema(description = "测试计划ID")
    private String testPlanId;

    /**
     * 报告名称
     */
    @Schema(description = "报告名称")
    private String name;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 执行时间;计划真正执行的时间
     */
    @Schema(description = "执行时间;计划真正执行的时间")
    private LocalDateTime executeTime;

    /**
     * 开始时间;计划开始执行的时间
     */
    @Schema(description = "开始时间;计划开始执行的时间")
    private LocalDateTime startTime;

    /**
     * 结束时间;计划结束执行的时间
     */
    @Schema(description = "结束时间;计划结束执行的时间")
    private LocalDateTime endTime;

    /**
     * 执行状态
     */
    @Schema(description = "执行状态")
    private String execStatus;

    /**
     * 结果状态
     */
    @Schema(description = "结果状态")
    private String resultStatus;

    /**
     * 通过率
     */
    @Schema(description = "通过率")
    private BigDecimal passRate;

    /**
     * 触发类型
     */
    @Schema(description = "触发类型")
    private String triggerMode;

    /**
     * 通过阈值
     */
    @Schema(description = "通过阈值")
    private BigDecimal passThreshold;

    /**
     * 项目id
     */
    @Schema(description = "项目id")
    private String projectId;

    /**
     * 是否是集成报告
     */
    @Schema(description = "是否是集成报告")
    private Boolean integrated;

    /**
     * 是否删除
     */
    @Schema(description = "是否删除")
    private Boolean deleted;

}
