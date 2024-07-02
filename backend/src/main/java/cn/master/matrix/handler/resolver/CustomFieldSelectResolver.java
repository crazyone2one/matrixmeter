package cn.master.matrix.handler.resolver;

import cn.master.matrix.entity.CustomFieldOption;
import cn.master.matrix.payload.dto.CustomFieldDao;
import cn.master.matrix.service.BaseCustomFieldOptionService;
import cn.master.matrix.util.CommonBeanFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
public class CustomFieldSelectResolver extends AbstractCustomFieldResolver{
    @Override
    public void validate(CustomFieldDao customField, Object value) {
        validateRequired(customField, value);
        if (value == null) {
            return;
        }
        validateString(customField.getName(), value);
        if (StringUtils.isBlank((String) value)) {
            return;
        }
        List<CustomFieldOption> options = getOptions(customField.getId());
        Set<String> values = options.stream().map(CustomFieldOption::getValue).collect(Collectors.toSet());
        if (!values.contains(value)) {
            throwValidateException(customField.getName());
        }
    }
    protected List<CustomFieldOption> getOptions(String id) {
        BaseCustomFieldOptionService customFieldOptionService = CommonBeanFactory.getBean(BaseCustomFieldOptionService.class);
        assert customFieldOptionService != null;
        return customFieldOptionService.getByFieldId(id);
    }
}
