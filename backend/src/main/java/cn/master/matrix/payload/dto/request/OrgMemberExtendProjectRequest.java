package cn.master.matrix.payload.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class OrgMemberExtendProjectRequest extends OrganizationMemberRequestByOrg{

    /**
     * 项目ID集合
     */
    @Schema(description =  "项目ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{projectIds.not_empty}")
    private List<String> projectIds;

}
