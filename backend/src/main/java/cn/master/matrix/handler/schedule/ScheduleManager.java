package cn.master.matrix.handler.schedule;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Component;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
@Slf4j
@Component
public class ScheduleManager {
    @Resource
    private Scheduler scheduler;

    public void removeJob(JobKey jobKey, TriggerKey triggerKey) {
        try {
            log.info("RemoveJob: {},{}", jobKey.getName(), jobKey.getGroup());
            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);
            scheduler.deleteJob(jobKey);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
