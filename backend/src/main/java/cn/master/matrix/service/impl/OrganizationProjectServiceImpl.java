package cn.master.matrix.service.impl;

import cn.master.matrix.constants.OperationLogModule;
import cn.master.matrix.payload.dto.ProjectDTO;
import cn.master.matrix.payload.dto.request.AddProjectRequest;
import cn.master.matrix.service.CommonProjectService;
import cn.master.matrix.service.OrganizationProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Created by 11's papa on 07/02/2024
 **/
@Service
@RequiredArgsConstructor
public class OrganizationProjectServiceImpl implements OrganizationProjectService {
    private final CommonProjectService commonProjectService;
    private final static String PREFIX = "/organization-project";
    private final static String ADD_PROJECT = PREFIX + "/add";
    private final static String UPDATE_PROJECT = PREFIX + "/update";
    private final static String REMOVE_PROJECT_MEMBER = PREFIX + "/remove-member/";
    private final static String ADD_MEMBER = PREFIX + "/add-member";

    @Override
    public ProjectDTO add(AddProjectRequest request, String userId) {
        return commonProjectService.add(request, userId, ADD_PROJECT, OperationLogModule.SETTING_ORGANIZATION_PROJECT);
    }
}
