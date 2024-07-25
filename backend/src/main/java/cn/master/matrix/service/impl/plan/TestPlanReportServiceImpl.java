package cn.master.matrix.service.impl.plan;

import cn.master.matrix.entity.TestPlanReport;
import cn.master.matrix.mapper.TestPlanReportMapper;
import cn.master.matrix.service.plan.TestPlanReportService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.master.matrix.entity.table.TestPlanReportTableDef.TEST_PLAN_REPORT;

/**
 * 测试计划报告 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-22T11:36:13.643773700
 */
@Service
public class TestPlanReportServiceImpl extends ServiceImpl<TestPlanReportMapper, TestPlanReport> implements TestPlanReportService {

    @Override
    public void deleteByTestPlanIds(List<String> testPlanIds) {
        if (CollectionUtils.isNotEmpty(testPlanIds)) {
            val reportIdList = queryChain().where(TEST_PLAN_REPORT.TEST_PLAN_ID.in(testPlanIds)).listAs(String.class);
            mapper.deleteBatchByIds(reportIdList);
            deleteTestPlanReportBlobs(reportIdList);
        }
    }

    private void deleteTestPlanReportBlobs(List<String> reportIdList) {
        //todo
    }
}
