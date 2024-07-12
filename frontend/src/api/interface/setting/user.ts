// 用户模型
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