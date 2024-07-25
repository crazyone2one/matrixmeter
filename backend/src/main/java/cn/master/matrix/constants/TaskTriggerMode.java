package cn.master.matrix.constants;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
public enum TaskTriggerMode {
    /**
     * 定时任务
     */
    SCHEDULE,
    /**
     * 手动执行
     */
    MANUAL,
    /**
     * 接口调用
     */
    API,
    /**
     * 批量执行
     */
    BATCH
}
