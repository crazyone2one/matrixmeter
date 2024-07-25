package cn.master.matrix.service.plan;

import cn.master.matrix.constants.ScheduleResourceType;
import cn.master.matrix.constants.TestPlanConstants;
import cn.master.matrix.entity.TestPlan;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.handler.schedule.TestPlanScheduleJob;
import cn.master.matrix.mapper.TestPlanMapper;
import cn.master.matrix.payload.dto.request.BaseScheduleConfigRequest;
import cn.master.matrix.payload.dto.request.ScheduleConfig;
import cn.master.matrix.service.ScheduleService;
import cn.master.matrix.util.JsonUtils;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.query.QueryChain;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static cn.master.matrix.entity.table.TestPlanTableDef.TEST_PLAN;

/**
 * @author Created by 11's papa on 07/23/2024
 **/
@Service
@RequiredArgsConstructor
public class TestPlanScheduleService {
    private final TestPlanMapper testPlanMapper;
    private final ScheduleService scheduleService;

    public String scheduleConfig(BaseScheduleConfigRequest request, String operator) {
        val testPlan = testPlanMapper.selectOneById(request.getResourceId());
        if (Objects.isNull(testPlan)) {
            throw new CustomException(Translator.get("test_plan.not.exist"));
        }
        ScheduleConfig scheduleConfig = ScheduleConfig.builder()
                .resourceId(testPlan.getId())
                .key(testPlan.getId())
                .projectId(testPlan.getProjectId())
                .name(testPlan.getName())
                .enable(request.isEnable())
                .cron(request.getCron())
                .resourceType(ScheduleResourceType.TEST_PLAN.name())
                .config(JsonUtils.toJsonString(request.getRunConfig()))
                .build();
        if (request.isEnable() && StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            val children = QueryChain.of(TestPlan.class).where(TEST_PLAN.GROUP_ID.eq(testPlan.getId())
                            .and(TEST_PLAN.STATUS.ne(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)))
                    .orderBy(TEST_PLAN.POS.asc()).list();
            for (TestPlan child : children) {
                scheduleService.updateIfExist(child.getId(), false, TestPlanScheduleJob.getJobKey(testPlan.getId()),
                        TestPlanScheduleJob.getTriggerKey(testPlan.getId()),
                        TestPlanScheduleJob.class, operator);
            }
        }
        return scheduleService.scheduleConfig(
                scheduleConfig,
                TestPlanScheduleJob.getJobKey(testPlan.getId()),
                TestPlanScheduleJob.getTriggerKey(testPlan.getId()),
                TestPlanScheduleJob.class,
                operator);
    }
}
