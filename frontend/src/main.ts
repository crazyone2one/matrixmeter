import {createApp} from "vue";
import App from "./App.vue";
import router from "./router";
import pinia from "./store";
import "./style.css";
import vLoading from "./utils/loading";
import naive from "./utils/naive";
import {setupI18n} from "/@/locale";
import 'virtual:uno.css'
import permission from '/@/directive/permission/index.ts'
import outerClick from '/@/directive/outer-click/index.ts'

const bootstrap = async () => {
    const app = createApp(App);
    app.use(router).use(naive).use(pinia);
    await setupI18n(app);
    app.directive("loading", vLoading);
    app.directive("permission", permission);
    app.directive('outer', outerClick);
    app.mount("#app");
};

bootstrap().then(() => console.log("welcome to Matrix"));
