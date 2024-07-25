package cn.master.matrix.service.plan;

import cn.master.matrix.constants.CaseType;
import cn.master.matrix.constants.ExecStatus;
import cn.master.matrix.entity.TestPlanCaseExecuteHistory;
import cn.master.matrix.entity.TestPlanCollection;
import cn.master.matrix.entity.TestPlanFunctionalCase;
import cn.master.matrix.mapper.TestPlanCaseExecuteHistoryMapper;
import cn.master.matrix.mapper.TestPlanFunctionalCaseMapper;
import cn.master.matrix.payload.dto.plan.dto.TestPlanCaseRunResultCount;
import cn.master.matrix.payload.dto.plan.dto.TestPlanCollectionDTO;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.update.UpdateChain;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.master.matrix.entity.table.TestPlanFunctionalCaseTableDef.TEST_PLAN_FUNCTIONAL_CASE;
import static cn.master.matrix.entity.table.FunctionalCaseTableDef.FUNCTIONAL_CASE;
import static cn.master.matrix.entity.table.TestPlanCollectionTableDef.TEST_PLAN_COLLECTION;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
@Service
@RequiredArgsConstructor
public class TestPlanFunctionalCaseService extends TestPlanResourceService {
    private final TestPlanFunctionalCaseMapper testPlanFunctionalCaseMapper;
    private final TestPlanCaseExecuteHistoryMapper testPlanCaseExecuteHistoryMapper;

    @Override
    public void deleteBatchByTestPlanId(List<String> testPlanIdList) {
        if (CollectionUtils.isNotEmpty(testPlanIdList)) {
            LogicDeleteManager.execWithoutLogicDelete(() -> {
                val queryChain = QueryChain.of(TestPlanFunctionalCase.class).where(TestPlanFunctionalCase::getTestPlanId).in(testPlanIdList);
                testPlanFunctionalCaseMapper.deleteByQuery(queryChain);
            });
            val queryChain = QueryChain.of(TestPlanCaseExecuteHistory.class).where(TestPlanCaseExecuteHistory::getTestPlanId).in(testPlanIdList);
            testPlanCaseExecuteHistoryMapper.deleteByQuery(queryChain);
        }
    }

    @Override
    public Map<String, Long> caseExecResultCount(String testPlanId) {
        val runResultCounts = QueryChain.of(TestPlanFunctionalCase.class)
                .select(TEST_PLAN_FUNCTIONAL_CASE.LAST_EXEC_RESULT.as("result"))
                .select(QueryMethods.count(TEST_PLAN_FUNCTIONAL_CASE.ID).as("resultCount"))
                .from(TEST_PLAN_FUNCTIONAL_CASE)
                .innerJoin(FUNCTIONAL_CASE).on(FUNCTIONAL_CASE.ID.eq(TEST_PLAN_FUNCTIONAL_CASE.FUNCTIONAL_CASE_ID))
                .where(TEST_PLAN_FUNCTIONAL_CASE.TEST_PLAN_ID.eq(testPlanId))
                .groupBy(TEST_PLAN_FUNCTIONAL_CASE.LAST_EXEC_RESULT).listAs(TestPlanCaseRunResultCount.class);
        return runResultCounts.stream()
                .collect(Collectors.toMap(TestPlanCaseRunResultCount::getResult, TestPlanCaseRunResultCount::getResultCount));
    }

    @Override
    public void initResourceDefaultCollection(String planId, List<TestPlanCollectionDTO> defaultCollections) {
        TestPlanCollectionDTO defaultCollection = defaultCollections.stream()
                .filter(collection -> StringUtils.equals(collection.getType(), CaseType.FUNCTIONAL_CASE.getKey())
                        && !StringUtils.equals(collection.getParentId(), "NONE")).toList().getFirst();
        UpdateChain.of(TestPlanFunctionalCase.class)
                .set(TestPlanFunctionalCase::getTestPlanCollectionId, defaultCollection.getId())
                .where(TestPlanFunctionalCase::getTestPlanId).eq(planId)
                .update();
    }

    @Override
    public long copyResource(String originalTestPlanId, String newTestPlanId, Map<String, String> oldCollectionIdToNewCollectionId, String operator, LocalDateTime operatorTime) {
        List<TestPlanFunctionalCase> copyList = new ArrayList<>();
        val defaultCollectionId = QueryChain.of(TestPlanCollection.class)
                .select(TEST_PLAN_COLLECTION.ID).from(TEST_PLAN_COLLECTION)
                .where(TEST_PLAN_COLLECTION.TEST_PLAN_ID.eq(newTestPlanId)
                        .and(TEST_PLAN_COLLECTION.PARENT_ID.ne("NONE"))
                        .and(TEST_PLAN_COLLECTION.TYPE.eq(CaseType.SCENARIO_CASE.getKey())))
                .orderBy(TEST_PLAN_COLLECTION.POS.asc()).limit(1).oneAs(String.class);
        QueryChain.of(TestPlanFunctionalCase.class)
                .from(TEST_PLAN_FUNCTIONAL_CASE)
                .innerJoin(FUNCTIONAL_CASE).on(FUNCTIONAL_CASE.ID.eq(TEST_PLAN_FUNCTIONAL_CASE.FUNCTIONAL_CASE_ID))
                .where(TEST_PLAN_FUNCTIONAL_CASE.TEST_PLAN_ID.eq(originalTestPlanId)).list().forEach(originalCase -> {
                    TestPlanFunctionalCase newCase = new TestPlanFunctionalCase();
                    BeanUtils.copyProperties(originalCase, newCase);
                    newCase.setTestPlanId(newTestPlanId);
                    newCase.setCreateUser(operator);
                    newCase.setLastExecTime(null);
                    newCase.setTestPlanCollectionId(oldCollectionIdToNewCollectionId.get(newCase.getTestPlanCollectionId()) == null ? defaultCollectionId : oldCollectionIdToNewCollectionId.get(newCase.getTestPlanCollectionId()));
                    newCase.setLastExecResult(ExecStatus.PENDING.name());
                    copyList.add(newCase);
                });
        testPlanFunctionalCaseMapper.insertBatch(copyList);
        return copyList.size();
    }

    @Override
    public long getNextOrder(String collectionId) {
        val maxPos = QueryChain.of(TestPlanFunctionalCase.class)
                .select(QueryMethods.max(TEST_PLAN_FUNCTIONAL_CASE.POS))
                .from(TEST_PLAN_FUNCTIONAL_CASE)
                .where(TestPlanFunctionalCase::getTestPlanCollectionId).eq(collectionId).oneAs(Long.class);
        if (Objects.isNull(maxPos)) {
            return DEFAULT_NODE_INTERVAL_POS;
        } else {
            return maxPos + DEFAULT_NODE_INTERVAL_POS;
        }
    }

    @Override
    public void updatePos(String id, long pos) {
        UpdateChain.of(TestPlanFunctionalCase.class).set(TestPlanFunctionalCase::getPos, pos)
                .where(TestPlanFunctionalCase::getId).eq(id).update();
    }

    @Override
    public void refreshPos(String testPlanId) {
        val functionalCaseIdList = QueryChain.of(TestPlanFunctionalCase.class).select(TEST_PLAN_FUNCTIONAL_CASE.ID)
                .from(TEST_PLAN_FUNCTIONAL_CASE)
                .where(TestPlanFunctionalCase::getTestPlanId).eq(testPlanId)
                .orderBy(TEST_PLAN_FUNCTIONAL_CASE.POS.asc())
                .listAs(String.class);
        for (int i = 0; i < functionalCaseIdList.size(); i++) {
            UpdateChain.of(TestPlanFunctionalCase.class).set(TestPlanFunctionalCase::getPos, i * DEFAULT_NODE_INTERVAL_POS)
                    .where(TestPlanFunctionalCase::getId).eq(functionalCaseIdList.get(i)).update();
        }
    }
}
