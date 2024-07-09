import {alovaInstance} from '/@/api/index.ts'
import {CommonList, TableQueryParams} from "/@/api/interface/common.ts";
import * as orgUrl from '/@/api/requrls/setting/organizationAndProject.ts';
import {OrgProjectTableItem} from "/@/api/interface/setting/org-project.ts";

export const postOrgTable = (page = 1, pageSize = 10, param: TableQueryParams) => {
    param.pageNum = page
    param.pageSize = pageSize
    return alovaInstance.Post<CommonList<OrgProjectTableItem>>(orgUrl.postOrgTableUrl, param)
}