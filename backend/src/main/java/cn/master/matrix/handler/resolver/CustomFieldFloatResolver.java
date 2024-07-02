package cn.master.matrix.handler.resolver;

import cn.master.matrix.payload.dto.CustomFieldDao;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
public class CustomFieldFloatResolver extends AbstractCustomFieldResolver {

    @Override
    public void validate(CustomFieldDao customField, Object value) {
        validateRequired(customField, value);
        if (value != null && !(value instanceof Number)) {
            throwValidateException(customField.getName());
        }
    }

    @Override
    public Object parse2Value(String value) {
        return value == null ? null : Float.parseFloat(value);
    }
}

