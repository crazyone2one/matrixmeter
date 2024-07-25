package cn.master.matrix.service.plan;

import cn.master.matrix.payload.dto.plan.dto.TestPlanCollectionDTO;
import cn.master.matrix.payload.dto.plan.request.TestPlanBatchProcessRequest;
import cn.master.matrix.payload.dto.plan.request.TestPlanBatchRequest;
import cn.master.matrix.payload.dto.plan.request.TestPlanCreateRequest;
import cn.master.matrix.payload.dto.plan.request.TestPlanTableRequest;
import cn.master.matrix.payload.dto.plan.response.TestPlanDetailResponse;
import cn.master.matrix.payload.dto.plan.response.TestPlanResponse;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.TestPlan;

import java.util.List;
import java.util.Map;

/**
 * 测试计划 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-19T14:49:06.626698900
 */
public interface TestPlanService extends IService<TestPlan> {

    Page<TestPlanResponse> page(TestPlanTableRequest request);

    TestPlan save(TestPlanCreateRequest request, String userId, String requestUrl, String requestMethod);

    void checkModule(String moduleId);

    List<TestPlanCollectionDTO> initDefaultPlanCollection(String planId, String currentUser);

    List<TestPlan> groupList(String projectId);

    List<TestPlanResponse> selectByGroupId(String groupId);

    Map<String, Long> moduleCount(TestPlanTableRequest request);

    void delete(String id, String operator, String requestUrl, String requestMethod);

    void editFollower(String testPlanId, String userId);

    void archived(String id, String userId);

    TestPlanDetailResponse detail(String id, String userId);

    void batchDelete(TestPlanBatchProcessRequest request, String operator, String requestUrl, String requestMethod);

    String copy(String id, String userId);

    void filterArchivedIds(TestPlanBatchRequest request);

    long batchCopy(TestPlanBatchRequest request, String userId, String url, String method);
}
