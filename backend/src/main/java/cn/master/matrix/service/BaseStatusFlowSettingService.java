package cn.master.matrix.service;

import cn.master.matrix.constants.BugStatusDefinitionType;
import cn.master.matrix.constants.DefaultBugStatusItem;
import cn.master.matrix.constants.TemplateScene;
import cn.master.matrix.constants.TemplateScopeType;
import cn.master.matrix.entity.StatusDefinition;
import cn.master.matrix.entity.StatusFlow;
import cn.master.matrix.entity.StatusItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Service
@RequiredArgsConstructor
public class BaseStatusFlowSettingService {
    private final BaseStatusItemService baseStatusItemService;
    private final BaseStatusFlowService baseStatusFlowService;
    private final BaseStatusDefinitionService baseStatusDefinitionService;

    public void initBugDefaultStatusFlowSetting(String projectId, TemplateScopeType scopeType) {
        List<StatusItem> statusItems = new ArrayList<>();
        List<StatusDefinition> statusDefinitions = new ArrayList<>();
        List<StatusFlow> statusFlows = new ArrayList<>();
        for (DefaultBugStatusItem defaultBugStatusItem : DefaultBugStatusItem.values()) {
            // 创建默认的状态项
            StatusItem statusItem = new StatusItem();
            statusItem.setName(defaultBugStatusItem.getName());
            statusItem.setScene(TemplateScene.BUG.name());
            statusItem.setInternal(true);
            statusItem.setScopeType(scopeType.name());
            statusItem.setRemark(defaultBugStatusItem.getRemark());
            statusItem.setScopeId(projectId);
            statusItems.add(statusItem);
        }
        statusItems = baseStatusItemService.batchAdd(statusItems);

        Map<String, String> statusNameMap = statusItems.stream().collect(Collectors.toMap(StatusItem::getName, StatusItem::getId));
        for (DefaultBugStatusItem defaultBugStatusItem : DefaultBugStatusItem.values()) {

            // 创建默认的状态定义
            List<BugStatusDefinitionType> definitionTypes = defaultBugStatusItem.getDefinitionTypes();
            for (BugStatusDefinitionType definitionType : definitionTypes) {
                StatusDefinition statusDefinition = new StatusDefinition();
                statusDefinition.setStatusId(statusNameMap.get(defaultBugStatusItem.getName()));
                statusDefinition.setDefinitionId(definitionType.name());
                statusDefinitions.add(statusDefinition);
            }

            // 创建默认的状态流
            String fromStatusId = statusNameMap.get(defaultBugStatusItem.getName());
            List<String> statusFlowTargets = defaultBugStatusItem.getStatusFlowTargets();
            for (String statusFlowTarget : statusFlowTargets) {
                StatusFlow statusFlow = new StatusFlow();
                statusFlow.setFromId(fromStatusId);
                statusFlow.setToId(statusNameMap.get(statusFlowTarget));
                statusFlows.add(statusFlow);
            }
        }
        baseStatusDefinitionService.batchAdd(statusDefinitions);
        baseStatusFlowService.batchAdd(statusFlows);
    }
}
