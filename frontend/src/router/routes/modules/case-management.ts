import {AppRouteRecordRaw} from "/@/router/routes/types.ts";
import {CaseManagementRouteEnum} from "/@/enums/routeEnum.ts";
import {DEFAULT_LAYOUT} from "/@/router/routes/base.ts";

const CaseManagement: AppRouteRecordRaw = {
    path: '/case-management',
    name: CaseManagementRouteEnum.CASE_MANAGEMENT,
    redirect: '/case-management/featureCase',
    component: DEFAULT_LAYOUT,
    meta: {
        locale: 'menu.caseManagement',
        collapsedLocale: 'menu.caseManagementShort',
        icon: 'icon-icon_functional_testing1',
        order: 3,
        hideChildrenInMenu: true,
        roles: ['FUNCTIONAL_CASE:READ', 'CASE_REVIEW:READ'],
    },
    children: [
        // 功能用例
        {
            path: 'featureCase',
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE,
            component: () => import('/@/views/case-management/case-management-feature/index.vue'),
            meta: {
                locale: 'menu.caseManagementShort',
                roles: ['FUNCTIONAL_CASE:READ'],
                isTopMenu: true,
            },
        },
    ]
}
export default CaseManagement;