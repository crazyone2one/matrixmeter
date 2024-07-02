package cn.master.matrix.handler.resolver;

import cn.master.matrix.payload.dto.CustomFieldDao;
import cn.master.matrix.util.DateUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
public class CustomFieldDateTimeResolver extends AbstractCustomFieldResolver {

    @Override
    public void validate(CustomFieldDao customField, Object value) {
        validateRequired(customField, value);
        try {
            if (value != null && StringUtils.isNotBlank(value.toString())) {
                DateUtils.getTime(value.toString());
            }
        } catch (Exception e) {
            throwValidateException(customField.getName());
        }
    }

}
