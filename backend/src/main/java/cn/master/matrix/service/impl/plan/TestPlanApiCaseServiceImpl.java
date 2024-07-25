package cn.master.matrix.service.impl.plan;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.matrix.entity.TestPlanApiCase;
import cn.master.matrix.mapper.TestPlanApiCaseMapper;
import cn.master.matrix.service.plan.TestPlanApiCaseService;
import org.springframework.stereotype.Service;

/**
 * 测试计划关联接口用例 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-22T13:57:33.574702
 */
@Service
public class TestPlanApiCaseServiceImpl extends ServiceImpl<TestPlanApiCaseMapper, TestPlanApiCase> implements TestPlanApiCaseService {

}
