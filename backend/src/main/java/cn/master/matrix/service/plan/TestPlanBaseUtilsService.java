package cn.master.matrix.service.plan;

import cn.master.matrix.constants.ModuleConstants;
import cn.master.matrix.constants.TestPlanConstants;
import cn.master.matrix.entity.TestPlanModule;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.query.QueryChain;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
@Service
public class TestPlanBaseUtilsService {

    public String calculateStatusByChildren(List<String> childStatus) {
        childStatus = childStatus.stream().distinct().toList();
        /*
		1:全部都未开始 则为未开始
		2:全部都已完成 则为已完成
		3:包含进行中 则为进行中
		4:未开始+已完成：进行中
		 */
        if (childStatus.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY)) {
            return TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY;
        } else if (childStatus.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED) && childStatus.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED)) {
            return TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY;
        } else if (childStatus.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
            return TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED;
        } else {
            return TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED;
        }
    }

    protected void checkModule(String moduleId) {
        if (!StringUtils.equals(moduleId, ModuleConstants.DEFAULT_NODE_ID)) {
            val list = QueryChain.of(TestPlanModule.class).where(TestPlanModule::getId).eq(moduleId).list();
            if (list.isEmpty()) {
                throw new CustomException(Translator.get("module.not.exist"));
            }
        }
    }
}
