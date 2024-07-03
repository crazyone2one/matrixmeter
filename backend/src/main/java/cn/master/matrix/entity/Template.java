package cn.master.matrix.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.RelationOneToOne;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模版 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T14:28:51.717977100
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模版")
@Table("template")
public class Template implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 名称
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 是否是内置模板
     */
    @Schema(description = "是否是内置模板")
    private Boolean internal;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    @Schema(description = "创建时间")
    private LocalDateTime updateTime;

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
    @RelationOneToOne(selfField = "createUser", targetField = "id", targetTable = "user")
    private String createUser;

    /**
     * 组织或项目级别字段（PROJECT, ORGANIZATION）
     */
    @Schema(description = "组织或项目级别字段（PROJECT, ORGANIZATION）")
    private String scopeType;

    /**
     * 组织或项目ID
     */
    @Schema(description = "组织或项目ID")
    private String scopeId;

    /**
     * 是否开启api字段名配置
     */
    @Schema(description = "是否开启api字段名配置")
    private Boolean enableThirdPart;

    /**
     * 项目模板所关联的组织模板ID
     */
    @Schema(description = "项目模板所关联的组织模板ID")
    private String refId;

    /**
     * 使用场景
     */
    @Schema(description = "使用场景")
    private String scene;

}
