package cn.master.matrix.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.mybatisflex.core.handler.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.List;

/**
 * 测试计划 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-19T14:49:06.626698900
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试计划")
@Table("test_plan")
public class TestPlan implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * num
     */
    @Schema(description = "num")
    private Long num;

    /**
     * 测试计划所属项目
     */
    @Schema(description = "测试计划所属项目")
    private String projectId;

    /**
     * 测试计划组ID;默认为none.只关联type为group的测试计划
     */
    @Schema(description = "测试计划组ID;默认为none.只关联type为group的测试计划")
    private String groupId;

    /**
     * 测试计划模块ID
     */
    @Schema(description = "测试计划模块ID")
    private String moduleId;

    /**
     * 测试计划名称
     */
    @Schema(description = "测试计划名称")
    private String name;

    /**
     * 测试计划状态;未开始，进行中，已完成，已归档
     */
    @Schema(description = "测试计划状态;未开始，进行中，已完成，已归档")
    private String status;

    /**
     * 数据类型;测试计划组（group）/测试计划（testPlan）
     */
    @Schema(description = "数据类型;测试计划组（group）/测试计划（testPlan）")
    private String type;

    /**
     * 标签
     */
    @Schema(description = "标签")
    @Column(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

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
     * 更新时间
     */
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    private String updateUser;

    /**
     * 计划开始时间
     */
    @Schema(description = "计划开始时间")
    private LocalDateTime plannedStartTime;

    /**
     * 计划结束时间
     */
    @Schema(description = "计划结束时间")
    private LocalDateTime plannedEndTime;

    /**
     * 实际开始时间
     */
    @Schema(description = "实际开始时间")
    private LocalDateTime actualStartTime;

    /**
     * 实际结束时间
     */
    @Schema(description = "实际结束时间")
    private LocalDateTime actualEndTime;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 自定义排序
     */
    @Schema(description = "自定义排序")
    private Long pos;

}
