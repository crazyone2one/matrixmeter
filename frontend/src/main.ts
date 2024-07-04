import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import pinia from "./store";
import "./style.css";
import vLoading from "./utils/loading";
import naive from "./utils/naive";
import { setupI18n } from "/@/locale";

const bootstrap = async () => {
  const app = createApp(App);
  app.use(router).use(naive).use(pinia);
  await setupI18n(app);
  app.directive("loading", vLoading);
  app.mount("#app");
};

bootstrap().then(() => console.log("welcome to Matrix"));
