import {alovaInstance} from '/@/api/index.ts'
import {CommonList, TableQueryParams} from "/@/api/interface/common.ts";
import {
    CreateUserParams,
    CreateUserResult,
    DeleteUserParams,
    ResetUserPasswordParams,
    SystemRole,
    UpdateUserInfoParams,
    UpdateUserStatusParams,
    UserListItem
} from "/@/api/interface/setting/user.ts";
import {
    CreateUserUrl,
    DeleteUserUrl,
    EnableUserUrl,
    GetSystemRoleUrl,
    GetUserListUrl,
    ResetPasswordUrl,
    UpdateUserUrl
} from "/@/api/requrls/setting/user.ts";

/**
 * 获取用户列表
 * @param page
 * @param pageSize
 * @param param
 */
export const getUserList = (page = 1, pageSize = 10, param: TableQueryParams) => {
    param.pageNum = page
    param.pageSize = pageSize
    return alovaInstance.Post<CommonList<UserListItem>>(GetUserListUrl, param, {
        // 函数接受响应数据和响应头数据，并要求将转换后的数据返回。
        // transform(rawData: CommonList<UserListItem>) {
        //     return rawData.records.map(item => {
        //         return {
        //             ...item,
        //             selectUserGroupVisible: false,
        //             selectUserGroupLoading: false,
        //         };
        //     });
        // }
    })
}
/**
 * 更新用户启用/禁用状态
 * @param param
 */
export const toggleUserStatus = (param: UpdateUserStatusParams) => alovaInstance.Post(EnableUserUrl, param)
/**
 * 删除用户
 * @param param
 */
export const deleteUserInfo = (param: DeleteUserParams) => alovaInstance.Post(DeleteUserUrl, param)
/**
 * 重置用户密码
 * @param param
 */
export const resetUserPassword = (param: ResetUserPasswordParams) => alovaInstance.Post(ResetPasswordUrl, param)
/**
 * 获取系统用户组
 */
export const getSystemRoles = () => alovaInstance.Get<SystemRole[]>(GetSystemRoleUrl)
/**
 * 批量创建用户
 * @param param
 */
export const batchCreateUser = (param: CreateUserParams) => alovaInstance.Post<CreateUserResult>(CreateUserUrl, param)
/**
 * 更新用户信息
 * @param param
 */
export const updateUserInfo = (param: UpdateUserInfoParams) => alovaInstance.Post(UpdateUserUrl, param)