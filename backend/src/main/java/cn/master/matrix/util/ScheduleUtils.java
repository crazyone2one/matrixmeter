package cn.master.matrix.util;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
public class ScheduleUtils {
    public static LocalDateTime getNextTriggerTime(String cron) {
        if (!CronExpression.isValidExpression(cron)) {
            return null;
        }
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("Calculate Date").withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
        Date time0 = trigger.getStartTime();
        Date time1 = trigger.getFireTimeAfter(time0);
        return time1 == null ? LocalDateTime.now() : DateUtils.date2LocalDate(time1);
    }
}
