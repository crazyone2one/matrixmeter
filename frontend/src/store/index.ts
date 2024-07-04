import { createPinia } from "pinia";
import piniaPluginPersistedstate from "pinia-plugin-persistedstate";
import useAppStore from "./modules/app";
const pinia = createPinia().use(piniaPluginPersistedstate);

export { useAppStore };
export default pinia;
