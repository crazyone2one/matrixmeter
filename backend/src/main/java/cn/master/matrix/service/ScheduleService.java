package cn.master.matrix.service;

import cn.master.matrix.payload.dto.request.ScheduleConfig;
import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.Schedule;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

/**
 * 定时任务 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-22T13:59:18.897982400
 */
public interface ScheduleService extends IService<Schedule> {

    int deleteByResourceId(String scenarioId, JobKey jobKey, TriggerKey triggerKey);

    Schedule getScheduleByResource(String resourceId, String job);

    void updateIfExist(String resourceId, boolean enable, JobKey jobKey, TriggerKey triggerKey, Class clazz, String operator);

    String scheduleConfig(ScheduleConfig scheduleConfig, JobKey jobKey, TriggerKey triggerKey, Class clazz, String operator);
}
