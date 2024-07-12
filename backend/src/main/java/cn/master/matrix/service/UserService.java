package cn.master.matrix.service;

import cn.master.matrix.payload.dto.TableBatchProcessDTO;
import cn.master.matrix.payload.dto.request.BasePageRequest;
import cn.master.matrix.payload.dto.request.user.PersonalUpdatePasswordRequest;
import cn.master.matrix.payload.dto.request.user.PersonalUpdateRequest;
import cn.master.matrix.payload.dto.request.user.UserCreateRequest;
import cn.master.matrix.payload.dto.request.user.UserEditRequest;
import cn.master.matrix.payload.dto.user.PersonalDTO;
import cn.master.matrix.payload.dto.user.UserDTO;
import cn.master.matrix.payload.dto.user.UserExtendDTO;
import cn.master.matrix.payload.dto.user.UserTableResponse;
import cn.master.matrix.payload.dto.user.response.UserBatchCreateResponse;
import cn.master.matrix.payload.response.TableBatchProcessResponse;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.User;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-21T10:54:08.016115500
 */
public interface UserService extends IService<User> {

    UserDTO getUserByKeyword(String keyword);

    UserEditRequest updateUser(UserEditRequest request, String userId);

    Page<UserTableResponse> page(BasePageRequest request);

    TableBatchProcessResponse deleteUser(TableBatchProcessDTO request, String userId, String username);

    UserBatchCreateResponse save(UserCreateRequest userCreateDTO, String source, String operator);

    UserDTO getUserDTO(String userId);

    PersonalDTO getPersonalById(String id);

    boolean updateAccount(PersonalUpdateRequest request, String operator);

    boolean updatePassword(PersonalUpdatePasswordRequest request);

    List<UserExtendDTO> getMemberOption(String sourceId, String keyword);

    List<User> getUserList(String keyword);

    void autoSwitch(UserDTO user);
}
