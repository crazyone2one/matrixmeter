package cn.master.matrix.payload.dto.request;

import cn.master.matrix.constants.ScheduleType;
import cn.master.matrix.entity.Schedule;
import lombok.Builder;
import lombok.Data;

/**
 * @author Created by 11's papa on 07/23/2024
 **/
@Data
@Builder
public class ScheduleConfig {

    private String resourceId;

    private String key;

    private String projectId;

    private String name;

    private Boolean enable;

    private String cron;

    private String resourceType;

    private String config;

    public Schedule genCronSchedule(Schedule schedule) {
        if (schedule == null) {
            schedule = new Schedule();
        }
        schedule.setName(this.getName());
        schedule.setResourceId(this.getResourceId());
        schedule.setType(ScheduleType.CRON.name());
        schedule.setKey(this.getKey());
        schedule.setEnable(this.getEnable());
        schedule.setProjectId(this.getProjectId());
        schedule.setValue(this.getCron());
        schedule.setResourceType(this.getResourceType());
        schedule.setConfig(this.getConfig());
        return schedule;
    }
}
