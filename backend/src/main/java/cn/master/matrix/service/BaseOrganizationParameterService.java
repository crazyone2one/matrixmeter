package cn.master.matrix.service;

import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.OrganizationParameter;

/**
 * 组织参数 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-07-02T12:54:14.136756700
 */
public interface BaseOrganizationParameterService extends IService<OrganizationParameter> {

    String getOrgTemplateEnableKeyByScene(String scene);

    String getValue(String orgId, String key);
}
