package cn.master.matrix.service.log;

import cn.master.matrix.constants.OperationLogModule;
import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.entity.Project;
import cn.master.matrix.entity.TestPlanModule;
import cn.master.matrix.mapper.ProjectMapper;
import cn.master.matrix.payload.LogDTOBuilder;
import cn.master.matrix.payload.dto.BaseModule;
import cn.master.matrix.payload.dto.LogDTO;
import cn.master.matrix.payload.dto.project.NodeSortDTO;
import cn.master.matrix.service.OperationLogService;
import cn.master.matrix.util.JsonUtils;
import cn.master.matrix.util.Translator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * @author Created by 11's papa on 07/24/2024
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanModuleLogService {
    private final String logModule = OperationLogModule.TEST_PLAN_MODULE;

    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;

    public void saveAddLog(TestPlanModule module, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectOneById(module.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(module.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.ADD.name())
                .module(logModule)
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(module.getId())
                .content(module.getName())
                .originalValue(JsonUtils.toJsonBytes(module))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveUpdateLog(TestPlanModule oldModule, TestPlanModule newModule, String projectId, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectOneById(projectId);
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(projectId)
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(logModule)
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(newModule.getId())
                .content(newModule.getName())
                .originalValue(JsonUtils.toJsonBytes(oldModule))
                .modifiedValue(JsonUtils.toJsonBytes(newModule))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveDeleteLog(TestPlanModule deleteModule, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectOneById(deleteModule.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(deleteModule.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.DELETE.name())
                .module(logModule)
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(deleteModule.getId())
                .content(deleteModule.getName() + " " + Translator.get("log.delete_module"))
                .originalValue(JsonUtils.toJsonBytes(deleteModule))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveMoveLog(@Validated NodeSortDTO request, String operator, String requestUrl, String requestMethod) {
        BaseModule moveNode = request.getNode();
        BaseModule previousNode = request.getPreviousNode();
        BaseModule nextNode = request.getNextNode();
        BaseModule parentModule = request.getParent();

        Project project = projectMapper.selectOneById(moveNode.getProjectId());
        String logContent;
        if (nextNode == null && previousNode == null) {
            logContent = moveNode.getName() + " " + Translator.get("file.log.move_to") + parentModule.getName();
        } else if (nextNode == null) {
            logContent = moveNode.getName() + " " + Translator.get("file.log.move_to") + parentModule.getName() + " " + previousNode.getName() + Translator.get("file.log.next");
        } else if (previousNode == null) {
            logContent = moveNode.getName() + " " + Translator.get("file.log.move_to") + parentModule.getName() + " " + nextNode.getName() + Translator.get("file.log.previous");
        } else {
            logContent = moveNode.getName() + " " + Translator.get("file.log.move_to") + parentModule.getName() + " " +
                    previousNode.getName() + Translator.get("file.log.next") + " " + nextNode.getName() + Translator.get("file.log.previous");
        }
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(moveNode.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(logModule)
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(moveNode.getId())
                .content(logContent)
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }
}
