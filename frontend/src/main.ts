import { createApp } from "vue";
import App from "./App.vue";
import "./style.css";
import vLoading from "./utils/loading";

const app = createApp(App);
app.directive("loading", vLoading);
app.mount("#app");
