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
 * 功能用例 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-22T14:17:59.493080100
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "功能用例")
@Table("functional_case")
public class FunctionalCase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 业务ID
     */
    @Schema(description = "业务ID")
    private Long num;

    /**
     * 模块ID
     */
    @Schema(description = "模块ID")
    private String moduleId;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private String projectId;

    /**
     * 模板ID
     */
    @Schema(description = "模板ID")
    private String templateId;

    /**
     * 名称
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 评审结果：未评审/评审中/通过/不通过/重新提审
     */
    @Schema(description = "评审结果：未评审/评审中/通过/不通过/重新提审")
    private String reviewStatus;

    /**
     * 标签（JSON)
     */
    @Schema(description = "标签（JSON)")
    private String tags;

    /**
     * 编辑模式：步骤模式/文本模式
     */
    @Schema(description = "编辑模式：步骤模式/文本模式")
    private String caseEditType;

    /**
     * 自定义排序，间隔5000
     */
    @Schema(description = "自定义排序，间隔5000")
    private Long pos;

    /**
     * 版本ID
     */
    @Schema(description = "版本ID")
    private String versionId;

    /**
     * 指向初始版本ID
     */
    @Schema(description = "指向初始版本ID")
    private String refId;

    /**
     * 最近的执行结果：未执行/通过/失败/阻塞/跳过
     */
    @Schema(description = "最近的执行结果：未执行/通过/失败/阻塞/跳过")
    private String lastExecuteResult;

    /**
     * 是否在回收站：0-否，1-是
     */
    @Schema(description = "是否在回收站：0-否，1-是")
    private Boolean deleted;

    /**
     * 是否是公共用例：0-否，1-是
     */
    @Schema(description = "是否是公共用例：0-否，1-是")
    private Boolean publicCase;

    /**
     * 是否为最新版本：0-否，1-是
     */
    @Schema(description = "是否为最新版本：0-否，1-是")
    private Boolean latest;

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

    /**
     * 删除人
     */
    @Schema(description = "删除人")
    private String deleteUser;

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
     * 删除时间
     */
    @Schema(description = "删除时间")
    private LocalDateTime deleteTime;

}
