package cn.master.matrix.service;

import cn.master.matrix.payload.dto.request.GlobalUserRoleRelationQueryRequest;
import cn.master.matrix.payload.dto.request.GlobalUserRoleRelationUpdateRequest;
import cn.master.matrix.payload.dto.request.user.UserExcludeOptionDTO;
import cn.master.matrix.payload.dto.request.user.UserRoleBatchRelationRequest;
import cn.master.matrix.payload.dto.user.UserRoleRelationUserDTO;
import cn.master.matrix.payload.response.TableBatchProcessResponse;
import com.mybatisflex.core.paginate.Page;

import java.io.Serializable;
import java.util.List;

/**
 * @author Created by 11's papa on 06/28/2024
 **/
public interface GlobalUserRoleRelationService extends BaseUserRoleRelationService{
    TableBatchProcessResponse batchAdd(UserRoleBatchRelationRequest request, String operator);

    void add(GlobalUserRoleRelationUpdateRequest request);

    Page<UserRoleRelationUserDTO> page(GlobalUserRoleRelationQueryRequest request);

    @Override
    void delete(String id);

    List<UserExcludeOptionDTO> getExcludeSelectOption(String roleId, String keyword);
}
