<script setup lang="ts">
import BaseCard from '/@/components/base-card/index.vue'
import {useI18n} from "/@/hooks/use-i18n.ts";
import {h, onBeforeMount, ref} from "vue";
import {TableQueryParams} from "/@/api/interface/common.ts";
import {DataTableColumns} from "naive-ui";
import {UserListItem} from "/@/api/interface/setting/user.ts";
import {usePagination} from "alova/client";
import {getUserList} from "/@/api/modules/setting/user.ts";

const {t} = useI18n()
const keyword = ref('');
// 表格请求参数集合
const tableQueryParams = ref<TableQueryParams>({
  keyword: keyword.value
});
const columns: DataTableColumns<UserListItem> = [
  {
    type: 'selection',
  }, {
    title: t('system.user.userName'),
    key: 'email',
  },
  {
    title: t('system.user.tableColumnName'),
    key: 'name',
  },
  {
    title: t('system.user.tableColumnEmail'),
    key: 'email',
  },
  {
    title: t('system.user.tableColumnPhone'),
    key: 'phone',
  },
  // {
  //   title: t('system.user.tableColumnOrg'),
  //   key: 'organizationList',
  // },
  {
    title: t('system.user.tableColumnUserGroup'),
    key: 'userRoleList',
    render(record) {
      if (!record.selectUserGroupVisible) {
        return h('span', null, {default: () => 'xxxTag'})
      } else {
        return h('span', null, {default: () => 'xxxSelect'})
      }
    }
  },
  {
    title: t('system.user.tableColumnStatus'),
    key: 'enable',
  },
  {
    title: t('system.user.tableColumnActions'),
    key: 'operation',
    fixed: 'right'
  },
]
const {data, page, pageSize, send: fetchData} = usePagination(
    // Method实例获取函数，它将接收page和pageSize，并返回一个Method实例
    (page, pageSize) => getUserList(page, pageSize, tableQueryParams.value),
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
const handleSearch = (withKeyword = false) => {
  tableQueryParams.value.keyword = withKeyword ? keyword.value : ""
  fetchData()
}
onBeforeMount(() => {
  fetchData()
})
</script>
<template>
  <base-card simple>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <n-button v-permission.all="['SYSTEM_USER:READ+ADD', 'SYSTEM_USER_ROLE:READ']" type="primary" class="mr-3">
          {{ t('system.user.createUser') }}
        </n-button>
        <n-button v-permission.all="['SYSTEM_USER:READ+IMPORT', 'SYSTEM_USER_ROLE:READ']" type="primary" class="mr-3">
          {{ t('system.user.importUser') }}
        </n-button>
      </div>
      <div>
        <n-input v-model:value="keyword" :placeholder="t('system.user.searchUser')" class="w-[230px]" clearable
                 @keyup.enter.prevent="handleSearch(true)" @clear="handleSearch(false)"/>
      </div>
    </div>
    <n-data-table
        :columns="columns"
        :data="data"
        :row-key="(row: UserListItem) => row.id"
    />
  </base-card>
</template>


<style scoped></style>
