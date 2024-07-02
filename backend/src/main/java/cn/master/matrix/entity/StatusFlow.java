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
 * 状态流转 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T15:15:03.084699400
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "状态流转")
@Table("status_flow")
public class StatusFlow implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 起始状态ID
     */
    @Schema(description = "起始状态ID")
    private String fromId;

    /**
     * 目的状态ID
     */
    @Schema(description = "目的状态ID")
    private String toId;

}
