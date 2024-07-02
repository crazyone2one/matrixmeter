package cn.master.matrix.entity;

import com.mybatisflex.annotation.Column;
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
 * 版本管理 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T14:11:29.264113700
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "版本管理")
@Table("project_version")
public class ProjectVersion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 版本ID
     */
    @Id
    @Schema(description = "版本ID")
    private String id;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private String projectId;

    /**
     * 版本名称
     */
    @Schema(description = "版本名称")
    private String name;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private String status;

    /**
     * 是否是最新版
     */
    @Schema(description = "是否是最新版")
    private Boolean latest;

    /**
     * 发布时间
     */
    @Schema(description = "发布时间")
    private Long publishTime;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private Long startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private Long endTime;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "创建时间")
    private Long createTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;

}
