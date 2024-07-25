package cn.master.matrix.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.matrix.entity.ApiTestCase;
import cn.master.matrix.mapper.ApiTestCaseMapper;
import cn.master.matrix.service.plan.ApiTestCaseService;
import org.springframework.stereotype.Service;

/**
 * 接口用例 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-22T14:22:45.489615300
 */
@Service
public class ApiTestCaseServiceImpl extends ServiceImpl<ApiTestCaseMapper, ApiTestCase> implements ApiTestCaseService {

}
