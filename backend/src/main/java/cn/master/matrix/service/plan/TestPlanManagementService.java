package cn.master.matrix.service.plan;

import cn.master.matrix.constants.TestPlanResourceConfig;
import cn.master.matrix.entity.Project;
import cn.master.matrix.entity.TestPlan;
import cn.master.matrix.entity.TestPlanModule;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.mapper.ProjectMapper;
import cn.master.matrix.util.JsonUtils;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.query.QueryChain;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

import static cn.master.matrix.entity.table.TestPlanModuleTableDef.TEST_PLAN_MODULE;
import static cn.master.matrix.entity.table.TestPlanTableDef.TEST_PLAN;

/**
 * @author Created by 11's papa on 07/19/2024
 **/
@Service
@RequiredArgsConstructor
public class TestPlanManagementService {
    private final ProjectMapper projectMapper;

    public void checkModuleIsOpen(String resourceId, String resourceType, List<String> moduleMenus) {
        Project project;

        if (StringUtils.equals(resourceType, TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN)) {
            project = projectMapper.selectOneById(selectProjectIdByTestPlanId(resourceId));
        } else if (StringUtils.equals(resourceType, TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN_MODULE)) {
            project = projectMapper.selectOneById(selectProjectIdByModuleId(resourceId));
        } else if (StringUtils.equals(resourceType, TestPlanResourceConfig.CHECK_TYPE_PROJECT)) {
            project = projectMapper.selectOneById(resourceId);
        } else {
            throw new CustomException(Translator.get("project.module_menu.check.error"));
        }

        if (project == null || CollectionUtils.isEmpty(project.getModuleSetting())) {
            throw new CustomException(Translator.get("project.module_menu.check.error"));
        }
        List<String> projectModuleMenus = JsonUtils.parseArray(JsonUtils.toJsonString(project.getModuleSetting()), String.class);
        if (!new HashSet<>(projectModuleMenus).containsAll(moduleMenus)) {
            throw new CustomException(Translator.get("project.module_menu.check.error"));
        }
    }

    private String selectProjectIdByModuleId(String resourceId) {
        return QueryChain.of(TestPlanModule.class).select(TEST_PLAN_MODULE.PROJECT_ID).from(TEST_PLAN_MODULE)
                .where(TEST_PLAN_MODULE.ID.eq(resourceId)).oneAs(String.class);
    }

    private String selectProjectIdByTestPlanId(String testPlanId) {
        return QueryChain.of(TestPlan.class).select(TEST_PLAN.PROJECT_ID).from(TEST_PLAN)
                .where(TEST_PLAN.ID.eq(testPlanId)).oneAs(String.class);
    }

}
