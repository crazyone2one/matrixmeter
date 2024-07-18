// switchUserOrg
import {alovaInstance} from '/@/api/index.ts'
import {SwitchOrgUrl} from "/@/api/requrls/system.ts";

export const switchUserOrg = (organizationId: string, userId: string) =>  alovaInstance.Post(SwitchOrgUrl,{ organizationId, userId })