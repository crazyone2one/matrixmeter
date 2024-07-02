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
 * 状态流的状态项 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T15:12:57.148264800
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "状态流的状态项")
@Table("status_item")
public class StatusItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态ID
     */
    @Id
    @Schema(description = "状态ID")
    private String id;

    /**
     * 状态名称
     */
    @Schema(description = "状态名称")
    private String name;

    /**
     * 使用场景
     */
    @Schema(description = "使用场景")
    private String scene;

    /**
     * 状态说明
     */
    @Schema(description = "状态说明")
    private String remark;

    /**
     * 是否是内置字段
     */
    @Schema(description = "是否是内置字段")
    private Boolean internal;

    /**
     * 组织或项目级别字段（PROJECT, ORGANIZATION）
     */
    @Schema(description = "组织或项目级别字段（PROJECT, ORGANIZATION）")
    private String scopeType;

    /**
     * 项目状态所关联的组织状态ID
     */
    @Schema(description = "项目状态所关联的组织状态ID")
    private String refId;

    /**
     * 组织或项目ID
     */
    @Schema(description = "组织或项目ID")
    private String scopeId;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段")
    private Integer pos;

}
