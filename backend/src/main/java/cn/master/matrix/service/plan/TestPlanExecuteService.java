package cn.master.matrix.service.plan;

import cn.master.matrix.constants.TestPlanConstants;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.handler.uid.IDGenerator;
import cn.master.matrix.mapper.TestPlanMapper;
import cn.master.matrix.payload.dto.plan.request.TestPlanExecuteRequest;
import cn.master.matrix.payload.dto.queue.TestPlanExecutionQueue;
import cn.master.matrix.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.master.matrix.service.plan.TestPlanExecuteSupportService.QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class TestPlanExecuteService {
    private final TestPlanExecuteSupportService testPlanExecuteSupportService;
    private final TestPlanMapper testPlanMapper;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED)
    public String singleExecuteTestPlan(TestPlanExecuteRequest request, String reportId, String userId) {
        String queueId = IDGenerator.nextStr();
        TestPlanExecutionQueue singleExecuteRootQueue = new TestPlanExecutionQueue(
                0,
                userId,
                System.currentTimeMillis(),
                queueId,
                QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE,
                null,
                null,
                request.getExecuteId(),
                request.getRunMode(),
                request.getExecutionSource(),
                reportId
        );

        testPlanExecuteSupportService.setRedisForList(
                testPlanExecuteSupportService.genQueueKey(queueId, QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE), List.of(JsonUtils.toJsonString(singleExecuteRootQueue)));
        TestPlanExecutionQueue nextQueue = testPlanExecuteSupportService.getNextQueue(queueId, QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE);
        log.info("测试计划（组）的单独执行start！计划报告[{}] , 资源ID[{}]", singleExecuteRootQueue.getPrepareReportId(), singleExecuteRootQueue.getSourceID());
        executeTestPlanOrGroup(nextQueue);
        return reportId;
    }

    private void executeTestPlanOrGroup(TestPlanExecutionQueue executionQueue) {
        val testPlan = testPlanMapper.selectOneById(executionQueue.getSourceID());
        if (testPlan == null || StringUtils.equalsIgnoreCase(testPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
            throw new CustomException("test_plan.error");
        }
    }
}
