package cn.master.matrix.constants;

import lombok.Getter;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Getter
public enum TemplateRequiredCustomField {
    BUG_DEGREE("functional_priority");

    private final String name;

    TemplateRequiredCustomField(String name) {
        this.name = name;
    }

}
