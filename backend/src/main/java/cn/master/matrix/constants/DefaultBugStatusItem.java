package cn.master.matrix.constants;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Getter
public enum DefaultBugStatusItem {

    /**
     * 新建
     */
    NEW(DefaultBugStatusItemName.NEW, null,
            Arrays.asList(DefaultBugStatusItemName.IN_PROCESS, DefaultBugStatusItemName.REJECTED),
            List.of(BugStatusDefinitionType.START)),
    /**
     * 处理中
     */
    IN_PROCESS(DefaultBugStatusItemName.IN_PROCESS, null,
            Arrays.asList(DefaultBugStatusItemName.REJECTED, DefaultBugStatusItemName.RESOLVED, DefaultBugStatusItemName.CLOSED),
            List.of()),
    /**
     * 已关闭
     */
    CLOSED(DefaultBugStatusItemName.CLOSED, null,
            List.of(DefaultBugStatusItemName.IN_PROCESS),
            List.of(BugStatusDefinitionType.END)),
    /**
     * 已解决
     */
    RESOLVED(DefaultBugStatusItemName.RESOLVED, null,
            Arrays.asList(DefaultBugStatusItemName.IN_PROCESS, DefaultBugStatusItemName.CLOSED),
            List.of()),
    /**
     * 已拒绝
     */
    REJECTED(DefaultBugStatusItemName.REJECTED, null,
            List.of(DefaultBugStatusItemName.IN_PROCESS),
            List.of(BugStatusDefinitionType.END));

    private final String name;
    private final String remark;
    /**
     * 状态流流转状态
     */
    private final List<String> statusFlowTargets;
    /**
     * 状态定义
     */
    private final List<BugStatusDefinitionType> definitionTypes;

    DefaultBugStatusItem(String name, String remark, List<String> statusFlowTargets, List<BugStatusDefinitionType> definitionTypes) {
        this.name = name;
        this.remark = remark;
        this.statusFlowTargets = statusFlowTargets;
        this.definitionTypes = definitionTypes;
    }
}