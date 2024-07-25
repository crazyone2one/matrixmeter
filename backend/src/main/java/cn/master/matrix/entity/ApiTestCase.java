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
 * 接口用例 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-22T14:22:45.489615300
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "接口用例")
@Table("api_test_case")
public class ApiTestCase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 接口用例pk
     */
    @Id
    @Schema(description = "接口用例pk")
    private String id;

    /**
     * 接口用例名称
     */
    @Schema(description = "接口用例名称")
    private String name;

    /**
     * 用例等级
     */
    @Schema(description = "用例等级")
    private String priority;

    /**
     * 接口用例编号id
     */
    @Schema(description = "接口用例编号id")
    private Long num;

    /**
     * 标签
     */
    @Schema(description = "标签")
    private String tags;

    /**
     * 用例状态
     */
    @Schema(description = "用例状态")
    private String status;

    /**
     * 最新执行结果状态
     */
    @Schema(description = "最新执行结果状态")
    private String lastReportStatus;

    /**
     * 最后执行结果报告fk
     */
    @Schema(description = "最后执行结果报告fk")
    private String lastReportId;

    /**
     * 自定义排序
     */
    @Schema(description = "自定义排序")
    private Long pos;

    /**
     * 项目fk
     */
    @Schema(description = "项目fk")
    private String projectId;

    /**
     * 接口fk
     */
    @Schema(description = "接口fk")
    private String apiDefinitionId;

    /**
     * 版本fk
     */
    @Schema(description = "版本fk")
    private String versionId;

    /**
     * 环境fk
     */
    @Schema(description = "环境fk")
    private String environmentId;

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
     * 删除标识
     */
    @Schema(description = "删除标识")
    private Boolean deleted;

}
