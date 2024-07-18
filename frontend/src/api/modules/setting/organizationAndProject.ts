import {alovaInstance} from '/@/api/index.ts'
import {CommonList, TableQueryParams} from "/@/api/interface/common.ts";
import * as orgUrl from '/@/api/requrls/setting/organizationAndProject.ts';
import {
    AddUserToOrgOrProjectParams,
    OrgProjectTableItem,
    SystemGetUserByOrgOrProjectIdParams, SystemOrgOption
} from "/@/api/interface/setting/org-project.ts";
import {UserTableItem} from "/@/api/interface/setting/user-group.ts";

export const postOrgTable = (page = 1, pageSize = 10, param: TableQueryParams) => {
    param.pageNum = page
    param.pageSize = pageSize
    return alovaInstance.Post<CommonList<OrgProjectTableItem>>(orgUrl.postOrgTableUrl, param)
}
export const postProjectTable = (page = 1, pageSize = 10, param: TableQueryParams) => {
    param.pageNum = page
    param.pageSize = pageSize
    return alovaInstance.Post<CommonList<OrgProjectTableItem>>(orgUrl.postProjectTableUrl, param)
}
/**
 * 根据 orgId 或 projectId 获取用户列表
 * @param page
 * @param pageSize
 * @param param
 */
export const postUserByOrgIdOrProjectId = (page = 1, pageSize = 10, param: SystemGetUserByOrgOrProjectIdParams) => {
    param.pageNum = page
    param.pageSize = pageSize
    return alovaInstance.Post<CommonList<UserTableItem>>(param.organizationId ? orgUrl.postOrgMemberUrl : orgUrl.postProjectMemberUrl, param)
}
/**
 * 获取用户下拉选项
 * @param sourceId
 * @param keyword
 */
export const getUserByOrganizationOrProject = (sourceId: string, keyword: string) => {
    return alovaInstance.Get<UserTableItem[]>(`${orgUrl.getUserByOrgOrProjectUrl}${sourceId}`, {params: {keyword}});
}
/**
 * 获取用户下拉选项
 * @param keyword
 */
export const getAdminByOrganizationOrProject = (keyword: string) => {
    return alovaInstance.Get<UserTableItem[]>(orgUrl.getAdminByOrgOrProjectUrl, {params: {keyword}});
}
/**
 * 给组织或项目添加成员
 * @param param
 */
export const addUserToOrgOrProject = (param: AddUserToOrgOrProjectParams) => {
    return alovaInstance.Post(param.projectId ? orgUrl.postAddProjectMemberUrl : orgUrl.postAddOrgMemberUrl, param)
}
/**
 * 创建或更新项目
 * @param param
 */
export const createOrUpdateProject = (param: Partial<OrgProjectTableItem>) => {
    return alovaInstance.Post<OrgProjectTableItem>(param.id ? orgUrl.postModifyProjectUrl : orgUrl.postAddProjectUrl, param)
}
/**
 * 获取组织下拉选项
 */
export const getSystemOrgOption = () => {
    return alovaInstance.Post<SystemOrgOption[]>(orgUrl.postOrgOptionsUrl)
}
/**
 * 获取项目和组织的总数
 */
export const getOrgAndProjectCount = () => {
    return alovaInstance.Get<{ organizationTotal: number, projectTotal: number }>(orgUrl.getOrgAndProjectCountUrl)
}
/**
 * 启用或禁用项目
 * @param id
 * @param isEnable
 */
export const enableOrDisableProject = (id: string, isEnable = true) => {
    return alovaInstance.Get(`${isEnable ? orgUrl.getEnableProjectUrl : orgUrl.getDisableProjectUrl}${id}`)
}
/**
 * 删除项目
 * @param id
 */
export const deleteProject = (id: string) => alovaInstance.Get(`${orgUrl.getDeleteProjectUrl}${id}`)