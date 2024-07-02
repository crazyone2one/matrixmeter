package cn.master.matrix.service.impl;

import cn.master.matrix.constants.TemplateScopeType;
import cn.master.matrix.entity.CustomField;
import cn.master.matrix.entity.CustomFieldOption;
import cn.master.matrix.entity.Project;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.mapper.TemplateCustomFieldMapper;
import cn.master.matrix.payload.dto.CustomFieldDTO;
import cn.master.matrix.payload.dto.request.CustomFieldOptionRequest;
import cn.master.matrix.service.BaseCustomFieldOptionService;
import cn.master.matrix.service.BaseOrganizationParameterService;
import cn.master.matrix.service.OrganizationCustomFieldService;
import cn.master.matrix.service.OrganizationService;
import cn.master.matrix.util.SubListUtils;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.master.matrix.entity.table.CustomFieldTableDef.CUSTOM_FIELD;
import static cn.master.matrix.exception.CommonResultCode.INTERNAL_CUSTOM_FIELD_PERMISSION;
import static cn.master.matrix.exception.SystemResultCode.ORGANIZATION_TEMPLATE_PERMISSION;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Service
public class OrganizationCustomFieldServiceImpl extends BaseCustomFieldServiceImpl implements OrganizationCustomFieldService {
    private final OrganizationService organizationService;

    public OrganizationCustomFieldServiceImpl(OrganizationService organizationService,
                                              BaseOrganizationParameterService baseOrganizationParameterService,
                                              BaseCustomFieldOptionService baseCustomFieldOptionService,
                                              TemplateCustomFieldMapper templateCustomFieldMapper) {
        super(baseOrganizationParameterService, baseCustomFieldOptionService, templateCustomFieldMapper);
        this.organizationService = organizationService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomField add(CustomField customField, List<CustomFieldOptionRequest> options) {
        checkOrganizationTemplateEnable(customField.getScopeId(), customField.getScene());
        organizationService.checkResourceExist(customField.getScopeId());
        customField.setScopeType(TemplateScopeType.ORGANIZATION.name());
        customField = super.add(customField, options);
        // 同步创建项目级别字段
        addRefProjectCustomField(customField, options);
        return customField;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomField update(CustomField customField, List<CustomFieldOptionRequest> options) {
        CustomField originCustomField = getWithCheck(customField.getId());
        if (originCustomField.getInternal()) {
            // 内置字段不能修改名字
            customField.setName(null);
        }
        checkOrganizationTemplateEnable(customField.getScopeId(), originCustomField.getScene());
        customField.setScopeId(originCustomField.getScopeId());
        customField.setScene(originCustomField.getScene());
        organizationService.checkResourceExist(originCustomField.getScopeId());
        // 同步创建项目级别字段
        updateRefProjectCustomField(customField, options);
        return super.update(customField, options);
    }

    @Override
    public void delete(String id) {
        CustomField customField = getWithCheck(id);
        checkOrganizationTemplateEnable(customField.getScopeId(), customField.getScene());
        checkInternal(customField);
        organizationService.checkResourceExist(customField.getScopeId());
        // 同步删除项目级别字段
        deleteRefProjectTemplate(id);
        super.delete(id);
    }

    @Override
    public CustomFieldDTO getCustomFieldWithCheck(String id) {
        CustomFieldDTO customField = super.getCustomFieldDtoWithCheck(id);
        organizationService.checkResourceExist(customField.getScopeId());
        return customField;
    }

    @Override
    public List<CustomFieldDTO> list(String orgId, String scene) {
        organizationService.checkResourceExist(orgId);
        return super.list(orgId, scene);
    }

    private void deleteRefProjectTemplate(String id) {
        val queryChain = queryChain().where(CustomField::getRefId).eq(id);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain));
        // 删除字段选项
        List<String> projectCustomFieldIds = queryChain.select("id").from(CUSTOM_FIELD)
                .where(CUSTOM_FIELD.REF_ID.eq(id)).listAs(String.class);
        // 分批删除
        SubListUtils.dealForSubList(projectCustomFieldIds, 100, super.baseCustomFieldOptionService::deleteByFieldIds);
    }

    protected void checkInternal(CustomField customField) {
        if (customField.getInternal()) {
            throw new CustomException(INTERNAL_CUSTOM_FIELD_PERMISSION);
        }
    }

    private void updateRefProjectCustomField(CustomField orgCustomField, List<CustomFieldOptionRequest> options) {
        List<CustomField> projectFields = getByRefId(orgCustomField.getId());
        CustomField customField = new CustomField();
        BeanUtils.copyProperties(orgCustomField, customField);
        projectFields.forEach(projectField -> {
            customField.setId(projectField.getId());
            customField.setScopeId(projectField.getScopeId());
            customField.setRefId(orgCustomField.getId());
            customField.setScene(orgCustomField.getScene());
            super.update(customField, options);
        });
    }

    private List<CustomField> getByRefId(String refId) {
        return queryChain().where(CustomField::getRefId).eq(refId).list();
    }

    private void addRefProjectCustomField(CustomField orgCustomField, List<CustomFieldOptionRequest> options) {
        String orgId = orgCustomField.getScopeId();
        List<String> projectIds = QueryChain.of(Project.class).where(Project::getOrganizationId).eq(orgId)
                .list().stream().map(Project::getId).toList();
        CustomField customField = new CustomField();
        BeanUtils.copyProperties(orgCustomField, customField);
        List<CustomFieldOption> customFieldOptions = parseCustomFieldOptionRequest2Option(options);
        projectIds.forEach(projectId -> {
            customField.setScopeType(TemplateScopeType.PROJECT.name());
            customField.setScopeId(projectId);
            customField.setRefId(orgCustomField.getId());
            super.baseAdd(customField, customFieldOptions);
        });
    }

    private void checkOrganizationTemplateEnable(String orgId, String scene) {
        if (!isOrganizationTemplateEnable(orgId, scene)) {
            throw new CustomException(ORGANIZATION_TEMPLATE_PERMISSION);
        }
    }
}
