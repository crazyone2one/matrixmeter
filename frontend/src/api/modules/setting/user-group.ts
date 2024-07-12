import {alovaInstance} from '/@/api/index.ts'
import {
    OrgUserGroupParams,
    SaveGlobalUSettingData,
    SystemUserGroupParams,
    UserGroupAuthSetting,
    UserGroupItem,
    UserTableItem
} from "/@/api/interface/setting/user-group.ts";
import * as ugUrl from '/@/api/requrls/setting/usergroup';
import {CommonList, TableQueryParams} from "/@/api/interface/common.ts";

/**
 * 系统-获取用户组列表
 */
export const getUserGroupList = () => alovaInstance.Get<UserGroupItem[]>(ugUrl.getUserGroupU)
export const getGlobalUSetting = (id: string) => alovaInstance.Get<UserGroupAuthSetting[]>(`${ugUrl.getGlobalUSettingUrl}${id}`)
/**
 * 系统-删除用户组对应的用户
 * @param id
 */
export const deleteUserFromUserGroup = (id: string) => alovaInstance.Get<UserGroupAuthSetting[]>(`${ugUrl.deleteUserFromUserGroupUrl}${id}`);
/**
 * 组织-删除用户组对应的用户
 * @param param
 */
export const deleteOrgUserFromUserGroup = (param: { userRoleId: string; userIds: string[]; organizationId: string }) =>
    alovaInstance.Post<CommonList<UserTableItem[]>>(ugUrl.deleteOrgUserFromUserGroupUrl, param)
/**
 * 系统-获取需要关联的用户选项
 * @param param
 */
export const getSystemUserGroupOption = (param: Record<string, any>) =>
    alovaInstance.Get<UserTableItem[]>(`${ugUrl.getSystemUserGroupOptionUrl}${param.roleId}`, {
        params: {
            param
        },
        transform(rawData: UserTableItem[]) {
            return rawData.map(item => {
                return {
                    ...item,
                    disabled: item.exclude
                };
            });
        }
    })
/**
 * 组织-获取需要关联的用户选项
 * @param organizationId
 * @param roleId
 * @param keyword
 */
export const getOrgUserGroupOption = (organizationId: string, roleId: string, keyword: string) =>
    alovaInstance.Get<UserTableItem[]>(`${ugUrl.getOrgUserGroupOptionUrl}${organizationId}/${roleId}`, {
        params: {
            keyword
        }
    })
/**
 * 系统-编辑用户组对应的权限配置
 * @param param
 */
export const saveGlobalUSetting = (param: SaveGlobalUSettingData) => alovaInstance.Post<UserGroupAuthSetting[]>(ugUrl.editGlobalUSettingUrl, param)
/**
 * 组织-编辑用户组对应的权限配置
 * @param param
 */
export const saveOrgUSetting = (param: SaveGlobalUSettingData) => alovaInstance.Post<UserGroupAuthSetting[]>(ugUrl.editOrgUSettingUrl, param)
/**
 * 系统-获取用户组对应的用户列表
 * @param page
 * @param pageSize
 * @param param
 */
export const postUserByUserGroup = (page = 1, pageSize = 10, param: TableQueryParams) => {
    param.pageNum = page
    param.pageSize = pageSize
    return alovaInstance.Post<CommonList<UserTableItem>>(ugUrl.postUserByUserGroupUrl, param)
}
/**
 * 组织-获取用户组对应的用户列表
 * @param page
 * @param pageSize
 * @param param
 */
export const postOrgUserByUserGroup = (page = 1, pageSize = 10, param: TableQueryParams) => {
    param.pageNum = page
    param.pageSize = pageSize
    return alovaInstance.Post<CommonList<UserTableItem>>(ugUrl.postOrgUserByUserGroupUrl, param)
}
/**
 * 系统-添加用户到用户组
 * @param param
 */
export const addUserToUserGroup = (param: { roleId: string; userIds: string[] }) => {
    return alovaInstance.Post(ugUrl.addUserToUserGroupUrl, param)
}
/**
 * 组织-添加用户到用户组
 * @param param
 */
export const addOrgUserToUserGroup = (param: { userRoleId: string; userIds: string[]; organizationId: string }) => {
    return alovaInstance.Post(ugUrl.addOrgUserToUserGroupUrl, param)
}
/**
 * 系统-创建或修改用户组
 * @param param
 */
export const updateOrAddUserGroup = (param: SystemUserGroupParams) =>
    alovaInstance.Post<UserGroupItem>(param.id ? ugUrl.updateUserGroupU : ugUrl.addUserGroupU, param)
/**
 * 组织-创建或修改用户组
 * @param param
 */
export const updateOrAddOrgUserGroup = (param: OrgUserGroupParams) =>
    alovaInstance.Post<UserGroupItem>(param.id ? ugUrl.updateOrgUserGroupU : ugUrl.addOrgUserGroupU, param)

/**
 * 系统-删除用户组
 * @param id
 */
export const deleteUserGroup = (id: string) => alovaInstance.Get<UserGroupAuthSetting[]>(`${ugUrl.deleteUserGroupU}${id}`)
/**
 * 组织-删除用户组
 * @param id
 */
export const deleteOrgUserGroup = (id: string) => alovaInstance.Get<UserGroupAuthSetting[]>(`${ugUrl.deleteOrgUserGroupU}${id}`)