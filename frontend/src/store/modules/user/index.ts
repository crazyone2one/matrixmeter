import {defineStore} from "pinia";
import useAppStore from "../app";
import {UserState} from "./types";
import {composePermissions} from "/@/utils/permission";
import {LoginData} from "/@/api/interface/user.ts";
import {loginApi, logoutApi} from "/@/api/modules/user";
import {clearToken, setToken} from "/@/utils/auth.ts";
import {useI18n} from "/@/hooks/use-i18n.ts";

const useUserStore = defineStore("user", {
    persist: true,
    state: (): UserState => ({
        name: undefined,
        avatar: undefined,
        // job: undefined,
        organization: undefined,
        location: undefined,
        email: undefined,
        introduction: undefined,
        personalWebsite: undefined,
        jobName: undefined,
        organizationName: undefined,
        locationName: undefined,
        phone: undefined,
        registrationDate: undefined,
        id: undefined,
        // certification: undefined,
        // role: "",
        userRolePermissions: [],
        userRoles: [],
        userRoleRelations: [],
        // loginType: [],
        // hasLocalExec: false, // 是否配置了api本地执行
        // isPriorityLocalExec: false, // 是否优先本地执行
        // localExecuteUrl: "",
        // lastProjectId: "",
        accessToken: '',
        refreshToken: '',
    }),
    getters: {
        userInfo(state: UserState): UserState {
            return {...state};
        },
        isAdmin(state: UserState): boolean {
            if (!state.userRolePermissions) return false;
            return (
                state.userRolePermissions.findIndex(
                    (ur) => ur.userRole.id === "admin"
                ) > -1
            );
        },
        currentRole(state: UserState): {
            projectPermissions: string[];
            orgPermissions: string[];
            systemPermissions: string[];
        } {
            const appStore = useAppStore();

            state.userRoleRelations?.forEach((ug) => {
                state.userRolePermissions?.forEach((gp) => {
                    if (gp.userRole.id === ug.roleId) {
                        ug.userRolePermissions = gp.userRolePermissions;
                        ug.userRole = gp.userRole;
                    }
                });
            });

            return {
                projectPermissions: composePermissions(
                    state.userRoleRelations || [],
                    "PROJECT",
                    appStore.state.currentProjectId
                ),
                orgPermissions: composePermissions(
                    state.userRoleRelations || [],
                    "ORGANIZATION",
                    appStore.state.currentOrgId
                ),
                systemPermissions: composePermissions(
                    state.userRoleRelations || [],
                    "SYSTEM",
                    "global"
                ),
            };
        },
    },
    actions: {
        setInfo(partial: Partial<UserState>) {
            this.$patch(partial);
        },
        // 重置用户信息
        resetInfo() {
            this.$reset();
        },
        async login(loginForm: LoginData) {
            try {
                const res = await loginApi(loginForm)
                const appStore = useAppStore();
                this.setInfo(res);
                setToken(res.accessToken, res.refreshToken);
                appStore.setCurrentOrgId(res.lastOrganizationId || '');
                appStore.setCurrentProjectId(res.lastProjectId || '');
            } catch (err) {
                clearToken();
                throw err;
            }
        },
        async logout() {
            const {t} = useI18n();
            try {
                await logoutApi();
                window.$message.success(t('message.logoutSuccess'))
            } finally {
                this.resetInfo();
            }
        }
    },
});
export default useUserStore;
