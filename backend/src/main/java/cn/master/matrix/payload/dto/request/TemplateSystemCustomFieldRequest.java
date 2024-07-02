package cn.master.matrix.payload.dto.request;

import cn.master.matrix.handler.validation.Created;
import cn.master.matrix.handler.validation.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Data
public class TemplateSystemCustomFieldRequest {

    @Schema(title = "字段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field.id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{template_custom_field.field_id.length_range}", groups = {Created.class, Updated.class})
    private String fieldId;

    @Schema(title = "默认值")
    private Object defaultValue;
}
