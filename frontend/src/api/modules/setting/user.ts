import {alovaInstance} from '/@/api/index.ts'
import {CommonList, TableQueryParams} from "/@/api/interface/common.ts";
import {UserListItem} from "/@/api/interface/setting/user.ts";
import {GetUserListUrl} from "/@/api/requrls/setting/user.ts";

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
        transform(rawData: CommonList<UserListItem>) {
            return rawData.records.map(item => {
                return {
                    ...item,
                    selectUserGroupVisible: false,
                    selectUserGroupLoading: false,
                };
            });
        }
    })
}