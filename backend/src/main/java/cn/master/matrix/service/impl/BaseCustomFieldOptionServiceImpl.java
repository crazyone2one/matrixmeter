package cn.master.matrix.service.impl;

import cn.master.matrix.entity.CustomFieldOption;
import cn.master.matrix.mapper.CustomFieldOptionMapper;
import cn.master.matrix.payload.dto.request.CustomFieldOptionRequest;
import cn.master.matrix.service.BaseCustomFieldOptionService;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自定义字段选项 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T11:45:38.767556500
 */
@Service
public class BaseCustomFieldOptionServiceImpl extends ServiceImpl<CustomFieldOptionMapper, CustomFieldOption> implements BaseCustomFieldOptionService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addByFieldId(String fieldId, List<CustomFieldOption> options) {
        if (CollectionUtils.isEmpty(options)) {
            return;
        }
        options.forEach(item -> {
            item.setFieldId(fieldId);
            item.setInternal(BooleanUtils.isTrue(item.getInternal()));
        });
        mapper.insertBatch(options);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByFieldId(String fieldId, List<CustomFieldOptionRequest> options) {
        List<CustomFieldOption> originOptions = getByFieldId(fieldId);
        // 查询原有选项
        Map<String, CustomFieldOption> optionMap =
                originOptions.stream().collect(Collectors.toMap(CustomFieldOption::getValue, i -> i));
        // 先删除选项，再添加
        deleteByFieldId(fieldId);
        List<CustomFieldOption> customFieldOptions = options.stream().map(item -> {
            CustomFieldOption customFieldOption = new CustomFieldOption();
            BeanUtils.copyProperties(item, customFieldOption);
            if (optionMap.get(item.getValue()) != null) {
                // 保留选项是否是内置的选项
                customFieldOption.setInternal(optionMap.get(item.getValue()).getInternal());
            } else {
                customFieldOption.setInternal(false);
            }
            customFieldOption.setFieldId(fieldId);
            return customFieldOption;
        }).toList();
        if (CollectionUtils.isNotEmpty(customFieldOptions)) {
            mapper.insertBatch(customFieldOptions);
        }
    }

    @Override
    public List<CustomFieldOption> getByFieldId(String fieldId) {
        val options = queryChain().where(CustomFieldOption::getFieldId).eq(fieldId).list();
        if (CollectionUtils.isNotEmpty(options)) {
            options.sort(Comparator.comparing(CustomFieldOption::getPos));
        }
        return options;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByFieldId(String fieldId) {
        val queryChain = queryChain().where(CustomFieldOption::getFieldId).eq(fieldId);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByFieldIds(List<String> fieldIds) {
        if (CollectionUtils.isEmpty(fieldIds)) {
            return;
        }
        val queryChain = queryChain().where(CustomFieldOption::getFieldId).in(fieldIds);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain));
    }

    @Override
    public List<CustomFieldOption> getByFieldIds(List<String> fieldIds) {
        if (CollectionUtils.isEmpty(fieldIds)) {
            return List.of();
        }
        val options = queryChain().where(CustomFieldOption::getFieldId).in(fieldIds).list();
        if (CollectionUtils.isNotEmpty(options)) {
            options.sort(Comparator.comparing(CustomFieldOption::getPos));
        }
        return options;
    }
}
