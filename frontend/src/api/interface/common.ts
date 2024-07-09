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