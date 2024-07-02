package cn.master.matrix.constants;

import cn.master.matrix.entity.CustomFieldOption;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Getter
public enum DefaultBugCustomField {

    /**
     * 严重程度
     */
    DEGREE("bug_degree", CustomFieldType.SELECT,
            Arrays.asList(
                    getNewOption("suggestive", "提示", 1),
                    getNewOption("general", "一般", 2),
                    getNewOption("severity", "严重", 3),
                    getNewOption("deadly", "致命", 4)
            )
    );

    private final String name;
    private final CustomFieldType type;
    private final List<CustomFieldOption> options;

    DefaultBugCustomField(String name, CustomFieldType type, List<CustomFieldOption> options) {
        this.name = name;
        this.type = type;
        this.options = options;
    }

    private static CustomFieldOption getNewOption(String value, String text, Integer pos) {
        CustomFieldOption customFieldOption = new CustomFieldOption();
        customFieldOption.setValue(value);
        customFieldOption.setText(text);
        customFieldOption.setPos(pos);
        customFieldOption.setInternal(true);
        return customFieldOption;
    }
}
