export default interface CommonResponse<T> {
    code: number;
    message: string;
    messageDetail: string;
    data: T;
}

export interface TableQueryParams {
    // 当前页
    pageNum?: number;
    // 每页条数
    pageSize?: number;
    // 排序仅针对单个字段
    sort?: object;
    // 排序仅针对单个字段
    sortString?: string;
    // 表头筛选
    filter?: object;
    // 查询条件
    keyword?: string;

    [key: string]: any;
}

export interface CommonList<T> {
    [x: string]: any;

    pageSize: number;
    totalRow: number;
    totalPage: number;
    pageNumber: number;
    records: T[];
}

export interface BatchActionQueryParams {
    excludeIds?: string[]; // 排除的id
    selectedIds?: string[];
    selectIds?: string[]; // 选中的id
    selectAll: boolean; // 是否跨页全选
    params?: TableQueryParams; // 查询参数
    currentSelectCount?: number; // 当前选中的数量
    condition?: any; // 查询条件
}

export interface BatchApiParams {
    selectIds: string[]; // 已选 ID 集合，当 selectAll 为 false 时接口会使用该字段
    excludeIds?: string[]; // 需要忽略的用户 id 集合，当selectAll为 true 时接口会使用该字段
    selectAll: boolean; // 是否跨页全选，即选择当前筛选条件下的全部表格数据
    condition: Record<string, any>; // 当前表格查询的筛选条件
    currentSelectCount?: number; // 当前已选择的数量
    projectId?: string; // 项目 ID
    moduleIds?: (string | number)[]; // 模块 ID 集合
    versionId?: string; // 版本 ID
    refId?: string; // 版本来源
    protocols?: string[]; // 协议集合
}