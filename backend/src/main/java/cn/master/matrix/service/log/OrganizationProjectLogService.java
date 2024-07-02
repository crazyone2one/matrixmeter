package cn.master.matrix.service.log;

import cn.master.matrix.constants.HttpMethodConstants;
import cn.master.matrix.constants.OperationLogConstants;
import cn.master.matrix.constants.OperationLogModule;
import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.entity.Project;
import cn.master.matrix.mapper.ProjectMapper;
import cn.master.matrix.payload.dto.LogDTO;
import cn.master.matrix.payload.dto.request.AddProjectRequest;
import cn.master.matrix.payload.dto.request.UpdateProjectNameRequest;
import cn.master.matrix.payload.dto.request.UpdateProjectRequest;
import cn.master.matrix.util.JsonUtils;
import lombok.RequiredArgsConstructor;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@RequiredArgsConstructor
public class OrganizationProjectLogService {
    private final ProjectMapper projectMapper;
    public LogDTO addLog(AddProjectRequest project) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                project.getOrganizationId(),
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.SETTING_ORGANIZATION_PROJECT,
                project.getName());

        dto.setOriginalValue(JsonUtils.toJsonBytes(project));
        return dto;
    }

    public LogDTO updateLog(UpdateProjectRequest request) {
        Project project = projectMapper.selectOneById(request.getId());
        if (project != null) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    project.getOrganizationId(),
                    project.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_ORGANIZATION_PROJECT,
                    request.getName());

            dto.setOriginalValue(JsonUtils.toJsonBytes(project));
            return dto;
        }
        return null;
    }
    public LogDTO renameLog(UpdateProjectNameRequest request) {
        Project project = projectMapper.selectOneById(request.getId());
        if (project != null) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    project.getOrganizationId(),
                    project.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_ORGANIZATION_PROJECT,
                    request.getName());

            dto.setOriginalValue(JsonUtils.toJsonBytes(project));
            return dto;
        }
        return null;
    }

    public LogDTO updateLog(String id) {
        Project project = projectMapper.selectOneById(id);
        if (project != null) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    project.getOrganizationId(),
                    project.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_ORGANIZATION_PROJECT,
                    project.getName());
            dto.setMethod(HttpMethodConstants.GET.name());

            dto.setOriginalValue(JsonUtils.toJsonBytes(project));
            return dto;
        }
        return null;
    }

    /**
     * 删除接口日志
     *
     * @param id
     * @return
     */
    public LogDTO deleteLog(String id) {
        Project project = projectMapper.selectOneById(id);
        if (project != null) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    project.getOrganizationId(),
                    id,
                    null,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.SETTING_ORGANIZATION_PROJECT,
                    project.getName());

            dto.setOriginalValue(JsonUtils.toJsonBytes(project));
            return dto;
        }
        return null;
    }

    /**
     * 恢复项目
     * @param id 接口请求参数
     * @return 日志详情
     */
    public LogDTO recoverLog(String id) {
        Project project = projectMapper.selectOneById(id);
        if (project != null) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    project.getOrganizationId(),
                    id,
                    null,
                    OperationLogType.RECOVER.name(),
                    OperationLogModule.SETTING_ORGANIZATION_PROJECT,
                    project.getName());
            dto.setOriginalValue(JsonUtils.toJsonBytes(project));
            return dto;
        }
        return null;
    }
}
