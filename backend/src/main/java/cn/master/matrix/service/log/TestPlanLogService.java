package cn.master.matrix.service.log;

import cn.master.matrix.constants.HttpMethodConstants;
import cn.master.matrix.constants.OperationLogModule;
import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.constants.TestPlanConstants;
import cn.master.matrix.entity.Project;
import cn.master.matrix.entity.TestPlan;
import cn.master.matrix.mapper.ProjectMapper;
import cn.master.matrix.mapper.TestPlanMapper;
import cn.master.matrix.payload.LogDTOBuilder;
import cn.master.matrix.payload.dto.LogDTO;
import cn.master.matrix.payload.dto.LogInsertModule;
import cn.master.matrix.service.OperationLogService;
import cn.master.matrix.util.JsonUtils;
import cn.master.matrix.util.Translator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TestPlanLogService {
    private final ProjectMapper projectMapper;
    private final TestPlanMapper testPlanMapper;
    private final OperationLogService operationLogService;

    public LogDTO scheduleLog(String id) {
        TestPlan testPlan = testPlanMapper.selectOneById(id);
        Project project = projectMapper.selectOneById(testPlan.getProjectId());

        return LogDTOBuilder.builder()
                .projectId(project.getId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(getLogModule(testPlan))
                .sourceId(testPlan.getId())
                .content(Translator.get("test_plan_schedule") + ":" + testPlan.getName())
                .build().getLogDTO();
    }

    public void saveAddLog(TestPlan module, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectOneById(module.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(module.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.ADD.name())
                .module(getLogModule(module))
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(module.getId())
                .content(module.getName())
                .originalValue(JsonUtils.toJsonBytes(module))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveUpdateLog(TestPlan oldTestPlan, TestPlan newTestPlan, String projectId, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectOneById(projectId);
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(projectId)
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(getLogModule(newTestPlan))
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(newTestPlan.getId())
                .content(newTestPlan.getName())
                .originalValue(JsonUtils.toJsonBytes(oldTestPlan))
                .modifiedValue(JsonUtils.toJsonBytes(newTestPlan))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveDeleteLog(TestPlan deleteTestPlan, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectOneById(deleteTestPlan.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(deleteTestPlan.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.DELETE.name())
                .module(getLogModule(deleteTestPlan))
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(deleteTestPlan.getId())
                .content(deleteTestPlan.getName())
                .originalValue(JsonUtils.toJsonBytes(deleteTestPlan))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public LogDTO archivedLog(String id) {
        TestPlan testPlan = testPlanMapper.selectOneById(id);
        LogDTO dto = new LogDTO(
                testPlan.getProjectId(),
                null,
                testPlan.getId(),
                null,
                OperationLogType.ARCHIVED.name(),
                getLogModule(testPlan),
                testPlan.getName());
        dto.setPath("/test-plan/archived");
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JsonUtils.toJsonBytes(testPlan));
        return dto;
    }

    public void copyLog(TestPlan testPlan, String operator) {
        if (testPlan != null) {
            Project project = projectMapper.selectOneById(testPlan.getProjectId());
            LogDTO dto = new LogDTO(
                    testPlan.getProjectId(),
                    project.getOrganizationId(),
                    testPlan.getId(),
                    operator,
                    OperationLogType.ADD.name(),
                    getLogModule(testPlan),
                    testPlan.getName());
            dto.setPath("/test-plan/copy");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JsonUtils.toJsonBytes(testPlan));
            operationLogService.add(dto);
        }
    }

    public void saveBatchLog(List<TestPlan> plans, String operator, String requestUrl, String requestMethod, String requestType) {
        if (CollectionUtils.isEmpty(plans)) {
            return;
        }
        Project project = projectMapper.selectOneById(plans.get(0).getProjectId());
        List<LogDTO> list = new ArrayList<>();
        for (TestPlan plan : plans) {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(plan.getProjectId())
                    .organizationId(project.getOrganizationId())
                    .type(requestType)
                    .module(getLogModule(plan))
                    .method(requestMethod)
                    .path(requestUrl)
                    .sourceId(plan.getId())
                    .content(plan.getName())
                    .originalValue(JsonUtils.toJsonBytes(plan))
                    .createUser(operator)
                    .build().getLogDTO();
            list.add(dto);
        }
        operationLogService.batchAdd(list);
    }

    public void saveMoveLog(TestPlan testPlan, String moveId, LogInsertModule logInsertModule) {

        Project project = projectMapper.selectOneById(testPlan.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(testPlan.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(getLogModule(testPlan))
                .method(logInsertModule.getRequestMethod())
                .path(logInsertModule.getRequestUrl())
                .sourceId(moveId)
                .content(Translator.get("log.test_plan.move.test_plan") + ":" + testPlan.getName() + StringUtils.SPACE)
                .createUser(logInsertModule.getOperator())
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    private String getLogModule(TestPlan testPlan) {
        if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_PLAN)) {
            return OperationLogModule.TEST_PLAN_TEST_PLAN;
        } else {
            return OperationLogModule.TEST_PLAN_TEST_PLAN_GROUP;
        }
    }
}

