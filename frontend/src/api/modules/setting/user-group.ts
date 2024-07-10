import {alovaInstance} from '/@/api/index.ts'
import {UserGroupAuthSetting, UserGroupItem} from "/@/api/interface/setting/user-group.ts";
import * as ugUrl from '/@/api/requrls/setting/usergroup';

/**
 * 系统-获取用户组列表
 */
export const getUserGroupList = () => alovaInstance.Get<UserGroupItem[]>(ugUrl.getUserGroupU)
export const getGlobalUSetting = (id: string) => alovaInstance.Get<UserGroupAuthSetting[]>(`${ugUrl.getGlobalUSettingUrl}${id}`)