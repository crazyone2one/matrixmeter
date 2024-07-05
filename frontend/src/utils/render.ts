import {RouteRecordName, RouterLink} from "vue-router";
import {useI18n} from "/@/hooks/use-i18n.ts";
import {h} from "vue";
import {NIcon} from "naive-ui";

const {t} = useI18n()
export const renderRouterLink = (name: RouteRecordName, label?: string) => {
    return h(RouterLink, {to: {name}}, {default: () => label ? t(label) : label});
};
export const renderIconStr = async (icon?: keyof typeof import("@vicons/ionicons5")) => {
    if (icon) {
        const {[icon]: iconNode} = await import("@vicons/ionicons5");
        return () => h(NIcon, null, {default: () => h(iconNode)});
    } else {
        return undefined;
    }
};