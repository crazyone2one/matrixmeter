package cn.master.matrix.constants;

import cn.master.matrix.entity.CustomFieldOption;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Getter
public enum DefaultFunctionalCustomField {

    PRIORITY("functional_priority", CustomFieldType.SELECT,
            Arrays.asList(
                    getNewOption("P0", "P0", 1),
                    getNewOption("P1", "P1", 2),
                    getNewOption("P2", "P2", 3),
                    getNewOption("P3", "P3", 4)
            )
    );

    private final String name;
    private final CustomFieldType type;
    private final List<CustomFieldOption> options;

    DefaultFunctionalCustomField(String name, CustomFieldType type, List<CustomFieldOption> options) {
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

