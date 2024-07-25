package cn.master.matrix.handler.converter;

import cn.master.matrix.entity.TestPlan;
import cn.master.matrix.payload.dto.plan.request.TestPlanCreateRequest;
import org.mapstruct.factory.Mappers;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
public interface TestPlanConverter {
    TestPlanConverter INSTANCE = Mappers.getMapper(TestPlanConverter.class);

    TestPlan toTestPlan(TestPlanCreateRequest request);
}
