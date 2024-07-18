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
let isExpired = false;
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
        const {t} = useI18n();
        if (isExpired) return;
        if (response.status >= 400) {
            let errMessage = '';
            const json = await response.json();
            const {message: msg} = json
            switch (response.status) {
                case 400:
                    errMessage = `${msg}`;
                    break;
                case 404:
                    errMessage = msg || t('api.errMsg404');
                    break;
                case 405:
                    errMessage = msg || t('api.errMsg405');
                    break;
                case 408:
                    errMessage = msg || t('api.errMsg408');
                    break;
                case 500:
                    errMessage = msg || t('api.errMsg500');
                    break;
                case 501:
                    errMessage = msg || t('api.errMsg501');
                    break;
                case 502:
                    errMessage = msg || t('api.errMsg502');
                    break;
                case 503:
                    errMessage = msg || t('api.errMsg503');
                    break;
                case 504:
                    errMessage = msg || t('api.errMsg504');
                    break;
                case 505:
                    errMessage = msg || t('api.errMsg505');
                    break;
                default:
            }
            window.$message.error(errMessage)
            throw new Error(response.statusText);
        }
        if (method.meta?.isDownload) {
            return response.blob()
        }
        const json = await response.json();
        if (json.code !== 100200) {
            // 抛出错误或返回reject状态的Promise实例时，此请求将抛出错误
            throw new Error(json.message);
        }
        return json.data;
    }),
});
