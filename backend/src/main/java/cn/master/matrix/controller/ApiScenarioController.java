package cn.master.matrix.controller;

import com.mybatisflex.core.paginate.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import cn.master.matrix.entity.ApiScenario;
import cn.master.matrix.service.plan.ApiScenarioService;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.io.Serializable;
import java.util.List;

/**
 * 场景 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-22T14:24:16.151644200
 */
@RestController
@Tag(name = "场景接口")
@RequestMapping("/apiScenario")
public class ApiScenarioController {

    @Autowired
    private ApiScenarioService apiScenarioService;

    /**
     * 添加场景。
     *
     * @param apiScenario 场景
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("save")
    @Operation(description="保存场景")
    public boolean save(@RequestBody @Parameter(description="场景")ApiScenario apiScenario) {
        return apiScenarioService.save(apiScenario);
    }

    /**
     * 根据主键删除场景。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description="根据主键场景")
    public boolean remove(@PathVariable @Parameter(description="场景主键")Serializable id) {
        return apiScenarioService.removeById(id);
    }

    /**
     * 根据主键更新场景。
     *
     * @param apiScenario 场景
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @Operation(description="根据主键更新场景")
    public boolean update(@RequestBody @Parameter(description="场景主键")ApiScenario apiScenario) {
        return apiScenarioService.updateById(apiScenario);
    }

    /**
     * 查询所有场景。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(description="查询所有场景")
    public List<ApiScenario> list() {
        return apiScenarioService.list();
    }

    /**
     * 根据场景主键获取详细信息。
     *
     * @param id 场景主键
     * @return 场景详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description="根据主键获取场景")
    public ApiScenario getInfo(@PathVariable Serializable id) {
        return apiScenarioService.getById(id);
    }

    /**
     * 分页查询场景。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    @Operation(description="分页查询场景")
    public Page<ApiScenario> page(@Parameter(description="分页信息")Page<ApiScenario> page) {
        return apiScenarioService.page(page);
    }

}
