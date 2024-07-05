import type {RouteRecordNormalized, RouteRecordRaw} from "vue-router";
import {ProjectListItem} from "/@/api/interface/setting/project.ts";

export interface AppState {
    navbar: boolean;
    menu: boolean;
    hideMenu: boolean;
    menuCollapse: boolean;
    footer: boolean;
    menuWidth: number;
    collapsedWidth: number;
    globalSettings: boolean;
    device: string;
    tabBar: boolean;
    serverMenu: RouteRecordNormalized[];
    loading: boolean;
    loadingTip: string;
    topMenus: RouteRecordRaw[];
    currentTopMenu: RouteRecordRaw;
    //   breadcrumbList: BreadcrumbItem[];
    currentOrgId: string;
    currentProjectId: string;
    //   pageSize: number;
    //   showPageSize: boolean;
    //   showTotal: boolean;
    //   showJumper: boolean;
    //   hideOnSinglePage: boolean;
    version: string;
    //   defaultThemeConfig: ThemeConfig;
    //   defaultLoginConfig: LoginConfig;
    //   defaultPlatformConfig: PlatformConfig;
    //   pageConfig: PageConfig;
    innerHeight: number;
    currentMenuConfig: string[];
    packageType: string;
    projectList: ProjectListItem[];
    ordList: { id: string; name: string }[];
    //   envList: EnvironmentItem[];
    //   currentEnvConfig?: EnvConfig; // 当前环境配置信息
}
