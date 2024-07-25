package cn.master.matrix.service.plan;

import cn.master.matrix.constants.TestPlanConstants;
import cn.master.matrix.entity.TestPlan;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.mapper.TestPlanMapper;
import cn.master.matrix.payload.dto.project.MoveNodeSortDTO;
import cn.master.matrix.payload.dto.request.PosRequest;
import cn.master.matrix.util.ServiceUtils;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static cn.master.matrix.entity.table.TestPlanTableDef.TEST_PLAN;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
@Service
@RequiredArgsConstructor
public class TestPlanGroupService extends TestPlanSortService {
    private final TestPlanMapper testPlanMapper;
    private static final int MAX_CHILDREN_COUNT = 20;

    @Override
    public long getNextOrder(String groupId) {
        long maxPos = selectMaxPosByGroupId(groupId);
        return maxPos + ServiceUtils.POS_STEP;
    }

    private long selectMaxPosByGroupId(String groupId) {
        return QueryChain.of(TestPlan.class)
                .select("IF(MAX(pos) IS NULL, 0, MAX(pos)) AS pos")
                .from(TEST_PLAN)
                .where(TEST_PLAN.GROUP_ID.eq(groupId))
                .objAs(Long.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePos(String id, long pos) {
        UpdateChain.of(TestPlan.class).set(TestPlan::getPos, pos).where(TEST_PLAN.ID.eq(id)).update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshPos(String rangeId) {
        QueryWrapper wrapper = new QueryWrapper();
        if (StringUtils.contains(rangeId, "_")) {
            String[] rangeIds = rangeId.split("_");
            String projectId = rangeIds[0];
            String testPlanGroupId = rangeIds[1];
            wrapper.where(TEST_PLAN.PROJECT_ID.eq(projectId).and(TEST_PLAN.GROUP_ID.eq(testPlanGroupId)));
        } else {
            wrapper.where(TEST_PLAN.GROUP_ID.eq(rangeId));
        }
        val testPlans = testPlanMapper.selectListByQuery(wrapper);
        long pos = 1;
        for (TestPlan testPlanItem : testPlans) {
            updatePos(testPlanItem.getId(), pos * ServiceUtils.POS_STEP);
            pos++;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void sort(PosRequest request) {
        TestPlan dropPlan = testPlanMapper.selectOneById(request.getMoveId());
        TestPlan targetPlan = testPlanMapper.selectOneById(request.getTargetId());

        // 校验排序的参数 （暂时不支持测试计划的移入移出）
        validateMoveRequest(dropPlan, targetPlan, request.getMoveMode());
        MoveNodeSortDTO sortDTO = super.getNodeSortDTO(
                targetPlan.getGroupId(),
                this.getNodeMoveRequest(request, true),
                testPlanMapper::selectDragInfoById,
                testPlanMapper::selectNodeByPosOperator
        );
        if (StringUtils.equalsIgnoreCase(sortDTO.getSortRangeId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            sortDTO.setSortRangeId(request.getProjectId() + "_" + TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID);
        }
        //判断是否需要刷新排序
        if (this.needRefreshBeforeSort(sortDTO.getPreviousNode(), sortDTO.getNextNode())) {
            this.refreshPos(sortDTO.getSortRangeId());
            dropPlan = testPlanMapper.selectOneById(request.getMoveId());
            targetPlan = testPlanMapper.selectOneById(request.getTargetId());
            sortDTO = super.getNodeSortDTO(
                    targetPlan.getGroupId(),
                    this.getNodeMoveRequest(request, true),
                    testPlanMapper::selectDragInfoById,
                    testPlanMapper::selectNodeByPosOperator
            );
            if (StringUtils.equalsIgnoreCase(sortDTO.getSortRangeId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
                sortDTO.setSortRangeId(request.getProjectId() + "_" + TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID);
            }
        }
        this.sort(sortDTO);
    }

    private void validateMoveRequest(TestPlan dropPlan, TestPlan targetPlan, String moveType) {
        //测试计划组不能进行移动操作
        if (dropPlan == null) {
            throw new CustomException(Translator.get("test_plan.drag.node.error"));
        }
        if (targetPlan == null) {
            throw new CustomException(Translator.get("test_plan.drag.node.error"));
        }
    }

    public TestPlan validateGroupCapacity(String groupId, int size) {
        // 判断测试计划组是否存在
        TestPlan groupPlan = testPlanMapper.selectOneById(groupId);
        if (groupPlan == null) {
            throw new CustomException(Translator.get("test_plan.group.error"));
        }
        if (!StringUtils.equalsIgnoreCase(groupPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            throw new CustomException(Translator.get("test_plan.group.error"));
        }
        //判断并未归档
        if (StringUtils.equalsIgnoreCase(groupPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
            throw new CustomException(Translator.get("test_plan.group.error"));
        }
        //判断测试计划组下的测试计划数量是否超过20
        val count = QueryChain.of(TestPlan.class).where(TEST_PLAN.GROUP_ID.eq(groupId)).count();
        if (count + size > 20) {
            throw new CustomException(Translator.getWithArgs("test_plan.group.children.max", MAX_CHILDREN_COUNT));
        }
        return groupPlan;
    }
}
