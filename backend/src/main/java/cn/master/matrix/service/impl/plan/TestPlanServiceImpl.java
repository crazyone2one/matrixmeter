package cn.master.matrix.service.impl.plan;

import cn.master.matrix.constants.*;
import cn.master.matrix.entity.*;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.handler.converter.TestPlanConverter;
import cn.master.matrix.handler.schedule.TestPlanScheduleJob;
import cn.master.matrix.mapper.*;
import cn.master.matrix.payload.dto.plan.dto.TestPlanCollectionDTO;
import cn.master.matrix.payload.dto.plan.request.TestPlanBatchProcessRequest;
import cn.master.matrix.payload.dto.plan.request.TestPlanBatchRequest;
import cn.master.matrix.payload.dto.plan.request.TestPlanCreateRequest;
import cn.master.matrix.payload.dto.plan.request.TestPlanTableRequest;
import cn.master.matrix.payload.dto.plan.response.TestPlanDetailResponse;
import cn.master.matrix.payload.dto.plan.response.TestPlanResponse;
import cn.master.matrix.payload.dto.plan.response.TestPlanStatisticsResponse;
import cn.master.matrix.payload.dto.project.ModuleCountDTO;
import cn.master.matrix.payload.dto.project.request.ProjectApplicationRequest;
import cn.master.matrix.service.ProjectApplicationService;
import cn.master.matrix.service.ScheduleService;
import cn.master.matrix.service.log.TestPlanLogService;
import cn.master.matrix.service.plan.*;
import cn.master.matrix.util.*;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.master.matrix.entity.table.TestPlanConfigTableDef.TEST_PLAN_CONFIG;
import static cn.master.matrix.entity.table.TestPlanFollowerTableDef.TEST_PLAN_FOLLOWER;
import static cn.master.matrix.entity.table.TestPlanTableDef.TEST_PLAN;

/**
 * 测试计划 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-19T14:49:06.626698900
 */
@Service
@RequiredArgsConstructor
public class TestPlanServiceImpl extends ServiceImpl<TestPlanMapper, TestPlan> implements TestPlanService {
    private final ApplicationContext applicationContext;
    private final TestPlanLogService testPlanLogService;
    private final TestPlanGroupService testPlanGroupService;
    private final TestPlanConfigMapper testPlanConfigMapper;
    private final ProjectApplicationService projectApplicationService;
    private final TestPlanCollectionMapper testPlanCollectionMapper;
    private final TestPlanModuleService testPlanModuleService;
    private final TestPlanFollowerMapper testPlanFollowerMapper;
    private final TestPlanStatisticsService testPlanStatisticsService;
    private final ScheduleService scheduleService;
    private final TestPlanModuleMapper testPlanModuleMapper;
    private final TestPlanReportService testPlanReportService;
    private final TestPlanBatchOperationService testPlanBatchOperationService;

    @Override
    public Page<TestPlanResponse> page(TestPlanTableRequest request) {
        initDefaultFilter(request);
        return queryChain().where(TEST_PLAN.PROJECT_ID.eq(request.getProjectId())
                        .and(TEST_PLAN.MODULE_ID.in(request.getModuleIds()))
                        .and(TEST_PLAN.GROUP_ID.eq("NONE").when("ALL".equals(request.getType())))
                        .and((TEST_PLAN.GROUP_ID.eq("NONE").and(TEST_PLAN.TYPE.eq("TEST_PLAN"))).when("TEST_PLAN".equals(request.getType())))
                        .and((TEST_PLAN.GROUP_ID.eq("NONE").and(TEST_PLAN.TYPE.eq("GROUP"))).when("GROUP".equals(request.getType())))
                        .and(TEST_PLAN.NAME.like(request.getKeyword())
                                .or(TEST_PLAN.NUM.like(request.getKeyword()))
                                .or(TEST_PLAN.TAGS.like(request.getKeyword()))
                                .or(TEST_PLAN.ID.in(request.getKeywordFilterIds())))
                        .and(TEST_PLAN.ID.in(request.getInnerIds()))
                ).orderBy(TEST_PLAN.POS.desc(), TEST_PLAN.ID.desc())
                .pageAs(Page.of(request.getPageNum(), request.getPageSize()), TestPlanResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestPlan save(TestPlanCreateRequest request, String operator, String requestUrl, String requestMethod) {
        TestPlan testPlan = savePlanDTO(request, operator);
        //自动生成测试规划
        initDefaultPlanCollection(testPlan.getId(), operator);

        testPlanLogService.saveAddLog(testPlan, operator, requestUrl, requestMethod);
        return testPlan;
    }

    @Override
    public void checkModule(String moduleId) {
        val list = queryChain().where(TEST_PLAN.ID.eq(moduleId).when(!StringUtils.equals(moduleId, ModuleConstants.DEFAULT_NODE_ID))).list();
        if (list.isEmpty()) {
            throw new CustomException(Translator.get("module.not.exist"));
        }
    }

    @Override
    public List<TestPlanCollectionDTO> initDefaultPlanCollection(String planId, String currentUser) {
        List<TestPlanCollectionDTO> collectionDTOList = new ArrayList<>();
        val testPlan = mapper.selectOneById(planId);
        ProjectApplicationRequest projectApplicationRequest = new ProjectApplicationRequest();
        projectApplicationRequest.setProjectId(testPlan.getProjectId());
        projectApplicationRequest.setType("apiTest");
        Map<String, Object> configMap = projectApplicationService.get(projectApplicationRequest, Arrays.stream(ProjectApplicationType.API.values()).map(ProjectApplicationType.API::name).collect(Collectors.toList()));
        // 批量插入测试集
        List<TestPlanCollection> collections = new ArrayList<>();
        TestPlanCollection defaultCollection = new TestPlanCollection();
        defaultCollection.setTestPlanId(planId);
        defaultCollection.setExecuteMethod(ExecuteMethod.SERIAL.name());
        defaultCollection.setExtended(true);
        defaultCollection.setGrouped(false);
        defaultCollection.setEnvironmentId("NONE");
        defaultCollection.setTestResourcePoolId(configMap.getOrDefault(ProjectApplicationType.API.API_RESOURCE_POOL_ID.name(), StringUtils.EMPTY).toString());
        defaultCollection.setRetryOnFail(false);
        defaultCollection.setStopOnFail(false);
        defaultCollection.setCreateUser(currentUser);
        long initPos = 1L;
        for (CaseType caseType : CaseType.values()) {
            // 测试集分类
            TestPlanCollectionDTO parentCollectionDTO = new TestPlanCollectionDTO();
            TestPlanCollection parentCollection = new TestPlanCollection();
            BeanUtils.copyProperties(defaultCollection, parentCollection);
            //parentCollection.setId(IDGenerator.nextStr());
            parentCollection.setParentId(TestPlanConstants.DEFAULT_PARENT_ID);
            parentCollection.setName(caseType.getType());
            parentCollection.setType(caseType.getKey());
            parentCollection.setPos(initPos << 12);
            collections.add(parentCollection);
            BeanUtils.copyProperties(parentCollection, parentCollectionDTO);
            // 测试集
            TestPlanCollectionDTO childCollectionDTO = new TestPlanCollectionDTO();
            TestPlanCollection childCollection = new TestPlanCollection();
            BeanUtils.copyProperties(defaultCollection, childCollection);
            //childCollection.setId(IDGenerator.nextStr());
            childCollection.setParentId(parentCollection.getId());
            childCollection.setName(caseType.getPlanDefaultCollection());
            childCollection.setType(caseType.getKey());
            childCollection.setPos(1L << 12);
            collections.add(childCollection);
            BeanUtils.copyProperties(childCollection, childCollectionDTO);
            parentCollectionDTO.setChildren(List.of(childCollectionDTO));
            // 更新pos
            initPos++;

            collectionDTOList.add(parentCollectionDTO);
        }
        testPlanCollectionMapper.insertBatch(collections);
        return collectionDTOList;
    }

    @Override
    public List<TestPlan> groupList(String projectId) {
        return queryChain()
                .where(TEST_PLAN.PROJECT_ID.eq(projectId)
                        .and(TEST_PLAN.TYPE.eq(TestPlanConstants.TEST_PLAN_TYPE_GROUP))
                        .and(TEST_PLAN.STATUS.ne(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)))
                .orderBy(TEST_PLAN.POS.desc(), TEST_PLAN.ID.desc())
                .list();
    }

    @Override
    public List<TestPlanResponse> selectByGroupId(String groupId) {
        return queryChain()
                .where(TEST_PLAN.GROUP_ID.in(Collections.singletonList(groupId)))
                .orderBy(TEST_PLAN.POS.desc())
                .listAs(TestPlanResponse.class);
    }

    @Override
    public Map<String, Long> moduleCount(TestPlanTableRequest request) {
        this.initDefaultFilter(request);
        //查出每个模块节点下的资源数量。 不需要按照模块进行筛选
        request.setModuleIds(null);
        List<ModuleCountDTO> moduleCountDTOList = countModuleIdByConditions(request);
        Map<String, Long> moduleCountMap = testPlanModuleService.getModuleCountMap(request.getProjectId(), moduleCountDTOList);
        long allCount = 0;
        for (ModuleCountDTO item : moduleCountDTOList) {
            allCount += item.getDataCount();
        }
        moduleCountMap.put("all", allCount);
        return moduleCountMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id, String operator, String requestUrl, String requestMethod) {
        val testPlan = mapper.selectOneById(id);
        if (StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            this.deleteGroupByList(Collections.singletonList(testPlan.getId()));
        } else {
            mapper.deleteById(id);
            //级联删除
            TestPlanReportService testPlanReportService = CommonBeanFactory.getBean(TestPlanReportService.class);
            this.cascadeDeleteTestPlanIds(Collections.singletonList(id), testPlanReportService);
        }
        //记录日志
        testPlanLogService.saveDeleteLog(testPlan, operator, requestUrl, requestMethod);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editFollower(String testPlanId, String userId) {
        val queryChain = QueryChain.of(TestPlanFollower.class)
                .where(TestPlanFollower::getUserId).eq(userId)
                .and(TestPlanFollower::getTestPlanId).eq(testPlanId);
        val exists = queryChain.exists();
        if (exists) {
            LogicDeleteManager.execWithoutLogicDelete(() -> testPlanFollowerMapper.deleteByQuery(queryChain));
        } else {
            val build = TestPlanFollower.builder().testPlanId(testPlanId).userId(userId).build();
            testPlanFollowerMapper.insert(build);
        }
    }

    @Override
    public void archived(String id, String userId) {
        val testPlan = mapper.selectOneById(id);
        if (StringUtils.equalsAnyIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            //判断当前计划组下是否都已完成 (由于算法原因，只需要校验当前测试计划组即可）
            if (!this.isTestPlanCompleted(id)) {
                throw new CustomException(Translator.get("test_plan.group.not_plan"));
            }
            //测试计划组归档
            updateCompletedGroupStatus(testPlan.getId(), userId);
            //关闭定时任务
            this.deleteScheduleConfig(testPlan.getId());
        } else if (this.isTestPlanCompleted(id) && StringUtils.equalsIgnoreCase(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            //测试计划
            testPlan.setStatus(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
            testPlan.setUpdateUser(userId);
            mapper.update(testPlan);
        } else {
            throw new CustomException(Translator.get("test_plan.cannot.archived"));
        }
    }

    @Override
    public TestPlanDetailResponse detail(String id, String userId) {
        val testPlan = mapper.selectOneById(id);
        TestPlanDetailResponse response = new TestPlanDetailResponse();
        String moduleName = Translator.get("unplanned.plan");
        if (!ModuleConstants.DEFAULT_NODE_ID.equals(testPlan.getModuleId())) {
            TestPlanModule module = testPlanModuleMapper.selectOneById(testPlan.getModuleId());
            moduleName = module == null ? Translator.get("unplanned.plan") : module.getName();
            response.setModuleId(module == null ? ModuleConstants.DEFAULT_NODE_ID : module.getId());
        } else {
            response.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
        }
        //计划组只有几个参数
        response.setId(testPlan.getId());
        response.setNum(testPlan.getNum());
        response.setStatus(testPlan.getStatus());
        response.setName(testPlan.getName());
        response.setTags(testPlan.getTags());
        response.setModuleName(moduleName);
        response.setDescription(testPlan.getDescription());
        response.setType(testPlan.getType());
        if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_PLAN)) {
            //计划的 其他参数
            getGroupName(response, testPlan);
            response.setPlannedStartTime(testPlan.getPlannedStartTime());
            response.setPlannedEndTime(testPlan.getPlannedEndTime());
            getOtherConfig(response, testPlan);
            testPlanStatisticsService.calculateCaseCount(List.of(response));
        }
        val exists = QueryChain.of(Schedule.class).where(Schedule::getResourceId).eq(id).and(Schedule::getResourceType).eq("TEST_PLAN").exists();
        response.setUseSchedule(exists);
        //是否关注计划
        Boolean isFollow = checkIsFollowCase(id, userId);
        response.setFollowFlag(isFollow);
        val exists1 = QueryChain.of(TestPlanCollection.class).where(TestPlanCollection::getTestPlanId).eq(id).exists();
        if (!exists1) {
            List<TestPlanCollectionDTO> collections = initDefaultPlanCollection(id, userId);
            initResourceDefaultCollection(id, collections);
        }
        return response;
    }

    @Override
    public void batchDelete(TestPlanBatchProcessRequest request, String operator, String requestUrl, String requestMethod) {
        List<String> deleteIdList = request.getSelectIds();
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            val deleteTestPlanList = mapper.selectListByIds(deleteIdList);
            if (CollectionUtils.isNotEmpty(deleteTestPlanList)) {
                List<String> testPlanGroupList = new ArrayList<>();
                List<String> testPlanIdList = new ArrayList<>();
                for (TestPlan testPlan : deleteTestPlanList) {
                    if (StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                        testPlanGroupList.add(testPlan.getId());
                    } else {
                        testPlanIdList.add(testPlan.getId());
                    }
                }
                deleteByList(testPlanIdList);
                deleteGroupByList(testPlanGroupList);
                testPlanLogService.saveBatchLog(deleteTestPlanList, operator, requestUrl, requestMethod, OperationLogType.DELETE.name());
            }
        }
    }

    @Override
    public String copy(String id, String userId) {
        val testPlan = mapper.selectOneById(id);
        TestPlan copyPlan = null;
        if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            copyPlan = testPlanBatchOperationService.copyPlanGroup(testPlan, testPlan.getModuleId(), ModuleConstants.NODE_TYPE_DEFAULT, LocalDateTime.now(), userId);
        } else {
            copyPlan = testPlanBatchOperationService.copyPlan(testPlan, testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_TYPE_GROUP, LocalDateTime.now(), userId);
        }
        return copyPlan.getId();
    }

    @Override
    public void filterArchivedIds(TestPlanBatchRequest request) {
        if (CollectionUtils.isNotEmpty(request.getSelectIds())) {
            request.setSelectIds(queryChain().where(TEST_PLAN.ID.in(request.getSelectIds())
                    .and(TEST_PLAN.STATUS.ne("ARCHIVED"))).listAs(String.class));
        }
    }

    @Override
    public long batchCopy(TestPlanBatchRequest request, String userId, String url, String method) {
        // 目前计划的批量操作不支持全选所有页
        List<String> copyIds = request.getSelectIds();
        long copyCount = 0;
        if (CollectionUtils.isNotEmpty(copyIds)) {
            List<TestPlan> originalTestPlanList = queryChain().where(TEST_PLAN.ID.in(copyIds)).list();
            //批量复制时，不允许存在测试计划组下的测试计划。
            originalTestPlanList = originalTestPlanList.stream()
                    .filter(item -> StringUtils.equalsIgnoreCase(item.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID))
                    .toList();
            //日志
            if (CollectionUtils.isNotEmpty(originalTestPlanList)) {
                List<TestPlan> copyPlanList = testPlanBatchOperationService.batchCopy(originalTestPlanList, request.getTargetId(), request.getMoveType(), userId);
                copyCount = copyPlanList.size();
                testPlanLogService.saveBatchLog(copyPlanList, userId, url, method, OperationLogType.ADD.name());
            }
        }
        return copyCount;
    }

    private void deleteByList(List<String> testPlanIds) {
        if (CollectionUtils.isNotEmpty(testPlanIds)) {
            BatchProcessUtils.consumerByString(testPlanIds, (deleteIds) -> {
                mapper.deleteByQuery(queryChain().where(TEST_PLAN.ID.in(deleteIds)));
                //级联删除
                this.cascadeDeleteTestPlanIds(deleteIds, testPlanReportService);
            });
        }
    }

    @Async
    protected void initResourceDefaultCollection(String planId, List<TestPlanCollectionDTO> allCollections) {
        // 批处理旧数据
        List<TestPlanCollectionDTO> defaultCollections = new ArrayList<>();
        allCollections.forEach(allCollection -> defaultCollections.addAll(allCollection.getChildren()));
        Map<String, TestPlanResourceService> beansOfType = applicationContext.getBeansOfType(TestPlanResourceService.class);
        beansOfType.forEach((k, v) -> v.initResourceDefaultCollection(planId, defaultCollections));
    }

    private Boolean checkIsFollowCase(String testPlanId, String userId) {
        return QueryChain.of(TestPlanFollower.class).where(TEST_PLAN_FOLLOWER.TEST_PLAN_ID.eq(testPlanId)
                .and(TEST_PLAN_FOLLOWER.USER_ID.eq(userId))).exists();
    }

    private void getGroupName(TestPlanDetailResponse response, TestPlan testPlan) {
        if (!StringUtils.equalsIgnoreCase(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            TestPlan group = mapper.selectOneById(testPlan.getGroupId());
            response.setGroupId(testPlan.getGroupId());
            response.setGroupName(group.getName());
        }
    }

    private void getOtherConfig(TestPlanDetailResponse response, TestPlan testPlan) {
        val testPlanConfig = QueryChain.of(TestPlanConfig.class).where(TEST_PLAN_CONFIG.TEST_PLAN_ID.eq(testPlan.getId())).one();
        response.setAutomaticStatusUpdate(testPlanConfig.getAutomaticStatusUpdate());
        response.setRepeatCase(testPlanConfig.getRepeatCase());
        response.setPassThreshold(testPlanConfig.getPassThreshold());
    }

    private void deleteScheduleConfig(String testPlanId) {
        scheduleService.deleteByResourceId(testPlanId, TestPlanScheduleJob.getJobKey(testPlanId), TestPlanScheduleJob.getTriggerKey(testPlanId));
        //判断是不是测试计划组
        queryChain().where(TEST_PLAN.GROUP_ID.eq(testPlanId)).list()
                .forEach(item -> scheduleService.deleteByResourceId(testPlanId, TestPlanScheduleJob.getJobKey(testPlanId), TestPlanScheduleJob.getTriggerKey(testPlanId)));
    }

    private void updateCompletedGroupStatus(String id, String userId) {
        val testPlanList = QueryChain.of(TestPlan.class)
                .where(TEST_PLAN.GROUP_ID.eq(id)).list();
        if (CollectionUtils.isEmpty(testPlanList)) {
            throw new CustomException(Translator.get("test_plan.group.not_plan"));
        }
        List<String> ids = testPlanList.stream().map(TestPlan::getId).collect(Collectors.toList());
        ids.add(id);
        updateChain().set(TEST_PLAN.STATUS, TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)
                .set(TEST_PLAN.UPDATE_USER, userId)
                .where(TEST_PLAN.ID.in(ids)).update();
    }

    private boolean isTestPlanCompleted(String testPlanId) {
        TestPlanStatisticsResponse statisticsResponse = testPlanStatisticsService.calculateRate(Collections.singletonList(testPlanId)).get(0);
        return StringUtils.equalsIgnoreCase(statisticsResponse.getStatus(), TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED);
    }

    private void deleteGroupByList(List<String> testPlanGroupIds) {
        if (CollectionUtils.isNotEmpty(testPlanGroupIds)) {
            TestPlanReportService testPlanReportService = CommonBeanFactory.getBean(TestPlanReportService.class);
            assert testPlanReportService != null;
            BatchProcessUtils.consumerByString(testPlanGroupIds, (deleteGroupIds) -> {
                /*
                 * 计划组删除逻辑{第一版需求: 删除组, 组下的子计划Group置为None}:
                 * 1. 查询计划组下的全部子计划并删除(级联删除这些子计划的关联资源)
                 * 2. 删除所有计划组
                 */
                List<TestPlan> deleteGroupPlans = queryChain().where(TEST_PLAN.GROUP_ID.in(deleteGroupIds)).list();
                List<String> deleteGroupPlanIds = deleteGroupPlans.stream().map(TestPlan::getId).toList();
                List<String> allDeleteIds = ListUtils.union(deleteGroupIds, deleteGroupPlanIds);
                if (CollectionUtils.isNotEmpty(allDeleteIds)) {
                    // 级联删除子计划关联的资源(计划组不存在关联的资源,但是存在报告)
                    this.cascadeDeleteTestPlanIds(allDeleteIds, testPlanReportService);
                    mapper.deleteByQuery(queryChain().where(TEST_PLAN.ID.in(allDeleteIds)));
                }
            });
        }
    }

    private void cascadeDeleteTestPlanIds(List<String> testPlanIds, TestPlanReportService testPlanReportService) {
//删除当前计划对应的资源
        Map<String, TestPlanResourceService> subTypes = CommonBeanFactory.getBeansOfType(TestPlanResourceService.class);
        subTypes.forEach((k, t) -> t.deleteBatchByTestPlanId(testPlanIds));
        //删除测试计划配置
        testPlanConfigMapper.deleteByQuery(QueryChain.of(TestPlanConfig.class).where(TEST_PLAN_CONFIG.TEST_PLAN_ID.in(testPlanIds)));
        LogicDeleteManager.execWithoutLogicDelete(() -> testPlanFollowerMapper
                .deleteByQuery(QueryChain.of(TestPlanFollower.class).where(TestPlanFollower::getTestPlanId).in(testPlanIds)));
        //删除测试计划报告
        testPlanReportService.deleteByTestPlanIds(testPlanIds);
    }

    private QueryWrapper baseConditionQuery(QueryWrapper wrapper, TestPlanTableRequest request) {
        return wrapper.and(TEST_PLAN.MODULE_ID.in(request.getModuleIds()))
                .and(TEST_PLAN.GROUP_ID.eq("NONE").when("ALL".equals(request.getType())))
                .and((TEST_PLAN.GROUP_ID.eq("NONE").and(TEST_PLAN.TYPE.eq("TEST_PLAN"))).when("TEST_PLAN".equals(request.getType())))
                .and((TEST_PLAN.GROUP_ID.eq("NONE").and(TEST_PLAN.TYPE.eq("GROUP"))).when("GROUP".equals(request.getType())));
    }

    private QueryWrapper queryByTableRequest(QueryWrapper wrapper, TestPlanTableRequest request) {
        return wrapper.and(TEST_PLAN.NAME.like(request.getKeyword())
                        .or(TEST_PLAN.NUM.like(request.getKeyword()))
                        .or(TEST_PLAN.TAGS.like(request.getKeyword()))
                        .or(TEST_PLAN.ID.in(request.getKeywordFilterIds())))
                .and(TEST_PLAN.ID.in(request.getInnerIds()));
    }

    private List<ModuleCountDTO> countModuleIdByConditions(TestPlanTableRequest request) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.select(TEST_PLAN.MODULE_ID, QueryMethods.count(TEST_PLAN.ID).as("dataCount"))
                .from(TEST_PLAN)
                .where(TEST_PLAN.PROJECT_ID.eq(request.getProjectId()));
        wrapper = baseConditionQuery(wrapper, request);
        wrapper = queryByTableRequest(wrapper, request);
        wrapper.groupBy(TEST_PLAN.MODULE_ID);
        return mapper.selectListByQueryAs(wrapper, ModuleCountDTO.class);
    }

    private TestPlan savePlanDTO(TestPlanCreateRequest request, String operator) {
        val instance = TestPlanConverter.INSTANCE;
        //检查模块的合法性
        checkModule(request.getModuleId());
        if (StringUtils.equalsIgnoreCase(request.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)
                && !StringUtils.equalsIgnoreCase(request.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            throw new CustomException(Translator.get("test_plan.group.error"));
        }
        val createTestPlan = instance.toTestPlan(request);
        createTestPlan.setTags(ServiceUtils.parseTags(createTestPlan.getTags()));
        if (!StringUtils.equals(createTestPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            TestPlan groupPlan = testPlanGroupService.validateGroupCapacity(createTestPlan.getGroupId(), 1);
            // 判断测试计划组是否存在
            createTestPlan.setModuleId(groupPlan.getModuleId());
        }
        initTestPlanPos(createTestPlan);
        createTestPlan.setNum(NumGenerator.nextNum(request.getProjectId(), ApplicationNumScope.TEST_PLAN));
        createTestPlan.setCreateUser(operator);
        createTestPlan.setUpdateUser(operator);
        createTestPlan.setStatus(TestPlanConstants.TEST_PLAN_STATUS_NOT_ARCHIVED);
        mapper.insert(createTestPlan);
        val testPlanConfig = TestPlanConfig.builder()
                .testPlanId(createTestPlan.getId())
                .automaticStatusUpdate(request.isAutomaticStatusUpdate())
                .repeatCase(request.isRepeatCase())
                .passThreshold(request.getPassThreshold())
                .build();
        testPlanConfigMapper.insert(testPlanConfig);
        return createTestPlan;
    }

    private void initTestPlanPos(TestPlan testPlan) {
        if (StringUtils.equals(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            testPlan.setPos(this.getNextOrder(testPlan.getProjectId(), testPlan.getGroupId()));
        } else {
            testPlan.setPos(testPlanGroupService.getNextOrder(testPlan.getGroupId()));
        }
    }

    private Long getNextOrder(String projectId, String groupId) {
        long maxPos = selectMaxPosByProjectIdAndGroupId(projectId, groupId);
        return maxPos + ServiceUtils.POS_STEP;
    }

    private long selectMaxPosByProjectIdAndGroupId(String projectId, String groupId) {
        return queryChain().select("IF(MAX(pos) IS NULL, 0, MAX(pos)) AS pos").from(TEST_PLAN)
                .where(TEST_PLAN.PROJECT_ID.eq(projectId).and(TEST_PLAN.GROUP_ID.eq(groupId)))
                .oneAs(long.class);
    }

    private void initDefaultFilter(TestPlanTableRequest request) {
        List<String> defaultStatusList = new ArrayList<>();
        defaultStatusList.add(TestPlanConstants.TEST_PLAN_STATUS_NOT_ARCHIVED);
        if (request.getFilter() == null || !request.getFilter().containsKey("status")) {
            if (request.getFilter() == null) {
                request.setFilter(new HashMap<>() {{
                    this.put("status", defaultStatusList);
                }});
            } else {
                request.getFilter().put("status", defaultStatusList);
            }
        }

    }
}
