package cn.master.matrix.constants;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
public enum ApiExecuteRunMode {
    /**
     * 手动运行
     */
    RUN,
    /**
     * 前端调试
     */
    FRONTEND_DEBUG,
    /**
     * 后端调试
     */
    BACKEND_DEBUG,

    /**
     * jenkins 触发
     */
    JENKINS,
    /**
     * 定时任务
     */
    SCHEDULE
}
