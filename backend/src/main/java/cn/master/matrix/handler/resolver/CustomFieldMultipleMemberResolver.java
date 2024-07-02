package cn.master.matrix.handler.resolver;

import cn.master.matrix.payload.dto.CustomFieldDao;
import cn.master.matrix.util.JsonUtils;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
public class CustomFieldMultipleMemberResolver extends CustomFieldMemberResolver {

    @Override
    public void validate(CustomFieldDao customField, Object value) {
        validateArrayRequired(customField, value);
        validateArray(customField.getName(), value);
    }

    @Override
    public String parse2String(Object value) {
        return JsonUtils.toJsonString(value);
    }

    @Override
    public Object parse2Value(String value) {
        return parse2Array(value);
    }
}
