<script setup lang="ts">

import {computed, h, onMounted, ref} from "vue";
import {OrgProjectTableItem} from "/@/api/interface/setting/org-project.ts";
import {DataTableColumns, DataTableRowKey, NButton} from "naive-ui";
import {useI18n} from "/@/hooks/use-i18n.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";
import {usePagination} from "alova/client";
import {postOrgTable} from "/@/api/modules/setting/organizationAndProject.ts";
import {TableQueryParams} from "/@/api/interface/common.ts";
import BaseButton from '/@/components/base-button/index.vue'

const {t} = useI18n()
const keyword = defineModel<string>("keyword")
// 表格请求参数集合
const tableQueryParams = ref<TableQueryParams>({
  keyword: keyword.value
});
const hasOperationPermission = computed(() =>
    hasAnyPermission([
      'SYSTEM_ORGANIZATION_PROJECT:READ+RECOVER',
      'SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE',
      'SYSTEM_ORGANIZATION_PROJECT:READ+DELETE',
    ])
);
const columns: DataTableColumns<OrgProjectTableItem> = [
  {
    type: 'selection',
  },
  {
    title: t('system.organization.ID'),
    key: 'num',
    width: 100
  },
  {
    title: t('system.organization.name'),
    key: 'name',
    width: 300,
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
          onClick: showUserDrawer(record)
        }, {default: () => record.memberCount})
      }
      return h('span', null, {default: () => record.memberCount})
    }
  },
  {
    title: t('system.organization.project'),
    key: 'projectCount',
    render(record) {
      if (hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE'])) {
        return h(NButton, {
          text: true,
          type: 'primary',
          onClick: showProjectDrawer(record)
        }, {default: () => record.projectCount})
      }
      return h('span', null, {default: () => record.projectCount})
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
    width: hasOperationPermission.value ? 230 : 50,
    render(record) {
      if (!record.enable) {
        return [
          h(BaseButton, {}, {default: () => t('common.enable')}),
          h(BaseButton, {}, {default: () => t('common.delete')})
        ]
      } else {
        return [
          h(BaseButton, {}, {default: () => t('common.edit')}),
          h(BaseButton, {}, {default: () => t('system.organization.addMember')}),
          h(BaseButton, {}, {default: () => t('common.end')}),
          h(BaseButton, {}, {default: () => t('system.user.delete')}),
        ]
      }
    }
  },
]
const {
  // 加载状态
  loading,

  // 列表数据
  data,

  // 当前页码，改变此页码将自动触发请求
  page,

  // 每页数据条数
  pageSize,
  send: fetchData
} = usePagination(
    // Method实例获取函数，它将接收page和pageSize，并返回一个Method实例
    (page, pageSize) => postOrgTable(page, pageSize, tableQueryParams.value),
    {
      // 请求前的初始数据（接口返回的数据格式）
      initialData: {
        total: 0,
        data: []
      },
      initialPage: 1, // 初始页码，默认为1
      initialPageSize: 10, // 初始每页数据条数，默认为10
      debounce: 300,
      data: response => response.records
    }
);
// 翻到上一页，page值更改后将自动发送请求
const handlePrevPage = () => {
  page.value--;
};

// 翻到下一页，page值更改后将自动发送请求
const handleNextPage = () => {
  page.value++;
};

// 更改每页数量，pageSize值更改后将自动发送请求
const handleSetPageSize = (param: number) => {
  pageSize.value = param;
};
const checkedRowKeysRef = ref<DataTableRowKey[]>([])
// const fetchData = () => {
//   console.log('SystemOrganization-keyword', keyword.value)
// }
const handleCheck = (rowKeys: DataTableRowKey[]) => checkedRowKeysRef.value = rowKeys;
const showUserDrawer = (record: OrgProjectTableItem) => () => {
  window.$message.info(`showUserDrawer ${record.id}`)
}
const showProjectDrawer = (record: OrgProjectTableItem) => () => {
  window.$message.info(`showProjectDrawer ${record.id}`)
}
defineExpose({
  fetchData, loading
});
onMounted(() => {
  fetchData();
});
</script>

<template>
  <n-data-table
      :columns="columns"
      :data="data"
      :row-key="(row: OrgProjectTableItem) => row.id"
      @update:checked-row-keys="handleCheck"
  />
</template>

<style scoped>

</style>