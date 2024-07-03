package cn.master.matrix.service.impl;

import cn.master.matrix.entity.CustomField;
import cn.master.matrix.entity.TemplateCustomField;
import cn.master.matrix.handler.resolver.AbstractCustomFieldResolver;
import cn.master.matrix.handler.resolver.CustomFieldResolverFactory;
import cn.master.matrix.mapper.TemplateCustomFieldMapper;
import cn.master.matrix.payload.dto.CustomFieldDao;
import cn.master.matrix.payload.dto.request.TemplateCustomFieldRequest;
import cn.master.matrix.service.BaseCustomFieldService;
import cn.master.matrix.service.BaseTemplateCustomFieldService;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 模板和字段的关联关系 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T14:36:15.797305200
 */
@Service
@RequiredArgsConstructor
public class BaseTemplateCustomFieldServiceImpl extends ServiceImpl<TemplateCustomFieldMapper, TemplateCustomField> implements BaseTemplateCustomFieldService {
    private final BaseCustomFieldService baseCustomFieldService;
    public static final ThreadLocal<Boolean> VALIDATE_DEFAULT_VALUE = new ThreadLocal<>();

    @Override
    public List<TemplateCustomField> getByTemplateIds(List<String> projectTemplateIds) {
        if (CollectionUtils.isEmpty(projectTemplateIds)) {
            return List.of();
        }
        return queryChain().where(TemplateCustomField::getTemplateId).in(projectTemplateIds).list();
    }

    @Override
    public void setValidateDefaultValue(boolean validateDefaultValue) {
        VALIDATE_DEFAULT_VALUE.set(validateDefaultValue);
    }

    @Override
    public void removeValidateDefaultValue() {
        VALIDATE_DEFAULT_VALUE.remove();
    }

    @Override
    public void addCustomFieldByTemplateId(String id, List<TemplateCustomFieldRequest> customFields) {
        if (CollectionUtils.isEmpty(customFields)) {
            return;
        }
        // 过滤下不存在的字段
        List<String> ids = customFields.stream().map(TemplateCustomFieldRequest::getFieldId).toList();
        Set<String> fieldIdSet = baseCustomFieldService.getByIds(ids)
                .stream()
                .map(CustomField::getId)
                .collect(Collectors.toSet());
        customFields = customFields.stream()
                .filter(item -> fieldIdSet.contains(item.getFieldId()))
                .toList();
        addByTemplateId(id, customFields, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSystemFieldByTemplateId(String id, List<TemplateCustomFieldRequest> customFieldRequests) {
        if (CollectionUtils.isEmpty(customFieldRequests)) {
            return;
        }
        this.addByTemplateId(id, customFieldRequests, true);
    }

    @Override
    public List<TemplateCustomField> getByTemplateId(String templateId) {
        return queryChain().where(TemplateCustomField::getTemplateId).eq(templateId).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTemplateIdAndSystem(String templateId, boolean isSystem) {
        val chain = queryChain().where(TemplateCustomField::getTemplateId).eq(templateId).and(TemplateCustomField::getSystemField).eq(isSystem);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(chain));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTemplateIds(List<String> projectTemplateIds) {
        if (CollectionUtils.isEmpty(projectTemplateIds)) {
            return;
        }
        val chain = query().where(TemplateCustomField::getTemplateId).in(projectTemplateIds);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(chain));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTemplateId(String templateId) {
        val chain = query().where(TemplateCustomField::getTemplateId).eq(templateId);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(chain));
    }

    private void addByTemplateId(String templateId, List<TemplateCustomFieldRequest> customFields, boolean isSystem) {
        AtomicReference<Integer> pos = new AtomicReference<>(0);
        List<TemplateCustomField> templateCustomFields = customFields.stream().map(field -> {
            TemplateCustomField templateCustomField = new TemplateCustomField();
            BeanUtils.copyProperties(field, templateCustomField);
            templateCustomField.setTemplateId(templateId);
            templateCustomField.setPos(pos.getAndSet(pos.get() + 1));
            templateCustomField.setDefaultValue(isSystem ? field.getDefaultValue().toString() : parseDefaultValue(field));
            templateCustomField.setSystemField(isSystem);
            return templateCustomField;
        }).toList();
        if (!templateCustomFields.isEmpty()) {
            mapper.insertBatch(templateCustomFields);
        }
    }

    private String parseDefaultValue(TemplateCustomFieldRequest field) {
        CustomField customField = baseCustomFieldService.getWithCheck(field.getFieldId());
        AbstractCustomFieldResolver customFieldResolver = CustomFieldResolverFactory.getResolver(customField.getType());
        CustomFieldDao customFieldDao = new CustomFieldDao();
        BeanUtils.copyProperties(customField, customFieldDao);
        customFieldDao.setRequired(false);
        if (BooleanUtils.isNotFalse(VALIDATE_DEFAULT_VALUE.get())) {
            // 创建项目时不校验默认值
            customFieldResolver.validate(customFieldDao, field.getDefaultValue());
        }
        return customFieldResolver.parse2String(field.getDefaultValue());
    }
}
