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
 * 功能用例执行历史表 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-22T16:24:52.680104900
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "功能用例执行历史表")
@Table("test_plan_case_execute_history")
public class TestPlanCaseExecuteHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 计划关联用例表ID
     */
    @Schema(description = "计划关联用例表ID")
    private String testPlanCaseId;

    /**
     * 测试计划id
     */
    @Schema(description = "测试计划id")
    private String testPlanId;

    /**
     * 用例ID
     */
    @Schema(description = "用例ID")
    private String caseId;

    /**
     * 执行结果：成功/失败/阻塞
     */
    @Schema(description = "执行结果：成功/失败/阻塞")
    private String status;

    /**
     * 执行评论意见
     */
    @Schema(description = "执行评论意见")
    private byte[] content;

    /**
     * 用例步骤执行记录（JSON)，step_model 为 Step 时启用
     */
    @Schema(description = "用例步骤执行记录（JSON)，step_model 为 Step 时启用")
    private byte[] steps;

    /**
     * 是否是取消关联或执行被删除的：0-否，1-是
     */
    @Schema(description = "是否是取消关联或执行被删除的：0-否，1-是")
    private Boolean deleted;

    /**
     * 通知人
     */
    @Schema(description = "通知人")
    private String notifier;

    /**
     * 操作人
     */
    @Schema(description = "操作人")
    private String createUser;

    /**
     * 操作时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "操作时间")
    private LocalDateTime createTime;

}
