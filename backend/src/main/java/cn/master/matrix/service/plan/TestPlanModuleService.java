package cn.master.matrix.service.plan;

import cn.master.matrix.constants.ModuleConstants;
import cn.master.matrix.entity.TestPlan;
import cn.master.matrix.entity.TestPlanModule;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.mapper.TestPlanModuleMapper;
import cn.master.matrix.payload.dto.BaseTreeNode;
import cn.master.matrix.payload.dto.plan.request.TestPlanBatchProcessRequest;
import cn.master.matrix.payload.dto.plan.request.TestPlanModuleCreateRequest;
import cn.master.matrix.payload.dto.plan.request.TestPlanModuleUpdateRequest;
import cn.master.matrix.payload.dto.project.ModuleCountDTO;
import cn.master.matrix.payload.dto.project.NodeSortDTO;
import cn.master.matrix.payload.dto.request.NodeMoveRequest;
import cn.master.matrix.service.ModuleTreeService;
import cn.master.matrix.service.log.TestPlanModuleLogService;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.master.matrix.entity.table.TestPlanModuleTableDef.TEST_PLAN_MODULE;
import static cn.master.matrix.entity.table.TestPlanTableDef.TEST_PLAN;

/**
 * @author Created by 11's papa on 07/22/2024
 **/
@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class TestPlanModuleService extends ModuleTreeService {
    private final TestPlanModuleMapper testPlanModuleMapper;
    private final TestPlanModuleLogService testPlanModuleLogService;
    @Lazy
    private final TestPlanService testPlanService;

    @Transactional(rollbackFor = Exception.class)
    public String add(TestPlanModuleCreateRequest request, String operator, String requestUrl, String requestMethod) {
        val testPlanModule = TestPlanModule.builder()
                .name(request.getName())
                .parentId(request.getParentId())
                .projectId(request.getProjectId())
                .build();
        checkDataValidity(testPlanModule);
        testPlanModule.setPos(this.countPos(request.getParentId()));
        testPlanModule.setCreateUser(operator);
        testPlanModule.setUpdateUser(operator);
        testPlanModuleMapper.insert(testPlanModule);
        testPlanModuleLogService.saveAddLog(testPlanModule, operator, requestUrl, requestMethod);
        return testPlanModule.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(TestPlanModuleUpdateRequest request, String userId, String requestUrl, String requestMethod) {
        val module = testPlanModuleMapper.selectOneById(request.getId());
        val testPlanModule = TestPlanModule.builder()
                .id(request.getId())
                .name(request.getName())
                .parentId(module.getParentId())
                .projectId(module.getProjectId())
                .build();
        checkDataValidity(testPlanModule);
        testPlanModule.setUpdateUser(userId);
        testPlanModuleMapper.update(testPlanModule);
        val selected = testPlanModuleMapper.selectOneById(request.getId());
        testPlanModuleLogService.saveUpdateLog(module, selected, module.getProjectId(), userId, requestUrl, requestMethod);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteModule(String deleteId, String operator, String requestUrl, String requestMethod) {
        TestPlanModule deleteModule = testPlanModuleMapper.selectOneById(deleteId);
        if (deleteModule != null) {
            this.deleteModule(Collections.singletonList(deleteId), deleteModule.getProjectId(), operator, requestUrl, requestMethod);
            //记录日志
            testPlanModuleLogService.saveDeleteLog(deleteModule, operator, requestUrl, requestMethod);
        }
    }

    @org.springframework.context.annotation.Lazy
    @Transactional(rollbackFor = Exception.class)
    public void deleteModule(List<String> deleteIds, String projectId, String operator, String requestUrl, String requestMethod) {
        if (CollectionUtils.isEmpty(deleteIds)) {
            return;
        }
        testPlanModuleMapper.deleteBatchByIds(deleteIds);
        List<String> planDeleteIds = QueryChain.of(TestPlan.class).select(TEST_PLAN.ID)
                .from(TEST_PLAN).where(TEST_PLAN.MODULE_ID.in(deleteIds)).listAs(String.class);
        if (CollectionUtils.isNotEmpty(planDeleteIds)) {
            TestPlanBatchProcessRequest request = new TestPlanBatchProcessRequest();
            request.setModuleIds(deleteIds);
            request.setSelectAll(false);
            request.setProjectId(projectId);
            request.setSelectIds(planDeleteIds);
            testPlanService.batchDelete(request, operator, requestUrl, requestMethod);
        }

        List<String> childrenIds = QueryChain.of(TestPlanModule.class)
                .select(TEST_PLAN_MODULE.ID).from(TEST_PLAN_MODULE)
                .where(TEST_PLAN_MODULE.PARENT_ID.in(deleteIds)).listAs(String.class);
        if (CollectionUtils.isNotEmpty(childrenIds)) {
            deleteModule(childrenIds, projectId, operator, requestUrl, requestMethod);
        }
    }

    private void checkDataValidity(TestPlanModule module) {
        QueryWrapper wrapper = new QueryWrapper();
        if (!StringUtils.equalsIgnoreCase(module.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
            wrapper.where(TEST_PLAN_MODULE.ID.eq(module.getParentId()));
            val countByQuery = testPlanModuleMapper.selectCountByQuery(wrapper);
            if (countByQuery == 0) {
                throw new CustomException(Translator.get("parent.node.not_blank"));
            }
            wrapper.clear();
            if (StringUtils.isNotBlank(module.getProjectId())) {
                wrapper.where(TEST_PLAN_MODULE.PROJECT_ID.eq(module.getProjectId())
                        .and(TEST_PLAN_MODULE.ID.eq(module.getParentId())));
                val countByQuery1 = testPlanModuleMapper.selectCountByQuery(wrapper);
                if (countByQuery1 == 0) {
                    throw new CustomException(Translator.get("project.cannot.match.parent"));
                }
                wrapper.clear();
            }
        }
        wrapper.where(TEST_PLAN_MODULE.PARENT_ID.eq(module.getParentId())
                .and(TEST_PLAN_MODULE.PROJECT_ID.eq(module.getProjectId()))
                .and(TEST_PLAN_MODULE.NAME.eq(module.getName()))
                .and(TEST_PLAN_MODULE.ID.ne(module.getId())));
        if (testPlanModuleMapper.selectCountByQuery(wrapper) > 0) {
            throw new CustomException(Translator.get("node.name.repeat"));
        }
    }

    protected Long countPos(String parentId) {
        Long maxPos = QueryChain.of(TestPlanModule.class)
                .select(QueryMethods.max(TEST_PLAN_MODULE.POS))
                .from(TEST_PLAN_MODULE)
                .where(TEST_PLAN_MODULE.PARENT_ID.eq(parentId)).oneAs(Long.class);
        if (maxPos == null) {
            return LIMIT_POS;
        } else {
            return maxPos + LIMIT_POS;
        }
    }

    @Override
    public void updatePos(String id, long pos) {

    }

    @Override
    public void refreshPos(String parentId) {

    }

    public Map<String, Long> getModuleCountMap(String projectId, List<ModuleCountDTO> moduleCountDTOList) {
        //构建模块树，并计算每个节点下的所有数量（包含子节点）
        List<BaseTreeNode> treeNodeList = getTreeOnlyIdsAndResourceCount(projectId, moduleCountDTOList);
        //通过广度遍历的方式构建返回值
        return super.getIdCountMapByBreadth(treeNodeList);
    }

    private List<BaseTreeNode> getTreeOnlyIdsAndResourceCount(String projectId, List<ModuleCountDTO> moduleCountDTOList) {
        //节点内容只有Id和parentId
        List<BaseTreeNode> fileModuleList = QueryChain.of(TestPlanModule.class)
                .where(TEST_PLAN_MODULE.PROJECT_ID.eq(projectId))
                .orderBy(TEST_PLAN_MODULE.POS.desc())
                .listAs(BaseTreeNode.class);
        return super.buildTreeAndCountResource(fileModuleList, moduleCountDTOList, true, Translator.get("default.module"));
    }
    public void moveNode(NodeMoveRequest request, String currentUser, String requestUrl, String requestMethod) {
        NodeSortDTO nodeSortDTO = super.getNodeSortDTO(request,
                testPlanModuleMapper::selectBaseModuleById,
                testPlanModuleMapper::selectModuleByParentIdAndPosOperator);
    }
}
