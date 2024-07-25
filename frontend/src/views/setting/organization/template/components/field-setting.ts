import {computed, ref} from "vue";
import {TemplateCardEnum} from "/@/enums/templateEnum.ts";
import {useI18n} from "/@/hooks/use-i18n.ts";
import useTemplateStore from "/@/store/modules/setting/template.ts";

const {t} = useI18n();
const templateStore = useTemplateStore();
const organizationState = computed(() => templateStore.ordStatus);
const projectState = computed(() => templateStore.projectStatus);
export const getCardList = (type: string) => {
    const dataList = ref([
        {
            id: 1001,
            key: 'FUNCTIONAL',
            value: TemplateCardEnum.FUNCTIONAL,
            name: t('system.orgTemplate.caseTemplates'),
        },
        /* {
          id: 1002,
          key: 'API',
          value: TemplateCardEnum.API,
          name: t('system.orgTemplate.APITemplates'),
        }, */
        // {
        //   id: 1003,
        //   key: 'UI',
        //   value: TemplateCardEnum.UI,
        //   name: t('system.orgTemplate.UITemplates'),
        // },
        // {
        //   id: 1004,
        //   key: 'TEST_PLAN',
        //   value: TemplateCardEnum.TEST_PLAN,
        //   name: t('system.orgTemplate.testPlanTemplates'),
        // },
        {
            id: 1005,
            key: 'BUG',
            value: TemplateCardEnum.BUG,
            name: t('system.orgTemplate.defectTemplates'),
        },
    ]);
    if (type === 'organization') {
        return dataList.value.map((item) => {
            return {
                ...item,
                enable: organizationState.value[item.key],
            };
        });
    }
    return dataList.value.map((item) => {
        return {
            ...item,
            enable: projectState.value[item.key],
        };
    });
}