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
 * 操作日志内容详情 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-28T14:30:22.977983200
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "操作日志内容详情")
@Table("operation_log_blob")
public class OperationLogBlob implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键,与operation_log表id一致
     */
    @Id
    @Schema(description = "主键,与operation_log表id一致")
    private String id;

    /**
     * 变更前内容
     */
    @Schema(description = "变更前内容")
    private byte[] originalValue;

    /**
     * 变更后内容
     */
    @Schema(description = "变更后内容")
    private byte[] modifiedValue;

}
