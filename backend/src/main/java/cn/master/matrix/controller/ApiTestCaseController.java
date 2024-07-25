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
import cn.master.matrix.entity.ApiTestCase;
import cn.master.matrix.service.plan.ApiTestCaseService;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.io.Serializable;
import java.util.List;

/**
 * 接口用例 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-22T14:22:45.489615300
 */
@RestController
@Tag(name = "接口用例接口")
@RequestMapping("/apiTestCase")
public class ApiTestCaseController {

    @Autowired
    private ApiTestCaseService apiTestCaseService;

    /**
     * 添加接口用例。
     *
     * @param apiTestCase 接口用例
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("save")
    @Operation(description="保存接口用例")
    public boolean save(@RequestBody @Parameter(description="接口用例")ApiTestCase apiTestCase) {
        return apiTestCaseService.save(apiTestCase);
    }

    /**
     * 根据主键删除接口用例。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description="根据主键接口用例")
    public boolean remove(@PathVariable @Parameter(description="接口用例主键")Serializable id) {
        return apiTestCaseService.removeById(id);
    }

    /**
     * 根据主键更新接口用例。
     *
     * @param apiTestCase 接口用例
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @Operation(description="根据主键更新接口用例")
    public boolean update(@RequestBody @Parameter(description="接口用例主键")ApiTestCase apiTestCase) {
        return apiTestCaseService.updateById(apiTestCase);
    }

    /**
     * 查询所有接口用例。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(description="查询所有接口用例")
    public List<ApiTestCase> list() {
        return apiTestCaseService.list();
    }

    /**
     * 根据接口用例主键获取详细信息。
     *
     * @param id 接口用例主键
     * @return 接口用例详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description="根据主键获取接口用例")
    public ApiTestCase getInfo(@PathVariable Serializable id) {
        return apiTestCaseService.getById(id);
    }

    /**
     * 分页查询接口用例。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    @Operation(description="分页查询接口用例")
    public Page<ApiTestCase> page(@Parameter(description="分页信息")Page<ApiTestCase> page) {
        return apiTestCaseService.page(page);
    }

}
