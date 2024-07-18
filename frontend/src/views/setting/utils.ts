import {getFirstRouteNameByPermission, hasAnyPermission} from "/@/utils/permission.ts";
import {h} from "vue";
import {NButton} from "naive-ui";
import {useI18n} from "/@/hooks/use-i18n.ts";
import {useAppStore, useUserStore} from "/@/store";
import router from "/@/router";
import {switchUserOrg} from "/@/api/modules/system.ts";
import {switchProject} from "/@/api/modules/project-manage/project.ts";

const {t} = useI18n()
export const showUpdateOrCreateMessage = (isEdit: boolean, id: string, organizationId?: string) => {
    if (isEdit) {
        window.$message.success(t('system.project.updateProjectSuccess'))
    } else if (!hasAnyPermission(['PROJECT_BASE_INFO:READ'])) {
        window.$message.success(t('system.project.createProjectSuccess'));
    } else {

        window.$message.success(() => h('div', {class: 'flex items-center gap-[12px]'}, [
            h('div', t('system.project.createProjectSuccess')),
            h(
                NButton,
                {
                    text: true,
                    disabled: true,
                    onClick() {
                        enterProject(id, organizationId);
                    },
                },
                {default: () => t('system.project.enterProject')}
            ),
        ]), {duration: 6000})
    }
}
export const enterProject = async (projectId: string, organizationId?: string) => {
    let appStore = useAppStore();
    let userStore = useUserStore()
    if (organizationId && appStore.state.currentOrgId !== organizationId) {
        await switchUserOrg(organizationId, userStore.id || '');
        // console.log('organizationId', organizationId, userStore.id || '', appStore.state.currentOrgId)
    }
    await switchProject({
        projectId,
        userId: userStore.id || '',
    });
    // console.log('projectId', projectId, userStore.id || '')
    // 跳转到项目页面
    router.replace({
        name: getFirstRouteNameByPermission(router.getRoutes()),
        query: {
            orgId: appStore.state.currentOrgId,
            pId: projectId,
        },
    });
}