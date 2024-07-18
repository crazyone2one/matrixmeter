import {alovaInstance} from '/@/api/index.ts'
import {ProjectListUrl, ProjectSwitchUrl} from "/@/api/requrls/system.ts";
import {ProjectListItem} from "/@/api/interface/setting/project.ts";

export const switchProject = (data: { projectId: string; userId: string }) => alovaInstance.Post(ProjectSwitchUrl, data)
export const getProjectList = (organizationId: string) => {
    const method = alovaInstance.Get<ProjectListItem[]>(`${ProjectListUrl}/${organizationId}`)
    // method.meta = {
    //     authRole: null
    // };
    return method;

}