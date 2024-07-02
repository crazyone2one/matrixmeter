package cn.master.matrix.service.log;

import cn.master.matrix.constants.OperationLogConstants;
import cn.master.matrix.constants.OperationLogModule;
import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.entity.User;
import cn.master.matrix.entity.UserRole;
import cn.master.matrix.entity.UserRoleRelation;
import cn.master.matrix.payload.dto.LogDTO;
import cn.master.matrix.payload.dto.OptionDTO;
import cn.master.matrix.payload.dto.request.GlobalUserRoleRelationUpdateRequest;
import cn.master.matrix.util.JsonUtils;
import com.mybatisflex.core.query.QueryChain;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.master.matrix.entity.table.UserTableDef.USER;

/**
 * @author Created by 11's papa on 07/01/2024
 **/
@Service
public class GlobalUserRoleRelationLogService {
    public LogDTO addLog(GlobalUserRoleRelationUpdateRequest request) {
        UserRole userRole = QueryChain.of(UserRole.class).where(UserRole::getId).eq(request.getRoleId()).one();
        List<String> userIds = request.getUserIds();
        List<OptionDTO> users = QueryChain.of(User.class)
                .select(USER.ID, USER.NAME).from(USER)
                .where(USER.ID.in(userIds)).listAs(OptionDTO.class);
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                userRole.getId(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.SETTING_SYSTEM_USER_GROUP,
                userRole.getName());

        dto.setOriginalValue(JsonUtils.toJsonBytes(users));
        return dto;
    }

    public LogDTO deleteLog(String id) {
        UserRoleRelation userRoleRelation = QueryChain.of(UserRoleRelation.class).where(UserRoleRelation::getId).eq(id).one();
        UserRole userRole = QueryChain.of(UserRole.class).where(UserRole::getId).eq(userRoleRelation.getRoleId()).one();
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                userRole.getId(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.SETTING_SYSTEM_USER_GROUP,
                userRole.getName());
        OptionDTO optionDTO = QueryChain.of(User.class)
                .select(USER.ID, USER.NAME).from(USER)
                .where(USER.ID.eq(userRoleRelation.getUserId())).objAs(OptionDTO.class);
        // 记录用户id和name
        dto.setOriginalValue(JsonUtils.toJsonBytes(optionDTO));
        return dto;
    }
}
