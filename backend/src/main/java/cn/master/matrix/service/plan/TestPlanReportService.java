package cn.master.matrix.service.plan;

import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.TestPlanReport;

import java.util.List;

/**
 * 测试计划报告 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-22T11:36:13.643773700
 */
public interface TestPlanReportService extends IService<TestPlanReport> {

    void deleteByTestPlanIds(List<String> testPlanIds);
}
