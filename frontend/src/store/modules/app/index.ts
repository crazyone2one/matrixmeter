import {defineStore} from "pinia";
import {ref} from "vue";
import {RouteRecordRaw} from "vue-router";
import {AppState} from "./types";
import {cloneDeep} from "lodash-es";

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
            projectList: []
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
        return {
            state,
            toggleMenu,
            setCurrentOrgId,
            setCurrentProjectId, setTopMenus, getTopMenus, getCurrentTopMenu, setCurrentTopMenu
        };
    },
    {
        persist: {
            paths: ["state.currentOrgId", "state.currentProjectId"],
        },
    }
);
export default useAppStore;
