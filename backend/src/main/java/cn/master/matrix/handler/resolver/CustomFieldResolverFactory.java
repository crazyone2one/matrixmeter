package cn.master.matrix.handler.resolver;

import cn.master.matrix.constants.CustomFieldType;

import java.util.HashMap;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
public class CustomFieldResolverFactory {
    private static final HashMap<String, AbstractCustomFieldResolver> RESOLVER_MAP = new HashMap<>();

    static {
        RESOLVER_MAP.put(CustomFieldType.SELECT.name(), new CustomFieldSelectResolver());
        RESOLVER_MAP.put(CustomFieldType.RADIO.name(), new CustomFieldSelectResolver());

        RESOLVER_MAP.put(CustomFieldType.MULTIPLE_SELECT.name(), new CustomFieldMultipleSelectResolver());
        RESOLVER_MAP.put(CustomFieldType.CHECKBOX.name(), new CustomFieldMultipleSelectResolver());

        RESOLVER_MAP.put(CustomFieldType.INPUT.name(), new CustomFieldTextResolver());
        RESOLVER_MAP.put(CustomFieldType.TEXTAREA.name(), new CustomFieldTextResolver());

        RESOLVER_MAP.put(CustomFieldType.MULTIPLE_INPUT.name(), new CustomFieldMultipleTextResolver());

        RESOLVER_MAP.put(CustomFieldType.DATE.name(), new CustomFieldDateResolver());
        RESOLVER_MAP.put(CustomFieldType.DATETIME.name(), new CustomFieldDateTimeResolver());

        RESOLVER_MAP.put(CustomFieldType.MEMBER.name(), new CustomFieldMemberResolver());
        RESOLVER_MAP.put(CustomFieldType.MULTIPLE_MEMBER.name(), new CustomFieldMultipleMemberResolver());

        RESOLVER_MAP.put(CustomFieldType.INT.name(), new CustomFieldIntegerResolver());
        RESOLVER_MAP.put(CustomFieldType.FLOAT.name(), new CustomFieldFloatResolver());
    }

    public static AbstractCustomFieldResolver getResolver(String type) {
        return RESOLVER_MAP.get(type);
    }
}
