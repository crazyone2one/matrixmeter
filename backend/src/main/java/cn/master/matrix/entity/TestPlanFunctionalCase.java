package cn.master.matrix.entity;

import cn.master.matrix.handler.validation.Created;
import cn.master.matrix.handler.validation.Updated;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * 测试计划关联功能用例 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-22T13:57:00.180449600
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试计划关联功能用例")
@Table("test_plan_functional_case")
public class TestPlanFunctionalCase implements Serializable {

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
     * 功能用例ID
     */
    @Schema(description = "功能用例ID")
    private String functionalCaseId;

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
     * 执行人
     */
    @Schema(description = "执行人")
    private String executeUser;

    /**
     * 最后执行时间
     */
    @Schema(description = "最后执行时间")
    private LocalDateTime lastExecTime;

    /**
     * 最后执行结果
     */
    @Schema(description = "最后执行结果")
    private String lastExecResult;

    /**
     * 自定义排序，间隔5000
     */
    @Schema(description = "自定义排序，间隔5000")
    private Long pos;
    @Schema(description = "测试计划集id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_functional_case.test_plan_collection_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_functional_case.test_plan_collection_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanCollectionId;
}
