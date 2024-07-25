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
 * 测试计划关联接口用例 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-22T13:57:33.574702
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试计划关联接口用例")
@Table("test_plan_api_case")
public class TestPlanApiCase implements Serializable {

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
     * 接口用例ID
     */
    @Schema(description = "接口用例ID")
    private String apiCaseId;

    /**
     * 所属环境
     */
    @Schema(description = "所属环境")
    private String environmentId;

    /**
     * 最后执行结果
     */
    @Schema(description = "最后执行结果")
    private String lastExecResult;

    /**
     * 最后执行报告
     */
    @Schema(description = "最后执行报告")
    private String lastExecReportId;

    /**
     * 执行人
     */
    @Schema(description = "执行人")
    private String executeUser;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;

    /**
     * 自定义排序，间隔5000
     */
    @Schema(description = "自定义排序，间隔5000")
    private Long pos;

    /**
     * 最后执行时间
     */
    @Schema(description = "最后执行时间")
    private LocalDateTime lastExecTime;

    /**
     * 测试计划集id
     */
    @Schema(description = "测试计划集id")
    private String testPlanCollectionId;

}
