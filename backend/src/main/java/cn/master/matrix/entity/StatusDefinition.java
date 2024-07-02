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
 * 状态定义 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T15:14:38.970197400
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "状态定义")
@Table("status_definition")
public class StatusDefinition implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 状态ID
     */
    @Schema(description = "状态ID")
    private String statusId;

    /**
     * 状态定义ID(在代码中定义)
     */
    @Schema(description = "状态定义ID(在代码中定义)")
    private String definitionId;

}
