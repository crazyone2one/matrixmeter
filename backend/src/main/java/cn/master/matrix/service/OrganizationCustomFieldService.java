package cn.master.matrix.service;

import cn.master.matrix.entity.CustomField;
import cn.master.matrix.payload.dto.CustomFieldDTO;
import cn.master.matrix.payload.dto.request.CustomFieldOptionRequest;

import java.util.List;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
public interface OrganizationCustomFieldService extends BaseCustomFieldService {
    @Override
    CustomField add(CustomField customField, List<CustomFieldOptionRequest> options);

    @Override
    CustomField update(CustomField customField, List<CustomFieldOptionRequest> options);

    @Override
    void delete(String id);

    CustomFieldDTO getCustomFieldWithCheck(String id);

    List<CustomFieldDTO> list(String organizationId, String scene);
}
