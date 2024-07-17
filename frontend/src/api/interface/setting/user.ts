// 用户模型
import {BatchApiParams} from "/@/api/interface/common.ts";

export interface UserListItem {
    id: string;
    name: string;
    email: string;
    password: string;
    enable: boolean;
    createTime: number;
    updateTime: number;
    language: string; // 语言
    lastOrganizationId: string; // 当前组织ID
    phone: string;
    source: string; // 来源：LOCAL OIDC CAS OAUTH2
    lastProjectId: string; // 当前项目ID
    createUser: string;
    updateUser: string;
    organizationList: OrganizationListItem[]; // 用户所属组织
    userRoleList: UserRoleListItem[]; // 用户所属用户组
    userRoles?: UserRoleListItem[]; // 用户所属用户组
    selectUserGroupVisible?: boolean
}

export interface OrganizationListItem {
    id: string;
    num: number;
    name: string;
    description: string;
    createTime: number;
    updateTime: number;
    createUser: string;
    updateUser: string;
    deleted: boolean; // 是否删除
    deleteUser: string;
    deleteTime: number;
    enable: boolean; // 是否启用
}

export interface UserRoleListItem {
    id: string;
    name: string;
    description: string;
    internal: boolean; // 是否内置用户组
    type: string; // 所属类型 SYSTEM ORGANIZATION PROJECT
    createTime: number;
    updateTime: number;
    createUser: string;
    scopeId: string; // 应用范围
}

export interface UpdateUserStatusParams extends BatchApiParams {
    enable: boolean;
}

export type DeleteUserParams = BatchApiParams;
export type ResetUserPasswordParams = BatchApiParams;

// 创建用户模型
export interface SimpleUserInfo {
    id?: string;
    name: string;
    email: string;
    phone?: string;
}

export interface SystemRole {
    id: string;
    name: string;
    selected: boolean; // 是否可选
    closeable: boolean; // 是否可取消
}

export interface CreateUserResult {
    errorEmails: Record<string, any>;
    successList: any[];
}

export interface CreateUserParams {
    userInfoList: SimpleUserInfo[];
    userRoleIdList: string[];
}

export interface UpdateUserInfoParams extends SimpleUserInfo {
    id: string;
    userRoleIdList: string[];
}

export interface ImportUserParams {
    fileList: (File)[];
}
