import {createAlova} from "alova";
import {createServerTokenAuthentication} from "alova/client";
import fetchAdapter from "alova/fetch";
import vueHook from "alova/vue";
import {useAppStore, useUserStore} from "../store";
import {clearToken, getToken} from "../utils/auth";
import {refreshTokenApi} from "/@/api/modules/user";
import router from "/@/router";
import {removeRouteListener} from "/@/utils/route-listener.ts";
import {useI18n} from "/@/hooks/use-i18n.ts";


const {onAuthRequired, onResponseRefreshToken} =
    createServerTokenAuthentication({
        assignToken: (method) => {
            const token = getToken();
            if (!method.meta?.authRole && method.meta?.authRole !== "refreshToken") {
                method.config.headers.Authorization = `Bearer ${token.accessToken}`;
            } else {
                method.config.headers.Authorization = `Bearer ${token.refreshToken}`;
            }
        },
        logout(_response, _method) {
            const userStore = useUserStore();
            userStore.$reset();
            clearToken();
            removeRouteListener();
            const appStore = useAppStore();

            appStore.setTopMenus([]);
            const currentRoute = router.currentRoute.value;
            router
                .push({
                    name: "login",
                    query: {
                        ...router.currentRoute.value.query,
                        redirect: currentRoute.name as string,
                    },
                })
                .then(() => "");
        },
        refreshTokenOnSuccess: {
            // 当服务端返回401时，表示token过期
            isExpired: (response, _method) => {
                return response.status === 401;
            },
            // 当token过期时触发，在此函数中触发刷新token
            handler: async (_response, _method) => {
                try {
                    const {accessToken, refreshToken} = await refreshTokenApi();
                    localStorage.setItem("token", accessToken);
                    localStorage.setItem("refresh_token", refreshToken);
                } catch (error) {
                    // token刷新失败，跳转回登录页
                    const currentRoute = router.currentRoute.value;
                    router
                        .push({
                            name: "login",
                            query: {
                                ...router.currentRoute.value.query,
                                redirect: currentRoute.name as string,
                            },
                        })
                        .then(() => "");
                    // 并抛出错误
                    throw error;
                }
            },
        },
    });

export const alovaInstance = createAlova({
    requestAdapter: fetchAdapter(),
    baseURL: import.meta.env.VITE_APP_BASE_API,
    timeout: 300 * 1000,
    statesHook: vueHook,
    beforeRequest: onAuthRequired((method) => {
        const appStore = useAppStore();
        method.config.headers.ORGANIZATION = appStore.state.currentOrgId;
        method.config.headers.PROJECT = appStore.state.currentProjectId;
    }),
    responded: onResponseRefreshToken(async (response, method) => {
        const { t } = useI18n();
        if (response.status >= 400) {
            switch (response.status) {
                case 400:
                    window.$message.error(response.statusText)
                    break;
                case 500:
                    window.$message.error(t('api.errMsg500'))
                    break
            }
            throw new Error(response.statusText);
        }
        const json = await response.json();
        if (json.code !== 100200) {
            // 抛出错误或返回reject状态的Promise实例时，此请求将抛出错误
            throw new Error(json.message);
        }
        // 解析的响应数据将传给method实例的transform钩子函数，这些函数将在后续讲解
        return method.meta?.isDownload ? response.blob() : json.data;
    }),
});
