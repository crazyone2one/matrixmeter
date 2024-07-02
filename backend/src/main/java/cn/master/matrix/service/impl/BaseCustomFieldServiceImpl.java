package cn.master.matrix.service.impl;

import cn.master.matrix.constants.CustomFieldType;
import cn.master.matrix.constants.TemplateRequiredCustomField;
import cn.master.matrix.constants.TemplateScene;
import cn.master.matrix.entity.CustomField;
import cn.master.matrix.entity.CustomFieldOption;
import cn.master.matrix.entity.TemplateCustomField;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.mapper.CustomFieldMapper;
import cn.master.matrix.mapper.TemplateCustomFieldMapper;
import cn.master.matrix.payload.dto.CustomFieldDTO;
import cn.master.matrix.payload.dto.request.CustomFieldOptionRequest;
import cn.master.matrix.service.BaseCustomFieldOptionService;
import cn.master.matrix.service.BaseCustomFieldService;
import cn.master.matrix.service.BaseOrganizationParameterService;
import cn.master.matrix.util.ServiceUtils;
import cn.master.matrix.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static cn.master.matrix.entity.table.CustomFieldTableDef.CUSTOM_FIELD;
import static cn.master.matrix.entity.table.TemplateCustomFieldTableDef.TEMPLATE_CUSTOM_FIELD;
import static cn.master.matrix.exception.CommonResultCode.CUSTOM_FIELD_EXIST;
import static cn.master.matrix.exception.CommonResultCode.TEMPLATE_SCENE_ILLEGAL;

/**
 * 自定义字段 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T11:44:33.956260700
 */
@Service
@RequiredArgsConstructor
public class BaseCustomFieldServiceImpl extends ServiceImpl<CustomFieldMapper, CustomField> implements BaseCustomFieldService {
    private final BaseOrganizationParameterService baseOrganizationParameterService;
    final BaseCustomFieldOptionService baseCustomFieldOptionService;
    private final TemplateCustomFieldMapper templateCustomFieldMapper;
    private static final String CREATE_USER = "CREATE_USER";

    @Override
    public CustomField getWithCheck(String id) {
        checkResourceExist(id);
        return mapper.selectOneById(id);
    }

    @Override
    public boolean isOrganizationTemplateEnable(String orgId, String scene) {
        String key = baseOrganizationParameterService.getOrgTemplateEnableKeyByScene(scene);
        String value = baseOrganizationParameterService.getValue(orgId, key);
        // 没有配置默认为 true
        return !StringUtils.equals(BooleanUtils.toStringTrueFalse(false), value);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomField add(CustomField customField, List<CustomFieldOptionRequest> options) {
        customField.setInternal(false);
        List<CustomFieldOption> customFieldOptions = parseCustomFieldOptionRequest2Option(options);
        return this.baseAdd(customField, customFieldOptions);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomField baseAdd(CustomField customField, List<CustomFieldOption> options) {
        checkAddExist(customField);
        customField.setEnableOptionKey(BooleanUtils.isTrue(customField.getEnableOptionKey()));
        mapper.insert(customField);
        baseCustomFieldOptionService.addByFieldId(customField.getId(), options);
        return customField;
    }

    private void checkAddExist(CustomField customField) {
        val exists = queryChain().where(CUSTOM_FIELD.SCOPE_ID.eq(customField.getScopeId())
                .and(CUSTOM_FIELD.SCENE.eq(customField.getScene()))
                .and(CUSTOM_FIELD.NAME.eq(customField.getName()))).exists();
        if (exists) {
            throw new CustomException(CUSTOM_FIELD_EXIST);
        }
    }

    @Override
    public List<CustomFieldOption> parseCustomFieldOptionRequest2Option(List<CustomFieldOptionRequest> options) {
        return options == null ? null : options.stream().map(item -> {
            CustomFieldOption customFieldOption = new CustomFieldOption();
            BeanUtils.copyProperties(item, customFieldOption);
            return customFieldOption;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomField update(CustomField customField, List<CustomFieldOptionRequest> options) {
        checkUpdateExist(customField);
        checkResourceExist(customField.getId());
        mapper.update(customField);
        if (options != null) {
            baseCustomFieldOptionService.updateByFieldId(customField.getId(), options);
        }
        return customField;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        LogicDeleteManager.execWithoutLogicDelete(() -> {
            mapper.deleteById(id);
        });
        baseCustomFieldOptionService.deleteByFieldId(id);
        deleteTemplateCustomField(id);
    }

    @Override
    public CustomFieldDTO getCustomFieldDtoWithCheck(String id) {
        checkResourceExist(id);
        val customField = mapper.selectOneById(id);
        CustomFieldDTO customFieldDTO = new CustomFieldDTO();
        BeanUtils.copyProperties(customField, customFieldDTO);
        customFieldDTO.setOptions(baseCustomFieldOptionService.getByFieldId(customFieldDTO.getId()));
        if (customField.getInternal()) {
            customFieldDTO.setInternalFieldKey(customField.getName());
            customField.setName(translateInternalField(customField.getName()));
        }
        return customFieldDTO;
    }

    @Override
    public String translateInternalField(String filedName) {
        return Translator.get("custom_field." + filedName);
    }

    @Override
    public List<CustomFieldDTO> list(String scopeId, String scene) {
        checkScene(scene);
        List<CustomField> customFields = getByScopeIdAndScene(scopeId, scene);
        List<CustomFieldOption> customFieldOptions = baseCustomFieldOptionService.getByFieldIds(customFields.stream().map(CustomField::getId).toList());
        Map<String, List<CustomFieldOption>> optionMap = customFieldOptions.stream().collect(Collectors.groupingBy(CustomFieldOption::getFieldId));
        List<String> usedFieldIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(customFields)) {
            val strings = QueryChain.of(templateCustomFieldMapper).select(QueryMethods.distinct(TEMPLATE_CUSTOM_FIELD.FIELD_ID))
                    .from(TEMPLATE_CUSTOM_FIELD)
                    .where(TEMPLATE_CUSTOM_FIELD.FIELD_ID.in(customFields.stream().map(CustomField::getId).toList()))
                    .listAs(String.class);
            usedFieldIds.addAll(strings);
        }
        return customFields.stream().map(item -> {
            CustomFieldDTO customFieldDTO = new CustomFieldDTO();
            BeanUtils.copyProperties(item, customFieldDTO);
            if (usedFieldIds.contains(item.getId())) {
                customFieldDTO.setUsed(true);
            }
            customFieldDTO.setOptions(optionMap.get(item.getId()));
            if (CustomFieldType.getHasOptionValueSet().contains(customFieldDTO.getType()) && customFieldDTO.getOptions() == null) {
                customFieldDTO.setOptions(List.of());
            }
            if (StringUtils.equalsAny(item.getType(), CustomFieldType.MEMBER.name(), CustomFieldType.MULTIPLE_MEMBER.name())) {
                // 成员选项添加默认的选项
                CustomFieldOption createUserOption = new CustomFieldOption();
                createUserOption.setFieldId(item.getId());
                createUserOption.setText(Translator.get("message.domain.createUser"));
                createUserOption.setValue(CREATE_USER);
                createUserOption.setInternal(false);
                customFieldDTO.setOptions(List.of(createUserOption));
            }
            if (BooleanUtils.isTrue(item.getInternal())) {
                // 设置哪些内置字段是模板里必选的
                Set<String> templateRequiredCustomFieldSet = Arrays.stream(TemplateRequiredCustomField.values())
                        .map(TemplateRequiredCustomField::getName)
                        .collect(Collectors.toSet());
                customFieldDTO.setTemplateRequired(templateRequiredCustomFieldSet.contains(item.getName()));
                customFieldDTO.setInternalFieldKey(item.getName());
                // 翻译内置字段名称
                customFieldDTO.setName(translateInternalField(item.getName()));
            }
            return customFieldDTO;
        }).toList();
    }

    @Override
    public List<CustomField> getByScopeIdAndScene(String scopeId, String scene) {
        return queryChain().where(CUSTOM_FIELD.SCOPE_ID.eq(scopeId).and(CUSTOM_FIELD.SCENE.eq(scene))).list();
    }

    private void checkScene(String scene) {
        Arrays.stream(TemplateScene.values()).map(TemplateScene::name)
                .filter(item -> item.equals(scene))
                .findFirst()
                .orElseThrow(() -> new CustomException(TEMPLATE_SCENE_ILLEGAL));
    }

    private void deleteTemplateCustomField(String id) {
        val queryChain = QueryChain.of(templateCustomFieldMapper).where(TemplateCustomField::getFieldId).eq(id);
        LogicDeleteManager.execWithoutLogicDelete(() -> templateCustomFieldMapper.deleteByQuery(queryChain));
    }

    private void checkUpdateExist(CustomField customField) {
        if (StringUtils.isBlank(customField.getName())) {
            return;
        }
        val exists = queryChain().where(CUSTOM_FIELD.SCOPE_ID.eq(customField.getScopeId())
                .and(CUSTOM_FIELD.SCENE.eq(customField.getScene()))
                .and(CUSTOM_FIELD.NAME.eq(customField.getName()))
                .and(CUSTOM_FIELD.ID.ne(customField.getId()))).exists();
        if (exists) {
            throw new CustomException(CUSTOM_FIELD_EXIST);
        }
    }

    private CustomField checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(mapper.selectOneById(id), "permission.organization_custom_field.name");
    }
}
