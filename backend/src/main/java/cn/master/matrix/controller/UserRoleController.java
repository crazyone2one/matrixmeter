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
import cn.master.matrix.entity.UserRole;
import cn.master.matrix.service.BaseUserRoleService;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.io.Serializable;
import java.util.List;

/**
 * 用户组 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-27T16:19:57.911393700
 */
@RestController
@Tag(name = "用户组接口")
@RequestMapping("/userRole")
public class UserRoleController {

    @Autowired
    private BaseUserRoleService baseUserRoleService;

    /**
     * 添加用户组。
     *
     * @param userRole 用户组
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("save")
    @Operation(description="保存用户组")
    public boolean save(@RequestBody @Parameter(description="用户组")UserRole userRole) {
        return baseUserRoleService.save(userRole);
    }

    /**
     * 根据主键删除用户组。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description="根据主键用户组")
    public boolean remove(@PathVariable @Parameter(description="用户组主键")Serializable id) {
        return baseUserRoleService.removeById(id);
    }

    /**
     * 根据主键更新用户组。
     *
     * @param userRole 用户组
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @Operation(description="根据主键更新用户组")
    public boolean update(@RequestBody @Parameter(description="用户组主键")UserRole userRole) {
        return baseUserRoleService.updateById(userRole);
    }

    /**
     * 查询所有用户组。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(description="查询所有用户组")
    public List<UserRole> list() {
        return baseUserRoleService.list();
    }

    /**
     * 根据用户组主键获取详细信息。
     *
     * @param id 用户组主键
     * @return 用户组详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description="根据主键获取用户组")
    public UserRole getInfo(@PathVariable Serializable id) {
        return baseUserRoleService.getById(id);
    }

    /**
     * 分页查询用户组。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    @Operation(description="分页查询用户组")
    public Page<UserRole> page(@Parameter(description="分页信息")Page<UserRole> page) {
        return baseUserRoleService.page(page);
    }

}
