export interface OrgProjectTableItem {
    id: string;
    name: string;
    description: string;
    enable: boolean;
    // adminList: UserItem[];
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