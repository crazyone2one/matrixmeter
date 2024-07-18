<script setup lang="ts">
import {computed, h, onMounted, reactive, ref, watchEffect} from "vue";
import {CreateOrUpdateSystemProjectParams} from "/@/api/interface/setting/system/org-project.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";
import {DataTableColumns, DataTableRowKey, NButton} from "naive-ui";
import {OrgProjectTableItem} from "/@/api/interface/setting/org-project.ts";
import BaseButton from "/@/components/base-button/index.vue";
import {useI18n} from "/@/hooks/use-i18n.ts";
import {usePagination, useRequest} from "alova/client";
import {
  deleteProject,
  enableOrDisableProject,
  postProjectTable
} from "/@/api/modules/setting/organizationAndProject.ts";
import {TableQueryParams} from "/@/api/interface/common.ts";
import Pagination from '/@/components/pagination/index.vue'
import UserDrawer from "/@/views/setting/system/org/components/UserDrawer.vue";
import {UserItem} from "/@/api/interface/setting/system/log.ts";
import AddProjectModal from '/@/views/setting/system/org/components/AddProjectModal.vue'
import {ActionsItem} from "/@/components/more-action/types.ts";
import MoreAction from '/@/components/more-action/index.vue'

export interface SystemOrganizationProps {
  keyword: string;
}

const {t} = useI18n()
const props = defineProps<SystemOrganizationProps>();
const userVisible = ref(false);
const addProjectVisible = ref(false);
const currentProjectId = ref('');
const currentUpdateProject = ref<CreateOrUpdateSystemProjectParams>();
const hasOperationPermission = computed(() =>
    hasAnyPermission([
      'SYSTEM_ORGANIZATION_PROJECT:READ+RECOVER',
      'SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE',
      'SYSTEM_ORGANIZATION_PROJECT:READ+DELETE',
    ])
);
const operationWidth = computed(() => {
  if (hasOperationPermission.value) {
    return 250;
  }
  if (hasAnyPermission(['PROJECT_BASE_INFO:READ'])) {
    return 100;
  }
  return 50;
});

const tableQueryParams = ref<TableQueryParams>({
  keyword: props.keyword
});
const currentUserDrawer = reactive({
  visible: false,
  projectId: '',
  currentName: '',
});
const checkedRowKeysRef = ref<DataTableRowKey[]>([])
const tableActions: ActionsItem[] = [
  {
    label: 'common.end',
    eventTag: 'end',
    permission: ['SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE'],
  },
  {
    label: 'system.user.delete',
    eventTag: 'delete',
    danger: true,
  },
];
const columns: DataTableColumns<OrgProjectTableItem> = [
  {
    type: 'selection',
  },
  {
    title: t('system.organization.ID'),
    key: 'num',
  },
  {
    title: t('system.organization.name'),
    key: 'name',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: t('system.organization.member'),
    key: 'memberCount',
    render(record) {
      if (hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+ADD_MEMBER', 'SYSTEM_ORGANIZATION_PROJECT:READ'])) {
        return h(NButton, {
          text: true,
          type: 'primary',
          onClick(e) {
            e.preventDefault()
            showUserDrawer(record)
          },
        }, {default: () => record.memberCount})
      }
      return h('span', null, {default: () => record.memberCount})
    }
  },
  {
    title: t('system.organization.status'),
    key: 'enable',
    render(record) {
      if (record.enable) {
        return h('div', {class: "flex flex-row flex-nowrap items-center gap-[2px]"}, {
          default: () => [
            h('div', {class: 'i-matrix:check-circle-fill'}, {}),
            h('span', null, {
              default: () => t('msTable.enable')
            })
          ]
        });
      } else {
        return h('span', null, {
          default: () => t('msTable.disable')
        })
      }
    }
  },
  {
    title: t('common.desc'),
    key: 'description',
    width: 200,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: t('system.organization.subordinateOrg'),
    key: 'organizationName',
    width: 200,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: t('system.organization.creator'),
    key: 'createUser',
    width: 200
  },
  {
    title: t('system.organization.createTime'),
    key: 'createTime',
    width: 180
  },
  {
    title: hasOperationPermission.value ? t('system.organization.operation') : '',
    key: 'operation',
    fixed: 'right',
    width: operationWidth.value,
    render(record) {
      if (!record.enable) {
        const result = []
        if (hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE'])) {
          result.push(
              h(BaseButton, {
                onClick: () => handleEnableOrDisableProject(record)
              }, {default: () => t('common.enable')}),
          )
        }
        if (hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+DELETE'])) {
          result.push(
              h(BaseButton, {onClick: () => handleDelete(record)}, {default: () => t('common.delete')})
          )
        }
        return result
      } else {
        const result = []
        if (hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE'])) {
          result.push(
              h(BaseButton, {
                onClick: () => showAddProjectModal(record)
              }, {default: () => t('common.edit')}),
          )
        }
        if (hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+ADD_MEMBER'])) {
          result.push(h(BaseButton, {}, {default: () => t('system.organization.addMember')}),)
        }
        // if (hasAnyPermission(['PROJECT_BASE_INFO:READ'])) {
        //   result.push(
        //       h(BaseButton, {onClick: () => enterProject(record.id, record.organizationId)}, {default: () => t('system.project.enterProject')}),
        //   )
        // }
        if (hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+DELETE'])) {
          // @select="(value) => handleMoreAction(value, element.id, AuthScopeEnum.SYSTEM)"
          result.push(h(MoreAction, {list: tableActions, onSelect: (value) => handleMoreAction(value, record)}, {}),)
        }
        return result
      }
    }
  },
]
const {
  loading,
  data,
  page,
  pageSize, total,
  send: fetchData
} = usePagination(
    // Method实例获取函数，它将接收page和pageSize，并返回一个Method实例
    (page, pageSize) => postProjectTable(page, pageSize, tableQueryParams.value),
    {
      // 请求前的初始数据（接口返回的数据格式）
      initialData: {
        total: 0,
        data: []
      },
      initialPage: 1, // 初始页码，默认为1
      initialPageSize: 10, // 初始每页数据条数，默认为10
      debounce: 300,
      data: response => response.records,
      total: response => response.totalRow,
    }
);
const handleCheck = (rowKeys: DataTableRowKey[]) => checkedRowKeysRef.value = rowKeys;
const handleSetPage = (param: number) => page.value = param
const handleSetPageSize = (param: number) => pageSize.value = param;
const showUserDrawer = (record: OrgProjectTableItem) => {
  currentUserDrawer.visible = true;
  currentUserDrawer.projectId = record.id;
  currentUserDrawer.currentName = record.name;
};
const handleUserDrawerCancel = () => {
  currentUserDrawer.visible = false;
  currentUserDrawer.projectId = '';
  currentUserDrawer.currentName = '';
};
const showAddProjectModal = (record: any) => {
  // window.$message.info('edit')
  const {id, name, description, enable, adminList, organizationId, moduleIds, resourcePoolList} = record;
  addProjectVisible.value = true;
  currentUpdateProject.value = {
    id,
    name,
    description,
    enable,
    userIds: adminList.map((item: UserItem) => item.id),
    organizationId,
    moduleIds,
    resourcePoolIds: resourcePoolList.map((item: { id: string }) => item.id),
  };
}
const {send: enableOrDisable} = useRequest((id, enable) => enableOrDisableProject(id, enable), {immediate: false})
const {send: deleteProjectApi} = useRequest((id) => deleteProject(id), {immediate: false})
const handleEnableOrDisableProject = (record: any, isEnable = true) => {
  const title = isEnable ? t('system.project.enableTitle') : t('system.project.endTitle');
  const content = isEnable ? t('system.project.enableContent') : t('system.project.endContent');
  const okText = isEnable ? t('common.confirmStart') : t('common.confirmEnd');
  window.$dialog.info({
    title, content,
    positiveText: okText,
    negativeText: t('common.cancel'),
    closable: false,
    onPositiveClick(e) {
      e.preventDefault();
      enableOrDisable(record.id, isEnable).then(() => {
        window.$message.success(isEnable ? t('common.enableSuccess') : t('common.closeSuccess'));
        fetchData();
      }).catch(e => {
        console.log(e)
      })
    },
  })
}
const handleAddProjectModalCancel = (shouldSearch: boolean) => {
  if (shouldSearch) {
    fetchData();
  }
  addProjectVisible.value = false;
}
const handleMoreAction = (item: ActionsItem, record: OrgProjectTableItem) => {
  const {eventTag} = item;
  switch (eventTag) {
    case 'end':
      handleEnableOrDisableProject(record, false);
      break;
    case 'delete':
      // window.$message.info('delete')
      handleDelete(record);
      break;
    default:
      break;
  }
}
const handleDelete = (record: OrgProjectTableItem) => {
  window.$dialog.error({
    title: t('system.project.deleteName', {name: record.name}),
    content: t('system.project.deleteTip'),
    positiveText: t('common.confirmDelete'),
    negativeText: t('common.cancel'),
    closable: false,
    onPositiveClick(e) {
      e.preventDefault();
      deleteProjectApi(record.id).then(() => {
        window.$message.success(t('common.deleteSuccess'));
        fetchData();
      }).catch(e => {
        console.log(e)
      })
    },
  })
}
defineExpose({
  fetchData,
});
onMounted(() => {
  fetchData();
});
watchEffect(() => {
  // keyword.value = props.keyword
  tableQueryParams.value.keyword = props.keyword
})
</script>

<template>
  <n-data-table
      :columns="columns"
      :data="data"
      :row-key="(row: OrgProjectTableItem) => row.id"
      @update:checked-row-keys="handleCheck"
  />
  <pagination :page-size="pageSize" :page="page" :count="total as number"
              @update-page-size="handleSetPageSize"
              @update-page="handleSetPage"/>
  <user-drawer v-bind="currentUserDrawer" @cancel="handleUserDrawerCancel"/>
  <add-project-modal :current-project="currentUpdateProject" :visible="addProjectVisible"
                     @cancel="handleAddProjectModalCancel"/>
</template>

<style scoped>

</style>