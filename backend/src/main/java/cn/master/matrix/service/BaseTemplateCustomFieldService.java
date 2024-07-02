package cn.master.matrix.service;

import cn.master.matrix.entity.TemplateCustomField;
import cn.master.matrix.payload.dto.request.TemplateCustomFieldRequest;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 模板和字段的关联关系 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T14:36:15.797305200
 */
public interface BaseTemplateCustomFieldService extends IService<TemplateCustomField> {
    List<TemplateCustomField> getByTemplateIds(List<String> projectTemplateIds);

    void setValidateDefaultValue(boolean validateDefaultValue);
    void removeValidateDefaultValue();

    void addCustomFieldByTemplateId(String id, List<TemplateCustomFieldRequest> customFields);

    void addSystemFieldByTemplateId(String id, List<TemplateCustomFieldRequest> customFieldRequests);
}
