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
 * 自定义字段 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T11:44:33.956260700
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "自定义字段")
@Table("custom_field")
public class CustomField implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自定义字段ID
     */
    @Id
    @Schema(description = "自定义字段ID")
    private String id;

    /**
     * 自定义字段名称
     */
    @Schema(description = "自定义字段名称")
    private String name;

    /**
     * 使用场景
     */
    @Schema(description = "使用场景")
    private String scene;

    /**
     * 自定义字段类型
     */
    @Schema(description = "自定义字段类型")
    private String type;

    /**
     * 自定义字段备注
     */
    @Schema(description = "自定义字段备注")
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
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;

    /**
     * 项目字段所关联的组织字段ID
     */
    @Schema(description = "项目字段所关联的组织字段ID")
    private String refId;

    /**
     * 是否需要手动输入选项key
     */
    @Schema(description = "是否需要手动输入选项key")
    private Boolean enableOptionKey;

    /**
     * 组织或项目ID
     */
    @Schema(description = "组织或项目ID")
    private String scopeId;

}
