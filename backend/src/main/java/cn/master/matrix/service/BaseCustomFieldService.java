package cn.master.matrix.service;

import cn.master.matrix.entity.CustomFieldOption;
import cn.master.matrix.payload.dto.CustomFieldDTO;
import cn.master.matrix.payload.dto.request.CustomFieldOptionRequest;
import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.CustomField;

import java.util.List;

/**
 * 自定义字段 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T11:44:33.956260700
 */
public interface BaseCustomFieldService extends IService<CustomField> {
    CustomField getWithCheck(String id);

    boolean isOrganizationTemplateEnable(String orgId, String scene);

    CustomField add(CustomField customField, List<CustomFieldOptionRequest> options);

    CustomField baseAdd(CustomField customField, List<CustomFieldOption> options);

    List<CustomFieldOption> parseCustomFieldOptionRequest2Option(List<CustomFieldOptionRequest> options);

    CustomField update(CustomField customField, List<CustomFieldOptionRequest> options);

    void delete(String id);

    CustomFieldDTO getCustomFieldDtoWithCheck(String id);

    String translateInternalField(String filedName);

    List<CustomFieldDTO> list(String orgId, String scene);

    List<CustomField> getByScopeIdAndScene(String scopeId, String scene);
}
