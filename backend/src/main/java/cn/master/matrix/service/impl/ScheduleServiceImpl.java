package cn.master.matrix.service.impl;

import cn.master.matrix.entity.Schedule;
import cn.master.matrix.handler.schedule.ScheduleManager;
import cn.master.matrix.handler.uid.IDGenerator;
import cn.master.matrix.mapper.ScheduleMapper;
import cn.master.matrix.payload.dto.request.ScheduleConfig;
import cn.master.matrix.service.ScheduleService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;

/**
 * 定时任务 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-22T13:59:18.897982400
 */
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    private final ScheduleManager scheduleManager;


    @Override
    public int deleteByResourceId(String scenarioId, JobKey jobKey, TriggerKey triggerKey) {
        val queryChain = queryChain().where(Schedule::getResourceId).eq(scenarioId);
        scheduleManager.removeJob(jobKey, triggerKey);
        return mapper.deleteByQuery(queryChain);
    }

    @Override
    public Schedule getScheduleByResource(String resourceId, String job) {
        val schedules = queryChain().where(Schedule::getResourceId).eq(resourceId).and(Schedule::getJob).eq(job).list();
        if (CollectionUtils.isNotEmpty(schedules)) {
            return schedules.getFirst();
        }
        return null;
    }

    @Override
    public void updateIfExist(String resourceId, boolean enable, JobKey jobKey, TriggerKey triggerKey, Class clazz, String operator) {
        val schedules = queryChain().where(Schedule::getResourceId).eq(resourceId).and(Schedule::getJob).eq(clazz.getName()).list();
        
    }

    @Override
    public String scheduleConfig(ScheduleConfig scheduleConfig, JobKey jobKey, TriggerKey triggerKey, Class clazz, String operator) {
        Schedule schedule;
        val queryChain = queryChain().where(Schedule::getResourceId).eq(scheduleConfig.getResourceId()).and(Schedule::getJob).eq(clazz.getName());
        val scheduleList = queryChain.list();
        boolean needSendNotice = false;
        if (CollectionUtils.isNotEmpty(scheduleList)) {
            needSendNotice = !scheduleList.getFirst().getEnable().equals(scheduleConfig.getEnable());
            schedule = scheduleConfig.genCronSchedule(scheduleList.getFirst());
            schedule.setJob(clazz.getName());
            mapper.updateByQuery(schedule, queryChain);
        } else {
            schedule = scheduleConfig.genCronSchedule(null);
            schedule.setJob(clazz.getName());
            schedule.setId(IDGenerator.nextStr());
            schedule.setCreateUser(operator);
            mapper.insert(schedule);
        }
        return schedule.getId();
    }
}
