package cn.master.matrix.payload.dto;

import cn.master.matrix.entity.OperationLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author Created by 11's papa on 06/28/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class LogDTO extends OperationLog {
    @Schema(description = "变更前内容")
    private byte[] originalValue;

    @Schema(description = "变更后内容")
    private byte[] modifiedValue;

    @Schema(description = "是否需要历史记录")
    private Boolean history = false;

    public LogDTO() {
    }

    public LogDTO(String projectId, String organizationId, String sourceId, String createUser, String type, String module, String content) {
        this.setProjectId(projectId);
        this.setOrganizationId(organizationId);
        this.setSourceId(sourceId);
        this.setCreateUser(createUser);
        this.setType(type);
        this.setModule(module);
        this.setContent(content);
        this.setCreateTime(LocalDateTime.now());
    }
}
