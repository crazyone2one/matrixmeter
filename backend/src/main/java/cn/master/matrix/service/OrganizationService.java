package cn.master.matrix.service;

import cn.master.matrix.entity.Project;
import cn.master.matrix.payload.dto.LogDTO;
import cn.master.matrix.payload.dto.OptionDTO;
import cn.master.matrix.payload.dto.OptionDisabledDTO;
import cn.master.matrix.payload.dto.OrgUserExtend;
import cn.master.matrix.payload.dto.request.*;
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
}
