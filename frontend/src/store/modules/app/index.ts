import {defineStore} from "pinia";
import {ref} from "vue";
import {RouteRecordRaw} from "vue-router";
import {AppState} from "./types";

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
        });
        const toggleMenu = (value: boolean) => {
            state.value.hideMenu = value;
        };
        const setTopMenus = (menus: RouteRecordRaw[] | undefined) => state.value.topMenus = menus ? [...menus] : []
        const setCurrentOrgId = (id: string) => (state.value.currentOrgId = id);
        const setCurrentProjectId = (id: string) =>
            (state.value.currentProjectId = id);
        return {
            state,
            toggleMenu,
            setCurrentOrgId,
            setCurrentProjectId,setTopMenus
        };
    },
    {
        persist: {
            paths: ["currentOrgId", "currentProjectId", "menuCollapse"],
        },
    }
);
export default useAppStore;
