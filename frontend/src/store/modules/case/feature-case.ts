import {defineStore} from "pinia";
import {ModuleTreeNode} from "/@/api/interface/common.ts";
import {CustomAttributes} from "/@/api/interface/case-management/feature-case.ts";

const useFeatureCaseStore = defineStore('featureCase',{
    persist: true,
    state: (): {
        moduleId: string[]; // 当前选中模块
        caseTree: ModuleTreeNode[]; // 用例树
        modulesCount: Record<string, any>; // 用例树模块数量
        recycleModulesCount: Record<string, any>; // 回收站模块数量
        operatingState: boolean; // 操作状态
        countMap: Record<string, any>;
        activeTab: string; // 激活tab
        defaultFields: CustomAttributes[];
        defaultCount: Record<string, any>;
    } => ({
        moduleId: [],
        caseTree: [],
        modulesCount: {},
        recycleModulesCount: {},
        operatingState: false,
        activeTab: 'detail',
        defaultFields: [],
        defaultCount: {},
        countMap: {
            case: '0',
            dependency: '0',
            caseReview: '0',
            testPlan: '0',
            bug: '0',
            requirement: '0',
            comments: '0',
            changeHistory: '0',
        },
    }),
})

export default useFeatureCaseStore;