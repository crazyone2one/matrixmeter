import { NIcon } from "naive-ui";
import { h } from "vue";
import { RouteRecordName, RouterLink } from "vue-router";
import { useI18n } from "/@/hooks/use-i18n.ts";

const { t } = useI18n();
export const renderRouterLink = (name: RouteRecordName, label?: string) => {
  return h(
    RouterLink,
    { to: { name } },
    { default: () => (label ? t(label) : label) }
  );
};

export const renderIcon = async (icon?: string) => {
  return icon
    ? () =>
        h(NIcon, null, {
          default: () => h("div", { class: "i-matrix:" + icon }),
        })
    : undefined;
};
