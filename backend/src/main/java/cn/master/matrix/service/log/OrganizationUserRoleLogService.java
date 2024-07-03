package cn.master.matrix.service.log;

import cn.master.matrix.constants.OperationLogConstants;
import cn.master.matrix.constants.OperationLogModule;
import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.entity.UserRole;
import cn.master.matrix.mapper.UserRoleMapper;
import cn.master.matrix.payload.dto.LogDTO;
import cn.master.matrix.payload.dto.request.OrganizationUserRoleEditRequest;
import cn.master.matrix.payload.dto.request.OrganizationUserRoleMemberEditRequest;
import cn.master.matrix.payload.dto.request.PermissionSettingUpdateRequest;
import cn.master.matrix.util.JsonUtils;
import com.mybatisflex.core.query.QueryChain;
import jakarta.annotation.Resource;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * @author Created by 11's papa on 07/03/2024
 **/
@Service
public class OrganizationUserRoleLogService {
    @Resource
    private UserRoleMapper userRoleMapper;
    /**
     * 新增组织-用户组
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO addLog(OrganizationUserRoleEditRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                request.getScopeId(),
                OperationLogConstants.SYSTEM,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.SETTING_ORGANIZATION_USER_ROLE,
                request.getName());

        dto.setOriginalValue(JsonUtils.toJsonBytes(request.getName()));
        return dto;
    }

    /**
     * 更新组织-用户组
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO updateLog(OrganizationUserRoleEditRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                request.getScopeId(),
                OperationLogConstants.SYSTEM,
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.SETTING_ORGANIZATION_USER_ROLE,
                request.getName());

        val userRoles = QueryChain.of(userRoleMapper).where(UserRole::getId).eq(request.getId()).list();
        UserRole userRole = userRoles.get(0);
        dto.setOriginalValue(JsonUtils.toJsonBytes(userRole.getName()));
        dto.setModifiedValue(JsonUtils.toJsonBytes(request.getName()));
        return dto;
    }

    /**
     * 删除组织-用户组
     * @param id 接口请求参数
     * @return 日志详情
     */
    public LogDTO deleteLog(String id) {
        UserRole userRole = userRoleMapper.selectOneById(id);
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                userRole.getScopeId(),
                OperationLogConstants.SYSTEM,
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.SETTING_ORGANIZATION_USER_ROLE,
                userRole.getName());

        dto.setOriginalValue(JsonUtils.toJsonBytes(userRole.getName()));
        return dto;
    }

    /**
     * 更新组织-用户组-权限
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO updatePermissionSettingLog(PermissionSettingUpdateRequest request) {
        LogDTO dto = getLog(request.getUserRoleId());
        dto.setType(OperationLogType.UPDATE.name());
        dto.setOriginalValue(JsonUtils.toJsonBytes(request));
        return dto;
    }

    /**
     * 更新组织-用户组-成员
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO editMemberLog(OrganizationUserRoleMemberEditRequest request) {
        UserRole userRole = userRoleMapper.selectOneById(request.getUserRoleId());
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                request.getOrganizationId(),
                OperationLogConstants.SYSTEM,
                null,
                null,
                OperationLogModule.SETTING_ORGANIZATION_USER_ROLE,
                userRole.getName());
        dto.setType(OperationLogType.UPDATE.name());
        dto.setModifiedValue(JsonUtils.toJsonBytes(request));
        return dto;
    }

    private LogDTO getLog(String roleId) {
        UserRole userRole = userRoleMapper.selectOneById(roleId);
        return new LogDTO(
                OperationLogConstants.ORGANIZATION,
                userRole.getScopeId(),
                OperationLogConstants.SYSTEM,
                null,
                null,
                OperationLogModule.SETTING_ORGANIZATION_USER_ROLE,
                userRole.getName());
    }
}
