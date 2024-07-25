package cn.master.matrix.handler.schedule;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
@Slf4j
public abstract class BaseScheduleJob implements Job {
    protected String resourceId;

    protected String userId;

    protected String expression;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobKey jobKey = context.getTrigger().getJobKey();
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        this.resourceId = jobDataMap.getString("resourceId");
        this.userId = jobDataMap.getString("userId");
        this.expression = jobDataMap.getString("expression");
        log.info("{} Running: {}", jobKey.getGroup(), resourceId);
        businessExecute(context);
    }

    protected abstract void businessExecute(JobExecutionContext context);
}
