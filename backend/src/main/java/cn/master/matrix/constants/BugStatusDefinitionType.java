package cn.master.matrix.constants;

import lombok.Getter;

import java.util.Arrays;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Getter
public enum BugStatusDefinitionType {
    /**
     * 起始状态
     */
    START("status_definition.type.start", true),
    /**
     * 结束状态
     */
    END("status_definition.type.end", false);

    BugStatusDefinitionType(String name, Boolean isSingleChoice) {
        this.name = name;
        this.isSingleChoice = isSingleChoice;
    }

    /**
     * 状态名
     */
    private final String name;
    /**
     * 是否是单选
     */
    private final Boolean isSingleChoice;

    public static BugStatusDefinitionType getStatusDefinitionType(String type) {
        return Arrays.stream(BugStatusDefinitionType.values()).filter(item -> item.name().equals(type))
                .findFirst().orElse(null);
    }
}
