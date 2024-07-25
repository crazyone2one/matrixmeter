package cn.master.matrix.service.plan;

import cn.master.matrix.constants.ApiBatchRunMode;
import cn.master.matrix.payload.dto.queue.TestPlanExecutionQueue;
import cn.master.matrix.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
@Service
@RequiredArgsConstructor
public class TestPlanExecuteSupportService {
    private final StringRedisTemplate stringRedisTemplate;
    public static final String QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE = "test-plan-batch-execute:";
    public static final String QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE = "test-plan-group-execute:";
    public static final String QUEUE_PREFIX_TEST_PLAN_CASE_TYPE = "test-plan-case-type-execute:";
    public static final String QUEUE_PREFIX_TEST_PLAN_COLLECTION = "test-plan-collection-execute:";
    public static final String LAST_QUEUE_PREFIX = "last-queue:";

    public void setRedisForList(String key, List<String> list) {
        stringRedisTemplate.opsForList().rightPushAll(key, list);
        stringRedisTemplate.expire(key, 1, TimeUnit.DAYS);
    }
    public String genQueueKey(String queueId, String queueType) {
        return queueType + queueId;
    }

    public TestPlanExecutionQueue getNextQueue(String queueId, String queueType) {
        if (StringUtils.isAnyBlank(queueId, queueType)) {
            return null;
        }

        String queueKey = this.genQueueKey(queueId, queueType);
        ListOperations<String, String> listOps = stringRedisTemplate.opsForList();
        String queueDetail = listOps.leftPop(queueKey);
        if (StringUtils.isBlank(queueDetail)) {
            // 重试1次获取
            try {
                Thread.sleep(1000);
            } catch (Exception ignore) {
            }
            queueDetail = stringRedisTemplate.opsForList().leftPop(queueKey);
        }

        if (StringUtils.isNotBlank(queueDetail)) {
            TestPlanExecutionQueue returnQueue = JsonUtils.parseObject(queueDetail, TestPlanExecutionQueue.class);
            Long size = listOps.size(queueKey);
            if (size == null || size == 0) {
                returnQueue.setLastOne(true);
                if (StringUtils.equalsIgnoreCase(returnQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
                    //串行的执行方式意味着最后一个节点要单独存储
                    stringRedisTemplate.opsForValue().setIfAbsent(genQueueKey(queueKey, LAST_QUEUE_PREFIX), JsonUtils.toJsonString(returnQueue), 1, TimeUnit.DAYS);
                }
                // 最后一个节点清理队列
                deleteQueue(queueKey);
            }
            return returnQueue;
        } else {
            String lastQueueJson = stringRedisTemplate.opsForValue().getAndDelete(genQueueKey(queueKey, LAST_QUEUE_PREFIX));
            if (StringUtils.isNotBlank(lastQueueJson)) {
                TestPlanExecutionQueue nextQueue = JsonUtils.parseObject(lastQueueJson, TestPlanExecutionQueue.class);
                nextQueue.setExecuteFinish(true);
                return nextQueue;
            }
        }

        // 整体获取完，清理队列
        deleteQueue(queueKey);
        return null;
    }
    public Boolean deleteQueue(String queueKey) {
        return stringRedisTemplate.delete(queueKey);
    }

}
