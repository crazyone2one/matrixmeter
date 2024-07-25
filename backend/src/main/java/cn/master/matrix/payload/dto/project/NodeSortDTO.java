package cn.master.matrix.payload.dto.project;

import cn.master.matrix.payload.dto.BaseModule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeSortDTO {

    @Schema(description = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{file_module.not.exist}")
    private BaseModule node;

    @Schema(description = "父模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{file_module.parent.not.exist}")
    private BaseModule parent;

    @Schema(description = "前一个节点")
    private BaseModule previousNode;

    @Schema(description = "后一个节点")
    private BaseModule nextNode;
}

