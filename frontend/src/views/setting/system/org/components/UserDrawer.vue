<script setup lang="ts">
import BaseDrawer from '/@/components/base-drawer/index.vue'
import {computed, ref, watch} from "vue";
import {useI18n} from "/@/hooks/use-i18n.ts";
import {DataTableColumns, NButton} from "naive-ui";
import {UserListItem} from "/@/api/interface/setting/user.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";
import {usePagination} from "alova/client";
import Pagination from '/@/components/pagination/index.vue'
import {postUserByOrgIdOrProjectId} from "/@/api/modules/setting/organizationAndProject.ts";
import {SystemGetUserByOrgOrProjectIdParams} from "/@/api/interface/setting/org-project.ts";
import AddUserModal from "/@/views/setting/system/org/components/AddUserModal.vue";

export interface projectDrawerProps {
  visible: boolean;
  organizationId?: string;
  projectId?: string;
  currentName: string;
}

const {t} = useI18n();
const props = defineProps<projectDrawerProps>();
const emit = defineEmits<{
  (e: 'cancel'): void;
  (e: 'requestFetchData'): void;
}>();
const currentVisible = ref(props.visible);
const userVisible = ref(false);
const keyword = ref('');
const handleCancel = () => {
  keyword.value = '';
  emit('cancel');
};
const tableQueryParams = ref<SystemGetUserByOrgOrProjectIdParams>({
  keyword: keyword.value
});
const hasOperationPermission = computed(() =>
    hasAnyPermission([
      'SYSTEM_ORGANIZATION_PROJECT:READ+RECOVER',
      'SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE',
      'SYSTEM_ORGANIZATION_PROJECT:READ+DELETE',
    ])
);
const columns: DataTableColumns<UserListItem> = [
  {
    type: 'selection',
  },
  {
    title: t('system.organization.userName'),
    key: 'name',
    width: 200
  },
  {
    title: t('system.organization.email'),
    key: 'email',
    width: 200
  },
  {
    title: t('system.organization.phone'),
    key: 'phone',
  },
  {
    title: hasOperationPermission.value ? t('system.organization.operation') : '',
    key: 'operation',
    fixed: 'right',
  },
]
const {data, page, pageSize, total, send: loadList} = usePagination(
    // Method实例获取函数，它将接收page和pageSize，并返回一个Method实例
    (page, pageSize) => postUserByOrgIdOrProjectId(page, pageSize, tableQueryParams.value),
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
      immediate: false
    }
);
const handleSetPage = (param: number) => page.value = param
const handleSetPageSize = (param: number) => pageSize.value = param;
const fetchData = () => {
  if (props.projectId) {
    tableQueryParams.value.projectId = props.projectId;
  }
  if (props.organizationId) {
    tableQueryParams.value.organizationId = props.organizationId;
  }
  loadList();
}
const handleAddMember = () => {
  userVisible.value = true;
};
const handleHideUserModal = () => {
  userVisible.value = false;
};
watch(
    () => props.organizationId,
    () => {
      fetchData();
    }
);
watch(
    () => props.projectId,
    () => {
      fetchData();
    }
);
watch(
    () => props.visible,
    (visible) => {
      currentVisible.value = visible;
      if (visible) {
        fetchData();
      }
    }
);
</script>

<template>
  <base-drawer :visible="currentVisible" :width="680"
               :title="t('system.organization.addMemberTitle')"
               :mask="false"
               @cancel="handleCancel"
  >
    <div>
      <div class="flex flex-row justify-between">
        <n-button v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+ADD_MEMBER']" class="mr-3"
                  @click="handleAddMember">
          {{ t('system.organization.addMember') }}
        </n-button>
        <div>
          <n-input :value="keyword" :placeholder="t('system.organization.searchUserPlaceholder')"
                   class="w-[230px]" clearable/>
        </div>
      </div>
      <div class="mt-4">
        <n-data-table
            :columns="columns"
            :data="data"
            :row-key="(row: UserListItem) => row.id"
        />
        <pagination :count="total as number" :page-size="pageSize" :page="page"
                    @update-page-size="handleSetPageSize"
                    @update-page="handleSetPage"/>
      </div>
    </div>
  </base-drawer>
  <add-user-modal :project-id="props.projectId"
                  :organization-id="props.organizationId"
                  :visible="userVisible"/>
</template>

<style scoped>

</style>