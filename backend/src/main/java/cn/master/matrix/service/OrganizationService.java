package cn.master.matrix.service;

import cn.master.matrix.entity.Project;
import cn.master.matrix.payload.dto.*;
import cn.master.matrix.payload.dto.request.*;
import cn.master.matrix.payload.dto.user.UserExtendDTO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import cn.master.matrix.entity.Organization;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 组织 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-06-28T15:16:07.028133900
 */
public interface OrganizationService extends IService<Organization> {

    List<OptionDTO> listAll();

    LinkedHashMap<Organization, List<Project>> getOrgProjectMap();

    void addMemberBySystem(OrganizationMemberBatchRequest request, String createUserId);

    void addMemberBySystem(OrganizationMemberRequest organizationMemberRequest, String createUserId);

    Page<OrgUserExtend> getMemberListByOrg(OrganizationRequest request);

    void addMemberByOrg(OrganizationMemberExtendRequest request, String userId);

    void addMemberRole(OrganizationMemberExtendRequest request, String userId);

    void updateMember(OrganizationMemberUpdateRequest request, String userId);

    void addMemberToProject(OrgMemberExtendProjectRequest request, String userId);

    List<LogDTO> batchDelLog(String organizationId, String userId);

    void removeMember(String organizationId, String userId, String currentUser);

    List<OptionDTO> getProjectList(String organizationId, String keyword);

    List<OptionDTO> getUserRoleList(String organizationId);

    List<OptionDisabledDTO> getUserList(String organizationId, String keyword);

    Organization checkResourceExist(String id);

    Page<OrganizationDTO> list(OrganizationRequest request);

    List<String> getOrgAdminIds(String organizationId);

    void update(OrganizationDTO organizationDTO);

    void updateName(OrganizationDTO organizationDTO);

    void delete(OrganizationDeleteRequest organizationDeleteRequest);

    void recover(String id);

    void enable(String id);

    void disable(String id);

    Page<UserExtendDTO> getMemberListBySystem(OrganizationRequest request);

    OrganizationDTO getDefault();

    Map<String, Long> getTotal(String organizationId);
}
