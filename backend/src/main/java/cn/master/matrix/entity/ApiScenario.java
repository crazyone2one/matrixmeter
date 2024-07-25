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
 * 场景 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-22T14:24:16.151644200
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "场景")
@Table("api_scenario")
public class ApiScenario implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id
    @Schema(description = "id")
    private String id;

    /**
     * 场景名称
     */
    @Schema(description = "场景名称")
    private String name;

    /**
     * 场景级别/P0/P1等
     */
    @Schema(description = "场景级别/P0/P1等")
    private String priority;

    /**
     * 场景状态/未规划/已完成 等
     */
    @Schema(description = "场景状态/未规划/已完成 等")
    private String status;

    /**
     * 场景步骤总数
     */
    @Schema(description = "场景步骤总数")
    private Integer stepTotal;

    /**
     * 请求通过率
     */
    @Schema(description = "请求通过率")
    private String requestPassRate;

    /**
     * 最后一次执行的结果状态
     */
    @Schema(description = "最后一次执行的结果状态")
    private String lastReportStatus;

    /**
     * 最后一次执行的报告fk
     */
    @Schema(description = "最后一次执行的报告fk")
    private String lastReportId;

    /**
     * 编号
     */
    @Schema(description = "编号")
    private Long num;

    /**
     * 删除状态
     */
    @Schema(description = "删除状态")
    private Boolean deleted;

    /**
     * 自定义排序
     */
    @Schema(description = "自定义排序")
    private Long pos;

    /**
     * 版本fk
     */
    @Schema(description = "版本fk")
    private String versionId;

    /**
     * 引用资源fk
     */
    @Schema(description = "引用资源fk")
    private String refId;

    /**
     * 是否为最新版本 0:否，1:是
     */
    @Schema(description = "是否为最新版本 0:否，1:是")
    private Boolean latest;

    /**
     * 项目fk
     */
    @Schema(description = "项目fk")
    private String projectId;

    /**
     * 场景模块fk
     */
    @Schema(description = "场景模块fk")
    private String moduleId;

    /**
     * 描述信息
     */
    @Schema(description = "描述信息")
    private String description;

    /**
     * 标签
     */
    @Schema(description = "标签")
    private String tags;

    /**
     * 是否为环境组
     */
    @Schema(description = "是否为环境组")
    private Boolean grouped;

    /**
     * 环境或者环境组ID
     */
    @Schema(description = "环境或者环境组ID")
    private String environmentId;

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
     * 删除时间
     */
    @Schema(description = "删除时间")
    private LocalDateTime deleteTime;

    /**
     * 删除人
     */
    @Schema(description = "删除人")
    private String deleteUser;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    private String updateUser;

    /**
     * 更新时间
     */
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
