package cn.master.matrix.service.log;

import cn.master.matrix.constants.HttpMethodConstants;
import cn.master.matrix.constants.OperationLogConstants;
import cn.master.matrix.constants.OperationLogModule;
import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.entity.Organization;
import cn.master.matrix.entity.User;
import cn.master.matrix.mapper.OrganizationMapper;
import cn.master.matrix.mapper.UserMapper;
import cn.master.matrix.payload.dto.LogDTO;
import cn.master.matrix.payload.dto.request.OrganizationEditRequest;
import cn.master.matrix.payload.dto.request.OrganizationNameEditRequest;
import cn.master.matrix.service.OrganizationService;
import cn.master.matrix.util.JsonUtils;
import com.mybatisflex.core.query.QueryChain;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Created by 11's papa on 07/03/2024
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class SystemOrganizationLogService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private OrganizationService organizationService;

    /**
     * 更新组织
     *
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO updateNameLog(OrganizationNameEditRequest request) {
        Organization organization = organizationMapper.selectOneById(request.getId());
        if (organization != null) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    request.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_SYSTEM_ORGANIZATION,
                    organization.getName());
            dto.setPath("/system/organization/update");
            dto.setMethod(HttpMethodConstants.POST.name());
            return dto;
        }
        return null;
    }

    /**
     * 更新组织
     *
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO updateLog(OrganizationEditRequest request) {
        Organization organization = organizationMapper.selectOneById(request.getId());
        if (organization != null) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    request.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_SYSTEM_ORGANIZATION,
                    organization.getName());
            dto.setPath("/system/organization/update");
            dto.setMethod(HttpMethodConstants.POST.name());
            // 新增的组织管理员ID
            List<String> newUserIds = request.getUserIds();
            List<User> newOrgUsers = QueryChain.of(User.class).
                    where(User::getId).in(newUserIds).
                    list();
            // 旧的组织管理员ID
            List<String> oldUserIds = organizationService.getOrgAdminIds(request.getId());
            List<User> oldOrgUsers = QueryChain.of(User.class).
                    where(User::getId).in(oldUserIds).
                    list();
            dto.setOriginalValue(JsonUtils.toJsonBytes(oldOrgUsers));
            dto.setModifiedValue(JsonUtils.toJsonBytes(newOrgUsers));
            return dto;
        }
        return null;
    }


    /**
     * 删除组织
     *
     * @param id 接口请求参数
     * @return 日志详情
     */
    public LogDTO deleteLog(String id) {
        Organization organization = organizationMapper.selectOneById(id);
        if (organization != null) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    id,
                    null,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.SETTING_SYSTEM_ORGANIZATION,
                    organization.getName());
            dto.setPath("/system/organization/delete");
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JsonUtils.toJsonBytes(organization));
            return dto;
        }
        return null;
    }

    /**
     * 恢复组织
     *
     * @param id 接口请求参数
     * @return 日志详情
     */
    public LogDTO recoverLog(String id) {
        Organization organization = organizationMapper.selectOneById(id);
        if (organization != null) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    id,
                    null,
                    OperationLogType.RECOVER.name(),
                    OperationLogModule.SETTING_SYSTEM_ORGANIZATION,
                    organization.getName());
            dto.setPath("/system/organization/recover");
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JsonUtils.toJsonBytes(organization));
            return dto;
        }
        return null;
    }
}
