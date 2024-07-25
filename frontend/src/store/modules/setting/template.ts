import {defineStore} from "pinia";
import {computed} from "vue";
import useAppStore from '../app';
import {hasAnyPermission} from "/@/utils/permission.ts";
import {getOrdTemplate, getProTemplate} from "/@/api/modules/setting/template.ts";

const appStore = useAppStore();
const useTemplateStore = defineStore('template', {
    persist: true,
    state: (): {
        ordStatus: Record<string, boolean>;
        projectStatus: Record<string, boolean>;
    } => ({
        ordStatus: {
            FUNCTIONAL: false,
            API: false,
            UI: false,
            TEST_PLAN: false,
            BUG: false,
        },
        projectStatus: {
            FUNCTIONAL: false,
            API: false,
            UI: false,
            TEST_PLAN: false,
            BUG: false,
        },
    }),
    actions: {
        // 模板列表的状态
        async getStatus() {
            const currentOrgId = computed(() => appStore.state.currentOrgId);
            const currentProjectId = computed(() => appStore.state.currentProjectId);
            try {
                if (currentOrgId.value && hasAnyPermission(['ORGANIZATION_TEMPLATE:READ'])) {
                    this.ordStatus = await getOrdTemplate(currentOrgId.value);
                }
                if (
                    currentProjectId.value &&
                    hasAnyPermission(['PROJECT_TEMPLATE:READ']) &&
                    currentProjectId.value !== 'no_such_project'
                ) {
                    this.projectStatus = await getProTemplate(currentProjectId.value);
                }
            } catch (error) {
                console.log(error);
            }
        },
    },
})
export default useTemplateStore;