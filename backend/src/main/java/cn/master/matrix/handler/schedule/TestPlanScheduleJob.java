package cn.master.matrix.handler.schedule;

import cn.master.matrix.constants.ApiBatchRunMode;
import cn.master.matrix.constants.ApiExecuteRunMode;
import cn.master.matrix.handler.uid.IDGenerator;
import cn.master.matrix.payload.dto.plan.request.TestPlanExecuteRequest;
import cn.master.matrix.service.plan.TestPlanExecuteService;
import cn.master.matrix.util.CommonBeanFactory;
import cn.master.matrix.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.util.Map;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
@Slf4j
public class TestPlanScheduleJob extends BaseScheduleJob{
    @Override
    protected void businessExecute(JobExecutionContext context) {
        TestPlanExecuteService testPlanExecuteService = CommonBeanFactory.getBean(TestPlanExecuteService.class);
        assert testPlanExecuteService != null;
        Map<String, String> runConfig = JsonUtils.parseObject(context.getJobDetail().getJobDataMap().get("config").toString(), Map.class);
        String runMode = runConfig.containsKey("runMode") ? runConfig.get("runMode") : ApiBatchRunMode.SERIAL.name();
        log.info("开始执行测试计划的定时任务. ID：{}", resourceId);
        Thread.startVirtualThread(() ->
                testPlanExecuteService.singleExecuteTestPlan(new TestPlanExecuteRequest() {{
                    this.setExecuteId(resourceId);
                    this.setRunMode(runMode);
                    this.setExecutionSource(ApiExecuteRunMode.SCHEDULE.name());
                }}, IDGenerator.nextStr(), userId)
        );
    }
    public static JobKey getJobKey(String testPlanId) {
        return new JobKey(testPlanId, TestPlanScheduleJob.class.getName());
    }

    public static TriggerKey getTriggerKey(String testPlanId) {
        return new TriggerKey(testPlanId, TestPlanScheduleJob.class.getName());
    }
}
