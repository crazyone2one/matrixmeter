package cn.master.matrix.service.impl;

import cn.master.matrix.constants.TemplateScene;
import cn.master.matrix.entity.OrganizationParameter;
import cn.master.matrix.mapper.OrganizationParameterMapper;
import cn.master.matrix.service.BaseOrganizationParameterService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static cn.master.matrix.constants.OrganizationParameterConstants.*;

/**
 * 组织参数 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T12:54:14.136756700
 */
@Service
public class BaseOrganizationParameterServiceImpl extends ServiceImpl<OrganizationParameterMapper, OrganizationParameter> implements BaseOrganizationParameterService {

    @Override
    public String getOrgTemplateEnableKeyByScene(String scene) {
        Map<String, String> sceneMap = new HashMap<>();
        sceneMap.put(TemplateScene.FUNCTIONAL.name(), ORGANIZATION_FUNCTIONAL_TEMPLATE_ENABLE_KEY);
        sceneMap.put(TemplateScene.BUG.name(), ORGANIZATION_BUG_TEMPLATE_ENABLE_KEY);
        sceneMap.put(TemplateScene.API.name(), ORGANIZATION_API_TEMPLATE_ENABLE_KEY);
        sceneMap.put(TemplateScene.UI.name(), ORGANIZATION_UI_TEMPLATE_ENABLE_KEY);
        sceneMap.put(TemplateScene.TEST_PLAN.name(), ORGANIZATION_TEST_PLAN_TEMPLATE_ENABLE_KEY);
        return sceneMap.get(scene);
    }

    @Override
    public String getValue(String orgId, String key) {
        val parameter = queryChain().where(OrganizationParameter::getOrganizationId).eq(orgId)
                .and(OrganizationParameter::getParamKey).eq(key).one();
        return parameter == null ? null : parameter.getParamValue();
    }
}
