package cn.master.matrix.mapper;

import cn.master.matrix.entity.TestPlan;
import cn.master.matrix.payload.dto.project.DropNode;
import cn.master.matrix.payload.dto.project.NodeSortQueryParam;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * 测试计划 映射层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-19T14:49:06.626698900
 */
public interface TestPlanMapper extends BaseMapper<TestPlan> {
    @Select("SELECT id, pos FROM test_plan WHERE id = #{0}")
    DropNode selectDragInfoById(String s);

    DropNode selectNodeByPosOperator(NodeSortQueryParam nodeSortQueryParam);
}
