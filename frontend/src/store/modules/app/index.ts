import {defineStore} from "pinia";
import {ref} from "vue";
import {RouteRecordRaw} from "vue-router";
import {AppState} from "./types";
import {cloneDeep} from "lodash-es";
import {getProjectList} from "/@/api/modules/project-manage/project.ts";
import {BreadcrumbItem} from "/@/router/routes/types.ts";

const useAppStore = defineStore(
    "app",
    () => {
        const state = ref<AppState>({
            currentOrgId: "",
            currentProjectId: "",
            version: "",
            navbar: false,
            menu: false,
            hideMenu: false,
            menuCollapse: false,
            footer: false,
            menuWidth: 0,
            collapsedWidth: 0,
            globalSettings: false,
            device: "desktop",
            tabBar: false,
            serverMenu: [],
            loading: false,
            loadingTip: "",
            topMenus: [],
            currentTopMenu: {} as RouteRecordRaw,
            innerHeight: 0,
            currentMenuConfig: [],
            packageType: "",
            ordList: [],
            projectList: [],
            breadcrumbList: []
        });
        const toggleMenu = (value: boolean) => {
            state.value.hideMenu = value;
        };
        const setTopMenus = (menus: RouteRecordRaw[] | undefined) => state.value.topMenus = menus ? [...menus] : []
        const getTopMenus = (state: AppState) => {
            return state.topMenus;
        }
        const getCurrentTopMenu = (state: AppState) => {
            return state.currentTopMenu;
        }
        const setCurrentTopMenu = (menu: RouteRecordRaw) => {
            state.value.currentTopMenu = cloneDeep(menu);
        }
        const setCurrentOrgId = (id: string) => (state.value.currentOrgId = id);
        const setCurrentProjectId = (id: string) =>
            (state.value.currentProjectId = id);
        const initProjectList = async () => {
            if (state.value.currentOrgId) {
                state.value.projectList = await getProjectList(state.value.currentOrgId);
            } else {
                state.value.projectList = [];
            }
        }
        const setBreadcrumbList = (breadcrumbs: BreadcrumbItem[] | undefined) => {
            state.value.breadcrumbList = breadcrumbs ? cloneDeep(breadcrumbs) : []
        }
        return {
            state,
            toggleMenu,
            setCurrentOrgId,
            setCurrentProjectId, setTopMenus, getTopMenus, getCurrentTopMenu, setCurrentTopMenu, initProjectList,
            setBreadcrumbList
        };
    },
    {
        persist: {
            paths: ["state.currentOrgId", "state.currentProjectId", 'state.projectList'],
        },
    }
);
export default useAppStore;
