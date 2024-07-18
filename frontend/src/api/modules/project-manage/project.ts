import {alovaInstance} from '/@/api/index.ts'
import {ProjectSwitchUrl} from "/@/api/requrls/system.ts";

export const switchProject = (data: { projectId: string; userId: string }) => alovaInstance.Post(ProjectSwitchUrl, data)