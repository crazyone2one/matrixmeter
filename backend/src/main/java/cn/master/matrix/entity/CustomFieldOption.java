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
 * 自定义字段选项 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T11:45:38.767556500
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "自定义字段选项")
@Table("custom_field_option")
public class CustomFieldOption implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 自定义字段ID
     */
    @Schema(description = "自定义字段ID")
    private String fieldId;

    /**
     * 选项值
     */
    @Schema(description = "选项值")
    private String value;

    /**
     * 选项值名称
     */
    @Schema(description = "选项值名称")
    private String text;

    /**
     * 是否内置
     */
    @Schema(description = "是否内置")
    private Boolean internal;

    /**
     * 自定义排序，间隔1
     */
    @Schema(description = "自定义排序，间隔1")
    private Integer pos;

}
