package cn.master.matrix.payload.dto.project;

import cn.master.matrix.entity.Template;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Created by 11's papa on 07/19/2024
 **/
@Setter
@Getter
public class ProjectTemplateDTO extends Template implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "是否是默认模板")
    private Boolean enableDefault = false;

    @Schema(description = "是否是平台自动获取模板")
    private Boolean enablePlatformDefault = false;
}
