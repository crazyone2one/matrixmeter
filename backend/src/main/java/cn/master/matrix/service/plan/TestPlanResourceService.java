package cn.master.matrix.service.plan;

import cn.master.matrix.payload.dto.plan.dto.TestPlanCollectionDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
public abstract class TestPlanResourceService extends TestPlanSortService {
    public static final String MODULE_ALL = "all";
    public abstract void deleteBatchByTestPlanId(List<String> testPlanIdList);
    public abstract Map<String, Long> caseExecResultCount(String testPlanId);
    public abstract void initResourceDefaultCollection(String planId, List<TestPlanCollectionDTO> defaultCollections);

    public abstract long copyResource(String originalTestPlanId, String newTestPlanId, Map<String, String> oldCollectionIdToNewCollectionId, String operator, LocalDateTime operatorTime);
}
