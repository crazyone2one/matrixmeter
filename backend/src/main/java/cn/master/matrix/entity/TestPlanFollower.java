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
 * 测试计划关注人 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-22T13:24:38.556589300
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试计划关注人")
@Table("test_plan_follower")
public class TestPlanFollower implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 测试计划ID;联合主键
     */
    @Schema(description = "测试计划ID;联合主键")
    private String testPlanId;

    /**
     * 用户ID;联合主键
     */
    @Schema(description = "用户ID;联合主键")
    private String userId;

}
