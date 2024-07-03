package cn.master.matrix.service;

import cn.master.matrix.entity.*;
import cn.master.matrix.payload.dto.request.user.UserExcludeOptionDTO;
import cn.master.matrix.payload.dto.user.UserTableResponse;
import com.mybatisflex.core.service.IService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

/**
 * 用户组关系 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-27T16:20:34.166413800
 */
public interface BaseUserRoleRelationService extends IService<UserRoleRelation> {
    void updateUserSystemGlobalRole(@Valid User user, @Valid @NotEmpty String operator, @Valid @NotEmpty List<String> roleList);

    Map<String, UserTableResponse> selectGlobalUserRoleAndOrganization(List<String> userIdList);

    void deleteByUserIdList(List<String> userIdList);

    void batchSave(List<String> userRoleIdList, List<User> saveUserList);

    void deleteByRoleId(String roleId);

    List<UserRoleRelation> getByRoleId(String roleId);

    List<String> getUserIdByRoleId(String roleId);

    List<UserRoleRelation> getUserIdAndSourceIdByUserIds(List<String> userIds);

    void batchInsert(List<UserRoleRelation> addRelations);

    void checkExist(UserRoleRelation userRoleRelation);

    UserRole getUserRole(String id);

    void delete(String id);

    List<UserExcludeOptionDTO> getExcludeSelectOptionWithLimit(String roleId, String keyword);

    Map<Organization, List<Project>> selectOrganizationProjectByUserId(String userId);
}
