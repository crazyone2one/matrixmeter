package cn.master.matrix.service.log;

import cn.master.matrix.constants.OperationLogConstants;
import cn.master.matrix.constants.OperationLogModule;
import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.entity.UserRole;
import cn.master.matrix.payload.dto.LogDTO;
import cn.master.matrix.payload.dto.request.PermissionSettingUpdateRequest;
import cn.master.matrix.payload.dto.request.UserRoleUpdateRequest;
import cn.master.matrix.util.JsonUtils;
import com.mybatisflex.core.query.QueryChain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Created by 11's papa on 07/01/2024
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class GlobalUserRoleLogService {

    public LogDTO addLog(UserRoleUpdateRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.SETTING_SYSTEM_USER_GROUP,
                request.getName());

        dto.setOriginalValue(JsonUtils.toJsonBytes(request));
        return dto;
    }

    public LogDTO updateLog(UserRoleUpdateRequest request) {
        UserRole userRole = QueryChain.of(UserRole.class).where(UserRole::getId).eq(request.getId()).one();
        LogDTO dto = null;
        if (userRole != null) {
            dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    userRole.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_SYSTEM_USER_GROUP,
                    userRole.getName());

            dto.setOriginalValue(JsonUtils.toJsonBytes(userRole));
        }
        return dto;
    }

    public LogDTO updateLog(PermissionSettingUpdateRequest request) {
        UserRole userRole = QueryChain.of(UserRole.class).where(UserRole::getId).eq(request.getUserRoleId()).one();
        LogDTO dto = null;
        if (userRole != null) {
            dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    request.getUserRoleId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_SYSTEM_USER_GROUP,
                    userRole.getName());

            dto.setOriginalValue(JsonUtils.toJsonBytes(request));
        }
        return dto;
    }

    public LogDTO deleteLog(String id) {
        UserRole userRole = QueryChain.of(UserRole.class).where(UserRole::getId).eq(id).one();
        if (userRole == null) {
            return null;
        }
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                userRole.getId(),
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.SETTING_SYSTEM_USER_GROUP,
                userRole.getName());

        dto.setOriginalValue(JsonUtils.toJsonBytes(userRole));
        return dto;
    }
}
