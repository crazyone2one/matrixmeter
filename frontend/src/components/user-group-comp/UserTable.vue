<script setup lang="ts">
import {computed, h, inject, onMounted, ref, watch, watchEffect} from "vue";
import {CurrentUserGroupItem, UserTableItem} from "/@/api/interface/setting/user-group.ts";
import {AuthScopeEnum} from "/@/enums/commonEnum";
import {useAppStore} from "/@/store";
import {DataTableColumns} from "naive-ui";
import {useI18n} from "/@/hooks/use-i18n.ts";
import {TableQueryParams} from "/@/api/interface/common.ts";
import {usePagination, useRequest} from "alova/client";
import {
  deleteOrgUserFromUserGroup,
  deleteUserFromUserGroup,
  postOrgUserByUserGroup,
  postUserByUserGroup
} from "/@/api/modules/setting/user-group.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";
import {prettyLog} from "/@/utils/log.ts";
import RemoveButton from '/@/components/remove-button/index.vue'

const systemType = inject<AuthScopeEnum>("systemType");
const appStore = useAppStore();
const currentOrgId = computed(() => appStore.state.currentOrgId);
const currentTempRoleId = computed(() => props.current.id);
const {t} = useI18n()
const log = prettyLog()
const props = defineProps<{
  keyword: string;
  current: CurrentUserGroupItem;
  deletePermission?: string[];
  readPermission?: string[];
  updatePermission?: string[];
}>();
const tableQueryParams = ref<TableQueryParams>({
  keyword: props.keyword
});
const columns: DataTableColumns<UserTableItem> = [
  {
    title: t('system.userGroup.name'),
    key: 'name',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: t('system.userGroup.email'),
    key: 'email',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: t('system.userGroup.phone'),
    key: 'phone', ellipsis: {
      tooltip: true
    }
  },
  {
    title: t('system.userGroup.operation'),
    key: 'operation',
    fixed: "right", width: 100,
    render(record) {
      if (hasAnyPermission(props.updatePermission || [])) {
        return h(RemoveButton, {
          disabled: systemType === AuthScopeEnum.SYSTEM && record.userId === 'admin',
          title: t('system.userGroup.removeName', {name: record.name}),
          onOk: () => handleRemove(record)
        }, {})
      }
    }
  }
]
const {
  loading,
  data,
  page,
  pageSize,
  send
} = usePagination(
    // Method实例获取函数，它将接收page和pageSize，并返回一个Method实例
    (page, pageSize) => systemType === AuthScopeEnum.SYSTEM ? postUserByUserGroup(page, pageSize, tableQueryParams.value) : postOrgUserByUserGroup(page, pageSize, tableQueryParams.value),
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
      immediate: false
    }
);
const handlePermission = (permission: string[], cb: () => void) => {
  if (!hasAnyPermission(permission)) {
    log.error('没有权限')
    return false;
  }
  cb();
};
const fetchData = async () => {
  handlePermission(props.readPermission || [], () => {
    tableQueryParams.value.keyword = props.keyword
    // debugger
    send();
  });
}
const {send: deleteUgUser} = useRequest((id) => deleteUserFromUserGroup(id), {immediate: false})
const {send: deleteUgOrgUser} = useRequest((param) => deleteOrgUserFromUserGroup(param), {immediate: false})
const handleRemove = async (record: UserTableItem) => {
  handlePermission(props.updatePermission || [], async () => {
    if (systemType === AuthScopeEnum.SYSTEM) {
      await deleteUgUser(record.id);
    } else if (systemType === AuthScopeEnum.ORGANIZATION) {
      await deleteOrgUserFromUserGroup({
        organizationId: currentOrgId.value,
        userRoleId: props.current.id,
        userIds: [record.id],
      });
    }
    await fetchData();
  })
}
// watchEffect(() => {
//   if (currentTempRoleId.value !== '' && currentOrgId.value) {
//     if (systemType === AuthScopeEnum.SYSTEM) {
//       tableQueryParams.value.roleId = props.current.id
//     } else if (systemType === AuthScopeEnum.ORGANIZATION) {
//       tableQueryParams.value.userRoleId = props.current.id
//       tableQueryParams.value.organizationId = currentOrgId.value
//     }
//     log.warning(props.current.id)
//     fetchData();
//   }
// });
watch(() => currentTempRoleId.value, (roleId) => {
  if (systemType === AuthScopeEnum.SYSTEM) {
    tableQueryParams.value.roleId = roleId
  } else if (systemType === AuthScopeEnum.ORGANIZATION) {
    tableQueryParams.value.userRoleId = roleId
    tableQueryParams.value.organizationId = currentOrgId.value
  }
  fetchData();
})

onMounted(() => {
  if (currentTempRoleId.value !== '' && currentOrgId.value) {
    if (systemType === AuthScopeEnum.SYSTEM) {
      tableQueryParams.value.roleId = props.current.id
    } else if (systemType === AuthScopeEnum.ORGANIZATION) {
      tableQueryParams.value.userRoleId = props.current.id
      tableQueryParams.value.organizationId = currentOrgId.value
    }
    fetchData();
  }
})
defineExpose({
  fetchData,
});
</script>

<template>
  <n-spin :show="loading">
    <n-data-table
        :columns="columns"
        :data="data"
        :row-key="(row: UserTableItem) => row.id"
    />
  </n-spin>
</template>

<style scoped></style>
