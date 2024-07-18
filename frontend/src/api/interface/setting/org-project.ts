import {UserItem} from "/@/api/interface/setting/system/log.ts";
import {TableQueryParams} from "/@/api/interface/common.ts";

export interface OrgProjectTableItem {
    id: string;
    name: string;
    description: string;
    enable: boolean;
    adminList: UserItem[];
    organizationId: string;
    organizationName: string;
    num: number;
    updateTime: number;
    createTime: number;
    memberCount: number;
    projectCount: number;
    userIds: string[];
    resourcePoolIds: string[];
}
export interface SystemGetUserByOrgOrProjectIdParams extends TableQueryParams {
    projectId?: string;
    organizationId?: string;
}
export interface AddUserToOrgOrProjectParams {
    userIds?: string[];
    organizationId?: string;
    projectId?: string;
    // 等待接口改动 将要废弃，以后用userIds
    memberIds?: string[];
}
export interface SystemOrgOption {
    id: string;
    name: string;
}