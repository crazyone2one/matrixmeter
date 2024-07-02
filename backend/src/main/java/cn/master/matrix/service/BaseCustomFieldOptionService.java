package cn.master.matrix.service;

import cn.master.matrix.payload.dto.request.CustomFieldOptionRequest;
import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.CustomFieldOption;

import java.util.List;

/**
 * 自定义字段选项 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T11:45:38.767556500
 */
public interface BaseCustomFieldOptionService extends IService<CustomFieldOption> {

    void addByFieldId(String fieldId, List<CustomFieldOption> options);

    void updateByFieldId(String fieldId, List<CustomFieldOptionRequest> options);

    List<CustomFieldOption> getByFieldId(String fieldId);

    void deleteByFieldId(String fieldId);

    void deleteByFieldIds(List<String> fieldIds);

    List<CustomFieldOption> getByFieldIds(List<String> fieldIds);
}
