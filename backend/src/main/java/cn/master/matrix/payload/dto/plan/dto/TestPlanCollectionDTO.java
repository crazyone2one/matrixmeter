package cn.master.matrix.payload.dto.plan.dto;

import cn.master.matrix.entity.TestPlanCollection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class TestPlanCollectionDTO extends TestPlanCollection {

    @Schema(description = "测试子集")
    private List<TestPlanCollectionDTO> children;
}
