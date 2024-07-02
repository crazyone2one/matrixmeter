import { createAlova } from "alova";
import fetchAdapter from "alova/fetch";
import vueHook from "alova/vue";

export const alovaInstance = createAlova({
  requestAdapter: fetchAdapter(),
  baseURL: import.meta.env.BASE_URL,
  timeout: 5000,
  statesHook: vueHook,
  beforeRequest(method) {
    if (!method.meta.ignoreToken) {
      method.config.headers.token = "token";
    }
  },
  responded: {
    onSuccess: async (response, method) => {
      if (response.status >= 400) {
        throw new Error(response.statusText);
      }
      const json = await response.json();
      if (json.code !== 200) {
        // 抛出错误或返回reject状态的Promise实例时，此请求将抛出错误
        throw new Error(json.message);
      }

      // 解析的响应数据将传给method实例的transform钩子函数，这些函数将在后续讲解
      return method.meta?.isDownload ? response.blob() : json.data;
    },
    onError: (err, method) => {
      console.log("🍞 method", "color:#6ec1c2", method);
      console.log("🍞 err", "color:#465975", err);
    },
  },
});
