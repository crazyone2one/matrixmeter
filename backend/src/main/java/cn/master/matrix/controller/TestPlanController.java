package cn.master.matrix.controller;

import cn.master.matrix.constants.HttpMethodConstants;
import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.constants.PermissionConstants;
import cn.master.matrix.constants.TestPlanResourceConfig;
import cn.master.matrix.entity.TestPlan;
import cn.master.matrix.handler.annotation.HasAuthorize;
import cn.master.matrix.handler.annotation.Log;
import cn.master.matrix.payload.dto.plan.request.*;
import cn.master.matrix.payload.dto.plan.response.TestPlanDetailResponse;
import cn.master.matrix.payload.dto.plan.response.TestPlanOperationResponse;
import cn.master.matrix.payload.dto.plan.response.TestPlanResponse;
import cn.master.matrix.payload.dto.plan.response.TestPlanSingleOperationResponse;
import cn.master.matrix.service.plan.TestPlanManagementService;
import cn.master.matrix.service.plan.TestPlanService;
import cn.master.matrix.service.log.TestPlanLogService;
import cn.master.matrix.util.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 测试计划 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-19T14:49:06.626698900
 */
@RestController
@Tag(name = "测试计划接口")
@RequiredArgsConstructor
@RequestMapping("/test-plan")
public class TestPlanController {

    private final TestPlanService testPlanService;
    private final TestPlanManagementService testPlanManagementService;

    /**
     * 添加测试计划。
     *
     * @param request 测试计划
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("add")
    @Operation(summary = "测试计划-创建测试计划")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_ADD)
    public TestPlan save(@Validated @RequestBody @Parameter(description = "测试计划") TestPlanCreateRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanService.save(request, SessionUtils.getUserId(), "/test-plan/add", HttpMethodConstants.POST.name());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "测试计划-删除测试计划")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_DELETE)
    public void remove(@NotBlank @PathVariable @Parameter(description = "测试计划主键") String id) {
        testPlanManagementService.checkModuleIsOpen(id, TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        testPlanService.delete(id, SessionUtils.getUserId(), "/test-plan/delete", HttpMethodConstants.GET.name());
    }

    /**
     * 根据主键更新测试计划。
     *
     * @param testPlan 测试计划
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @Operation(description = "根据主键更新测试计划")
    public boolean update(@RequestBody @Parameter(description = "测试计划主键") TestPlan testPlan) {
        return testPlanService.updateById(testPlan);
    }

    @GetMapping("/list-in-group/{groupId}")
    @Operation(summary = "测试计划-表格分页查询")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ)
    public List<TestPlanResponse> listInGroup(@NotBlank @PathVariable String groupId) {
        testPlanManagementService.checkModuleIsOpen(groupId, TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanService.selectByGroupId(groupId);
    }

    @PostMapping("/module/count")
    @Operation(summary = "测试计划-模块统计")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ)
    public Map<String, Long> moduleCount(@Validated @RequestBody TestPlanTableRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanService.moduleCount(request);
    }

    /**
     * 分页查询测试计划。
     *
     * @param request 分页对象
     * @return 分页对象
     */
    @PostMapping("page")
    @Operation(description = "分页查询测试计划")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ)
    public Page<TestPlanResponse> page(@Validated @RequestBody TestPlanTableRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanService.page(request);
    }

    @GetMapping("/group-list/{projectId}")
    @Operation(summary = "测试计划-测试计划组查询")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ)
    public List<TestPlan> groupList(@PathVariable String projectId) {
        testPlanManagementService.checkModuleIsOpen(projectId, TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanService.groupList(projectId);
    }

    @PostMapping("/edit/follower")
    @Operation(summary = "测试计划-关注/取消关注")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_UPDATE)
    public void editFollower(@Validated @RequestBody TestPlanFollowerRequest request) {
        String userId = SessionUtils.getUserId();
        testPlanService.editFollower(request.getTestPlanId(), userId);
    }

    @GetMapping("/archived/{id}")
    @Operation(summary = "测试计划-归档")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_UPDATE)
    //@CheckOwner(resourceId = "#id", resourceType = "test_plan")
    @Log(type = OperationLogType.ARCHIVED, expression = "#mmClass.archivedLog(#id)", mmClass = TestPlanLogService.class)
    public void archived(@NotBlank @PathVariable String id) {
        testPlanService.archived(id, SessionUtils.getUserId());
    }

    @GetMapping("/{id}")
    @Operation(summary = "测试计划-抽屉详情(单个测试计划获取详情用于编辑)")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ)
    public TestPlanDetailResponse detail(@NotBlank @PathVariable String id) {
        return testPlanService.detail(id, SessionUtils.getUserId());
    }

    @PostMapping(value = "/batch-delete")
    @Operation(summary = "测试计划-批量删除")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_DELETE)
    public void delete(@Validated @RequestBody TestPlanBatchProcessRequest request) throws Exception {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        testPlanService.batchDelete(request, SessionUtils.getUserId(), "/test-plan/batch-delete", HttpMethodConstants.POST.name());
    }

    @GetMapping("/copy/{id}")
    @Operation(summary = "测试计划-复制测试计划")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_ADD)
    public TestPlanSingleOperationResponse copy(@PathVariable String id) {
        return new TestPlanSingleOperationResponse(testPlanService.copy(id, SessionUtils.getUserId()));
    }

    @PostMapping("/batch-copy")
    @Operation(summary = "测试计划-批量复制测试计划")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_ADD)
    public TestPlanOperationResponse batchCopy(@Validated @RequestBody TestPlanBatchRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        testPlanService.filterArchivedIds(request);
        return new TestPlanOperationResponse(
                testPlanService.batchCopy(request, SessionUtils.getUserId(), "/test-plan/batch-copy", HttpMethodConstants.POST.name())
        );
    }
}
