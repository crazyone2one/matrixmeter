<script setup lang="ts">
import useTemplateStore from "/@/store/modules/setting/template.ts";
import {useI18n} from "/@/hooks/use-i18n.ts";
import {useRouter} from "vue-router";
import {onBeforeMount, ref} from "vue";
import {SettingRouteEnum} from "/@/enums/routeEnum.ts";
import {getCardList} from "/@/views/setting/organization/template/components/field-setting.ts";
import CardList from '/@/components/card-list/index.vue'
import TemplateItem from "/@/views/setting/organization/template/components/TemplateItem.vue";

const templateStore = useTemplateStore();
const {t} = useI18n();
const router = useRouter();
// 字段设置
const fieldSetting = (key: string) => {
  router.push({
    name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_FILED_SETTING,
    query: {
      type: key,
    },
  });
};

// 模板管理
const templateManagement = (key: string) => {
  router.push({
    name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT,
    query: {
      type: key,
    },
  });
};

// 工作流
const workflowSetup = (key: string) => {
  router.push({
    name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_WORKFLOW,
    query: {
      type: key,
    },
  });
};

const cardList = ref<Record<string, any>[]>([]);
const updateState = () => {
  cardList.value = [...getCardList('organization')];
};
onBeforeMount(() => {
  templateStore.getStatus();
});
onBeforeMount(() => {
  // doCheckIsTip();
  updateState();
});
</script>

<template>
  <div>
    <card-list mode="static"
               :card-min-width="360"
               class="flex-1"
               :shadow-limit="50"
               :list="cardList"
               :is-proportional="false"
               :gap="16"
               padding-bottom-space="16px">
      <template #item="{ item, index }">
        <template-item :card-item="item"
                       :index="index"
                       mode="organization"
                       @field-setting="fieldSetting"/>
      </template>
    </card-list>
  </div>
</template>

<style scoped>

</style>