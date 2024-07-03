package cn.master.matrix.payload.dto;

import cn.master.matrix.entity.TestResourcePool;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Created by 11's papa on 07/03/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectResourcePoolDTO extends TestResourcePool implements Serializable {
    @Schema(description =  "项目ID")
    private String projectId;
    @Schema(description =  "资源池ID")
    private String id;

    @Schema(description =  "资源池名称")
    private String name;

    @Serial
    private static final long serialVersionUID = 1L;
}

