package cn.master.matrix.mapper;

import cn.master.matrix.payload.dto.BaseModule;
import cn.master.matrix.payload.dto.project.NodeSortQueryParam;
import com.mybatisflex.core.BaseMapper;
import cn.master.matrix.entity.TestPlanModule;
import org.apache.ibatis.annotations.Select;

/**
 * 测试计划模块 映射层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-19T15:14:20.016505100
 */
public interface TestPlanModuleMapper extends BaseMapper<TestPlanModule> {
    @Select("SELECT id, name, pos, project_Id, parent_id FROM test_plan_module WHERE id = #{0}")
    BaseModule selectBaseModuleById(String dragNodeId);

    BaseModule selectModuleByParentIdAndPosOperator(NodeSortQueryParam nodeSortQueryParam);
}
