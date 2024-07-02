package cn.master.matrix.handler.resolver;

import cn.master.matrix.payload.dto.CustomFieldDao;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
public class CustomFieldTextResolver extends AbstractCustomFieldResolver {

    @Override
    public void validate(CustomFieldDao customField, Object value) {
        validateRequired(customField, value);
        validateString(customField.getName(), value);
    }

}
