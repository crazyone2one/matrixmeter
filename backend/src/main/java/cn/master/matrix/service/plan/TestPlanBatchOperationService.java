package cn.master.matrix.service.plan;

import cn.master.matrix.constants.ApplicationNumScope;
import cn.master.matrix.constants.ModuleConstants;
import cn.master.matrix.constants.TestPlanConstants;
import cn.master.matrix.entity.Schedule;
import cn.master.matrix.entity.TestPlan;
import cn.master.matrix.entity.TestPlanCollection;
import cn.master.matrix.entity.TestPlanConfig;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.handler.schedule.TestPlanScheduleJob;
import cn.master.matrix.handler.uid.IDGenerator;
import cn.master.matrix.mapper.TestPlanCollectionMapper;
import cn.master.matrix.mapper.TestPlanConfigMapper;
import cn.master.matrix.mapper.TestPlanMapper;
import cn.master.matrix.payload.dto.request.BaseScheduleConfigRequest;
import cn.master.matrix.service.ScheduleService;
import cn.master.matrix.util.CommonBeanFactory;
import cn.master.matrix.util.JsonUtils;
import cn.master.matrix.util.NumGenerator;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.query.QueryChain;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static cn.master.matrix.entity.table.TestPlanTableDef.TEST_PLAN;

/**
 * @author Created by 11's papa on 07/23/2024
 **/
@Service
@RequiredArgsConstructor
public class TestPlanBatchOperationService extends TestPlanBaseUtilsService {
    private final TestPlanMapper testPlanMapper;
    private final TestPlanConfigMapper testPlanConfigMapper;
    private final TestPlanGroupService testPlanGroupService;
    private final TestPlanCollectionMapper testPlanCollectionMapper;
    private final ApplicationContext applicationContext;
    private final TestPlanScheduleService testPlanScheduleService;
    private final ScheduleService scheduleService;

    @Transactional(rollbackFor = Exception.class)
    public TestPlan copyPlanGroup(TestPlan originalGroup, String targetId, String targetType, LocalDateTime operatorTime, String operator) {
        if (StringUtils.equalsIgnoreCase(targetType, TestPlanConstants.TEST_PLAN_TYPE_GROUP)
                || StringUtils.equalsIgnoreCase(originalGroup.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
            throw new CustomException(Translator.get("test_plan.group.error"));
        }
        super.checkModule(targetId);
        val childList = QueryChain.of(TestPlan.class).where(TEST_PLAN.GROUP_ID.eq(originalGroup.getId())
                        .and(TEST_PLAN.STATUS.ne(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)))
                .orderBy(TEST_PLAN.POS.asc()).list();
        TestPlan testPlanGroup = new TestPlan();
        BeanUtils.copyProperties(originalGroup, testPlanGroup);
        testPlanGroup.setNum(NumGenerator.nextNum(testPlanGroup.getProjectId(), ApplicationNumScope.TEST_PLAN));
        testPlanGroup.setName(this.getCopyName(originalGroup.getName(), originalGroup.getNum(), testPlanGroup.getNum()));
        testPlanGroup.setCreateUser(operator);
        testPlanGroup.setCreateTime(operatorTime);
        testPlanGroup.setUpdateUser(operator);
        testPlanGroup.setUpdateTime(operatorTime);
        testPlanGroup.setModuleId(targetId);
        testPlanGroup.setPos(testPlanGroupService.getNextOrder(originalGroup.getGroupId()));
        testPlanGroup.setActualEndTime(null);
        testPlanGroup.setActualStartTime(null);
        testPlanGroup.setStatus(TestPlanConstants.TEST_PLAN_STATUS_NOT_ARCHIVED);
        testPlanMapper.insert(testPlanGroup);
        extracted(originalGroup, testPlanGroup);
        for (TestPlan child : childList) {
            copyPlan(child, testPlanGroup.getId(), TestPlanConstants.TEST_PLAN_TYPE_GROUP, operatorTime, operator);
        }
        copySchedule(originalGroup.getId(), testPlanGroup.getId(), operator);
        return testPlanGroup;
    }

    private void copySchedule(String resourceId, String targetId, String operator) {
        Schedule originalSchedule = scheduleService.getScheduleByResource(resourceId, TestPlanScheduleJob.class.getName());
        if (originalSchedule != null) {
            // 来源的 "计划/计划组" 存在定时任务即复制, 无论开启或关闭
            BaseScheduleConfigRequest scheduleRequest = new BaseScheduleConfigRequest();
            scheduleRequest.setEnable(originalSchedule.getEnable());
            scheduleRequest.setCron(originalSchedule.getValue());
            // noinspection unchecked
            scheduleRequest.setRunConfig(JsonUtils.parseMap(originalSchedule.getConfig()));
            scheduleRequest.setResourceId(targetId);
            testPlanScheduleService.scheduleConfig(scheduleRequest, operator);
        }
    }

    private String getCopyName(String name, long oldNum, long newNum) {
        if (!StringUtils.startsWith(name, "copy_")) {
            name = "copy_" + name;
        }
        if (name.length() > 250) {
            name = name.substring(0, 200) + "...";
        }
        if (StringUtils.endsWith(name, "_" + oldNum)) {
            name = StringUtils.substringBeforeLast(name, "_" + oldNum);
        }
        name = name + "_" + newNum;
        return name;
    }

    public TestPlan copyPlan(TestPlan originalTestPlan, String targetId, String targetType, LocalDateTime operatorTime, String operator) {
        //已归档的无法操作
        if (StringUtils.equalsIgnoreCase(originalTestPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
            throw new CustomException(Translator.get("test_plan.error"));
        }
        String moduleId = originalTestPlan.getModuleId();
        String groupId = originalTestPlan.getGroupId();
        String sortRangeId = TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID;
        if (StringUtils.equals(targetType, TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            if (!StringUtils.equalsIgnoreCase(targetId, TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
                TestPlan group = testPlanMapper.selectOneById(targetId);
                //如果目标ID是测试计划组， 需要进行容量校验
                if (!StringUtils.equalsIgnoreCase(targetType, ModuleConstants.NODE_TYPE_DEFAULT)) {
                    testPlanGroupService.validateGroupCapacity(targetId, 1);
                }
                moduleId = group.getModuleId();
                sortRangeId = targetId;
            }
            groupId = targetId;
        } else {
            super.checkModule(targetId);
            moduleId = targetId;
        }
        TestPlan testPlan = new TestPlan();
        BeanUtils.copyProperties(originalTestPlan, testPlan);
        testPlan.setNum(NumGenerator.nextNum(testPlan.getProjectId(), ApplicationNumScope.TEST_PLAN));
        testPlan.setName(this.getCopyName(originalTestPlan.getName(), originalTestPlan.getNum(), testPlan.getNum()));
        testPlan.setCreateUser(operator);
        testPlan.setCreateTime(operatorTime);
        testPlan.setUpdateUser(operator);
        testPlan.setUpdateTime(operatorTime);
        testPlan.setModuleId(moduleId);
        testPlan.setGroupId(groupId);
        testPlan.setPos(testPlanGroupService.getNextOrder(sortRangeId));
        testPlan.setActualEndTime(null);
        testPlan.setActualStartTime(null);
        testPlan.setStatus(TestPlanConstants.TEST_PLAN_STATUS_NOT_ARCHIVED);
        testPlanMapper.insert(testPlan);
        extracted(originalTestPlan, testPlan);
        val testPlanCollectionList = QueryChain.of(TestPlanCollection.class)
                .where(TestPlanCollection::getTestPlanId).eq(originalTestPlan.getId())
                .and(TestPlanCollection::getParentId).eq(TestPlanConstants.DEFAULT_PARENT_ID).list();
        Map<String, String> oldCollectionIdToNewCollectionId = new HashMap<>();
        if (CollectionUtils.isEmpty(testPlanCollectionList)) {
            Objects.requireNonNull(CommonBeanFactory.getBean(TestPlanService.class)).initDefaultPlanCollection(testPlan.getId(), operator);
        } else {
            List<TestPlanCollection> newTestPlanCollectionList = new ArrayList<>();
            for (TestPlanCollection testPlanCollection : testPlanCollectionList) {
                TestPlanCollection newTestPlanCollection = new TestPlanCollection();
                BeanUtils.copyProperties(testPlanCollection, newTestPlanCollection);
                newTestPlanCollection.setId(IDGenerator.nextStr());
                newTestPlanCollection.setTestPlanId(testPlan.getId());
                newTestPlanCollection.setCreateUser(operator);
                newTestPlanCollection.setCreateTime(operatorTime);
                newTestPlanCollectionList.add(newTestPlanCollection);

                //查找测试集信息
                List<TestPlanCollection> children = QueryChain.of(TestPlanCollection.class)
                        .where(TestPlanCollection::getParentId).eq(testPlanCollection.getId()).list();
                for (TestPlanCollection child : children) {
                    TestPlanCollection childCollection = new TestPlanCollection();
                    BeanUtils.copyProperties(child, childCollection);
                    childCollection.setParentId(newTestPlanCollection.getId());
                    childCollection.setTestPlanId(testPlan.getId());
                    childCollection.setCreateUser(operator);
                    childCollection.setCreateTime(operatorTime);
                    newTestPlanCollectionList.add(childCollection);
                    oldCollectionIdToNewCollectionId.put(child.getId(), childCollection.getId());
                }
            }
            testPlanCollectionMapper.insertBatch(newTestPlanCollectionList);
        }
        //测试用例信息
        Map<String, TestPlanResourceService> beansOfType = applicationContext.getBeansOfType(TestPlanResourceService.class);
        beansOfType.forEach((k, v) -> v.copyResource(originalTestPlan.getId(), testPlan.getId(), oldCollectionIdToNewCollectionId, operator, operatorTime));
        copySchedule(originalTestPlan.getId(), testPlan.getId(), operator);
        return testPlan;
    }

    private void extracted(TestPlan originalTestPlan, TestPlan testPlanGroup) {
        val originalTestPlanConfig = QueryChain.of(TestPlanConfig.class).where(TestPlanConfig::getTestPlanId).eq(originalTestPlan.getId()).one();
        if (originalTestPlanConfig != null) {
            TestPlanConfig newTestPlanConfig = new TestPlanConfig();
            BeanUtils.copyProperties(originalTestPlanConfig, newTestPlanConfig);
            newTestPlanConfig.setTestPlanId(testPlanGroup.getId());
            testPlanConfigMapper.insertSelective(newTestPlanConfig);
        }
    }

    public List<TestPlan> batchCopy(List<TestPlan> originalPlanList, String targetId, String targetType, String userId) {
        List<TestPlan> copyPlanResult = new ArrayList<>();
        //如果目标ID是测试计划组， 需要进行容量校验
        if (!StringUtils.equalsIgnoreCase(targetType, ModuleConstants.NODE_TYPE_DEFAULT)) {
            testPlanGroupService.validateGroupCapacity(targetId, originalPlanList.size());
        }
        /*
            此处不进行批量处理，原因有两点：
            1） 测试计划内（或者测试计划组内）数据量不可控，选择批量操作时更容易出现数据太多不走索引、数据太多内存溢出等问题。不批量操作可以减少这些问题出现的概率，代价是速度会变慢。
            2） 作为数据量不可控的操作，如果数据量少，不采用批量处理也不会消耗太多时间。如果数据量多，就会容易出现1的问题。并且本人不建议针对不可控数据量的数据支持批量操作。
         */
        for (TestPlan copyPlan : originalPlanList) {
            if (StringUtils.equalsIgnoreCase(copyPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                copyPlanResult.add(this.copyPlanGroup(copyPlan, targetId, targetType, LocalDateTime.now(), userId));
            } else {
                copyPlanResult.add(this.copyPlan(copyPlan, targetId, targetType, LocalDateTime.now(), userId));
            }
        }
        return copyPlanResult;
    }
}
