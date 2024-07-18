package cn.master.matrix.controller;

import cn.master.matrix.constants.PermissionConstants;
import cn.master.matrix.handler.annotation.HasAuthorize;
import cn.master.matrix.payload.dto.ProjectDTO;
import cn.master.matrix.payload.dto.request.project.ProjectSwitchRequest;
import cn.master.matrix.payload.dto.user.UserDTO;
import cn.master.matrix.service.ProjectService;
import cn.master.matrix.util.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import cn.master.matrix.entity.Project;
import cn.master.matrix.service.CommonProjectService;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.io.Serializable;
import java.util.List;

/**
 * 项目 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-28T16:11:33.913527
 */
@RestController
@Tag(name = "项目管理")
@RequestMapping("/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/get/{id}")
    @Operation(summary = "项目管理-基本信息")
    @HasAuthorize(PermissionConstants.PROJECT_BASE_INFO_READ)
    public ProjectDTO getProject(@PathVariable String id) {
        return projectService.getProjectById(id);
    }

    @GetMapping("/list/options/{organizationId}")
    @Operation(summary = "根据组织ID获取所有有权限的项目")
    public List<Project> getUserProject(@PathVariable String organizationId) {
        return projectService.getUserProject(organizationId, SessionUtils.getUserId());
    }

    @GetMapping("/list/options/{organizationId}/{module}")
    @Operation(summary = "根据组织ID获取所有开启某个模块的所有有权限的项目")
    public List<Project> getUserProjectWidthModule(@PathVariable String organizationId, @PathVariable String module) {
        return projectService.getUserProjectWidthModule(organizationId, module, SessionUtils.getUserId());
    }

    @PostMapping("/switch")
    @Operation(summary = "切换项目")
    @HasAuthorize(PermissionConstants.PROJECT_BASE_INFO_READ)
    public UserDTO switchProject(@RequestBody ProjectSwitchRequest request) {
        return projectService.switchProject(request, SessionUtils.getUserId());
    }
}
