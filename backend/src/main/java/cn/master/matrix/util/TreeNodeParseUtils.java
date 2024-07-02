package cn.master.matrix.util;

import cn.master.matrix.entity.Organization;
import cn.master.matrix.entity.Project;
import cn.master.matrix.payload.dto.BaseTreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 06/28/2024
 **/
public class TreeNodeParseUtils {
    public static List<BaseTreeNode> parseOrgProjectMap(Map<Organization, List<Project>> orgProjectMap) {
        List<BaseTreeNode> returnList = new ArrayList<>();
        for (Map.Entry<Organization, List<Project>> entry : orgProjectMap.entrySet()) {
            Organization organization = entry.getKey();
            List<Project> projects = entry.getValue();

            BaseTreeNode orgNode = new BaseTreeNode(organization.getId(), organization.getName(), Organization.class.getName());
            returnList.add(orgNode);

            for (Project project : projects) {
                BaseTreeNode projectNode = new BaseTreeNode(project.getId(), project.getName(), Project.class.getName());
                orgNode.addChild(projectNode);
            }
        }
        return returnList;
    }
}
