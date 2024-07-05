import {alovaInstance} from '/@/api/index.ts'
import {LoginData, LoginRes} from "/@/api/interface/user.ts";
import {LoginUrl, LogoutUrl, RefreshTokenUrl} from "/@/api/requrls/user.ts";

export const loginApi = (param: LoginData) => {
    const method = alovaInstance.Post<LoginRes>(LoginUrl, param)
    method.meta = {
        authRole: 'login'
    };
    return method;
};
export const refreshTokenApi = () => {
    const method = alovaInstance.Post<LoginRes>(RefreshTokenUrl);
    method.meta = {
        authRole: 'refreshToken'
    };
    return method;
};
export const logoutApi = () => {
    const method = alovaInstance.Post(LogoutUrl)
    method.meta = {
        authRole: 'logout'
    };
    return method;
};