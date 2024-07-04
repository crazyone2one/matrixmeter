/// <reference types="vite/client" />
declare module "*.vue" {
  import type {
    DialogProviderInst,
    MessageProviderInst,
    NotificationProviderInst,
  } from "naive-ui";
  import { DefineComponent } from "vue";
  // 增加全局类型
  global {
    interface Window {
      $message: MessageProviderInst;
      $dialog: DialogProviderInst;
      $notification: NotificationProviderInst;
    }
  }
  const component: DefineComponent;
  export default component;
}
interface ImportMetaEnv {
  readonly VITE_APP_TITLE: string;
  readonly VITE_APP_BASE_URL: string;
  readonly VITE_APP_PROXY_URL: string;
  // 更多环境变量...
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
type LocaleType = "zh-CN" | "en-US";
type Recordable<T = any> = Record<string, T>;
