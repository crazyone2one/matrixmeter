<script setup lang="ts">
import BaseCard from '/@/components/base-card/index.vue'
import {useI18n} from "/@/hooks/use-i18n.ts";
import {h, onBeforeMount, Ref, ref, VNode} from "vue";
import {BatchActionQueryParams, TableQueryParams} from "/@/api/interface/common.ts";
import type {DataTableColumns, DataTableRowKey, FormInst, FormItemRule} from "naive-ui";
import {NButton, NFlex} from "naive-ui";
import {SimpleUserInfo, SystemRole, UserListItem} from "/@/api/interface/setting/user.ts";
import {usePagination, useRequest} from "alova/client";
import {
  batchCreateUser,
  deleteUserInfo,
  getSystemRoles,
  getUserList,
  resetUserPassword,
  toggleUserStatus,
  updateUserInfo
} from "/@/api/modules/setting/user.ts";
import {hasAllPermission, hasAnyPermission} from "/@/utils/permission.ts";
import TagGroup from '/@/components/tag-group/index.vue'
import MoreAction from '/@/components/more-action/index.vue'
import {ActionsItem} from "/@/components/more-action/types.ts";
import Pagination from '/@/components/pagination/index.vue'
import {cloneDeep} from "lodash-es";
import BatchForm from '/@/components/batch-form/index.vue'
import {FormItemModel} from "/@/components/batch-form/types.ts";
import {validateEmail, validatePhone} from "/@/utils/validate.ts";
import UploadModal from '/@/views/setting/system/user/components/upload-modal/index.vue'

type UserModalMode = 'create' | 'edit';

interface UserForm {
  list: SimpleUserInfo[];
  userGroup: string[];
}

const {t} = useI18n()
const keyword = ref('');
const checkedRowKeys = ref<DataTableRowKey[]>([])
// 表格请求参数集合
const tableQueryParams = ref<TableQueryParams>({
  keyword: keyword.value
});
const userFormMode = ref<UserModalMode>('create');
const visible = ref(false);
const importVisible = ref(false);
const isBatchFormChange = ref(false);
const userFormRef = ref<FormInst | null>(null)
const batchFormRef = ref<InstanceType<typeof BatchForm> | null>(null)
const UploadModalRef = ref<InstanceType<typeof UploadModal>>()
const batchFormModels: Ref<FormItemModel[]> = ref([
  {
    field: 'name',
    type: 'input',
    label: 'system.user.createUserName',
    rules: [{required: true, message: t('system.user.createUserNameNotNull')}, {
      validator(_rule: FormItemRule, value: string) {
        if (!value) {
          return new Error(t('system.user.createUserNameNotNull'))
        } else if (value.length > 255) {
          return new Error(t('system.user.createUserNameOverLength'))
        }
        return true
      }
    }],
    placeholder: 'system.user.createUserNamePlaceholder',
  },
  {
    field: 'email',
    type: 'input',
    label: 'system.user.createUserEmail',
    rules: [
      {required: true, message: t('system.user.createUserEmailNotNull')},
      {
        validator(_rule: FormItemRule, value: string) {
          if (!value) {
            return new Error(t('system.user.createUserEmailNotNull'))
          } else if (!validateEmail(value)) {
            return new Error(t('system.user.createUserEmailErr'))
          }
          return true
        }
      },
      {notRepeat: true, message: 'system.user.createUserEmailNoRepeat'},
    ],
    placeholder: 'system.user.createUserEmailPlaceholder',
  },
  {
    field: 'phone',
    type: 'input',
    label: 'system.user.createUserPhone',
    rules: [{
      validator(_rule: FormItemRule, value: string) {
        if (!value && !validatePhone(value)) {
          return new Error(t('system.user.createUserPhoneErr'))
        }
        return true
      }
    }],
    placeholder: 'system.user.createUserPhonePlaceholder',
  },
]);
const defaultUserForm = {
  list: [
    {
      name: '',
      email: '',
      phone: '',
    },
  ],
  userGroup: [],
};
const userForm = ref<UserForm>(cloneDeep(defaultUserForm));
const userGroupOptions = ref<SystemRole[]>([]);
const tableActions: ActionsItem[] = [
  {
    label: 'system.user.resetPassword',
    eventTag: 'resetPassword',
    permission: ['SYSTEM_USER:READ+UPDATE'],
  },
  {
    label: 'system.user.disable',
    eventTag: 'disabled',
    permission: ['SYSTEM_USER:READ+UPDATE'],
  },
  {
    isDivider: true,
  },
  {
    label: 'system.user.delete',
    eventTag: 'delete',
    danger: true,
    permission: ['SYSTEM_USER:READ+DELETE'],
  },
];
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
        return h(TagGroup, {
          tagList: record.userRoleList, type: 'primary',
          onClick: () => handleTagClick(record)
        }, {})
      } else {
        return h('span', null, {default: () => 'xxxSelect'})
      }
    }
  },
  {
    title: t('system.user.tableColumnStatus'),
    key: 'enable',
    render(record) {
      return h('div', {class: "flex flex-row flex-nowrap items-center gap-[2px]"}, {
        default: () => [
          h('div', {class: record.enable ? 'i-matrix:check-circle-fill' : 'i-mdi-circle-off-outline'}, {}),
          h('span', null, {
            default: () => t(record.enable ? 'msTable.enable' : 'msTable.disable')
          })
        ]
      });
    }
  },
  {
    title: t('system.user.tableColumnActions'),
    key: 'operation',
    fixed: 'right',
    render(record) {
      if (!record.enable) {
        const tmp: VNode[] = [];
        if (hasAnyPermission(['SYSTEM_USER:READ+UPDATE'])) {
          tmp.push(h(NButton, {
            text: true,
            type: 'primary',
            onClick(e) {
              e.preventDefault()
              enableUser(record)
            },
          }, {default: () => t('system.user.enable')}))
        }
        if (hasAnyPermission(['SYSTEM_USER:READ+DELETE'])) {
          tmp.push(h(NButton, {
            text: true, type: 'primary', onClick(e) {
              e.preventDefault()
              deleteUser(record)
            },
          }, {default: () => t('system.user.delete')}))
        }
        return h(NFlex, {}, {default: () => tmp});
      } else {
        const tmp: VNode[] = [];
        if (hasAnyPermission(['SYSTEM_USER:READ+UPDATE'])) {
          tmp.push(h(NButton, {
            text: true, type: 'primary', onClick(e) {
              e.preventDefault()
              showUserModal('edit', record)
            },
          }, {default: () => t('system.user.editUser')}))
        }
        if (hasAnyPermission(['SYSTEM_USER:READ+UPDATE', 'SYSTEM_USER:READ+DELETE'])) {
          tmp.push(h(MoreAction, {
            list: tableActions,
            onSelect: (value: ActionsItem) => handleSelect(value, record)
          }, {}))
        }
        return h(NFlex, {}, {default: () => tmp});
      }
    }
  },
]
const handleSelect = (item: ActionsItem, record: UserListItem) => {
  switch (item.eventTag) {
    case 'resetPassword':
      resetPassword(record);
      break;
    case 'disabled':
      disabledUser(record);
      break;
    case 'delete':
      deleteUser(record);
      break;
    default:
      break;
  }
}
const resetPassword = (record?: UserListItem, isBatch?: boolean, params?: BatchActionQueryParams) => {
  let title = t('system.user.resetPswTip', {name: record?.name});
  let selectIds = [record?.id || ''];
  if (isBatch) {
    title = t('system.user.batchResetPswTip', {count: params?.currentSelectCount || checkedRowKeys.value.length});
    selectIds = checkedRowKeys.value as string[];
  }

  let content = t('system.user.resetPswContent');
  if (record && record.id === 'admin') {
    content = t('system.user.resetAdminPswContent');
  }
  window.$dialog.warning({
    title,
    content,
    positiveText: t('system.user.resetPswConfirm'),
    negativeText: t('system.user.resetPswCancel'),
    maskClosable: false,
    onPositiveClick(e) {
      e.preventDefault()
      resetUserPasswordApi({
        selectIds,
        selectAll: !!params?.selectAll,
        excludeIds: params?.excludeIds || [],
        condition: {keyword: keyword.value},
      }).then(() => {
        window.$message.success(t('system.user.resetPswSuccess'));
      })
    },
  })
}
const {send: toggleUserStatusApi} = useRequest((param) => toggleUserStatus(param), {immediate: false})
const {send: deleteUserInfoApi} = useRequest((param) => deleteUserInfo(param), {immediate: false})
const {send: resetUserPasswordApi} = useRequest((param) => resetUserPassword(param), {immediate: false})
/**
 * 禁用用户
 * @param record
 * @param isBatch
 * @param params
 */
const disabledUser = (record?: UserListItem, isBatch?: boolean, params?: BatchActionQueryParams) => {
  let title = t('system.user.disableUserTip', {name: record?.name});
  let selectIds = [record?.id || ''];
  if (isBatch) {
    title = t('system.user.batchDisableUserTip', {count: params?.currentSelectCount || checkedRowKeys.value.length});
    selectIds = checkedRowKeys.value as string[];
  }
  window.$dialog.warning({
    title,
    content: t('system.user.disableUserContent'),
    positiveText: t('system.user.disableUserConfirm'),
    negativeText: t('system.user.disableUserCancel'),
    maskClosable: false,
    onPositiveClick(e) {
      e.preventDefault();
      const param = {
        selectIds,
        selectAll: !!params?.selectAll,
        excludeIds: params?.excludeIds || [],
        condition: {keyword: keyword.value},
        enable: false,
      }
      toggleUserStatusApi(param).then(() => {
        window.$message.success(t('system.user.disableUserSuccess'));
        fetchData();
      })
    },
  })
}
/**
 * 启用用户
 * @param record
 * @param isBatch
 * @param params
 */
const enableUser = (record?: UserListItem, isBatch?: boolean, params?: BatchActionQueryParams) => {
  let title = t('system.user.enableUserTip', {name: record?.name});
  let selectIds = [record?.id || ''];
  if (isBatch) {
    title = t('system.user.batchEnableUserTip', {count: params?.currentSelectCount || checkedRowKeys.value.length});
    selectIds = checkedRowKeys.value as string[];
  }
  window.$dialog.info({
    title,
    content: t('system.user.enableUserContent'),
    positiveText: t('system.user.enableUserConfirm'),
    negativeText: t('system.user.enableUserCancel'),
    maskClosable: false,
    onPositiveClick(e) {
      e.preventDefault()
      toggleUserStatusApi({
        selectIds,
        selectAll: !!params?.selectAll,
        excludeIds: params?.excludeIds || [],
        condition: {keyword: keyword.value},
        enable: true,
      }).then(() => {
        window.$message.success(t('system.user.enableUserSuccess'));
        fetchData();
      })
    },
  })
}
const deleteUser = (record?: UserListItem, isBatch?: boolean, params?: BatchActionQueryParams) => {
  let title = t('system.user.deleteUserTip', {name: record?.name});
  let selectIds = [record?.id || ''];
  if (isBatch) {
    title = t('system.user.batchDeleteUserTip', {count: params?.currentSelectCount || checkedRowKeys.value.length});
    selectIds = checkedRowKeys.value as string[];
  }
  window.$dialog.error({
    title,
    content: t('system.user.deleteUserContent'),
    positiveText: t('system.user.deleteUserConfirm'),
    negativeText: t('system.user.deleteUserCancel'),
    maskClosable: false,
    onPositiveClick(e) {
      e.preventDefault()
      deleteUserInfoApi({
        selectIds,
        selectAll: !!params?.selectAll,
        excludeIds: params?.excludeIds || [],
        condition: {keyword: keyword.value},
      }).then(() => {
        window.$message.success(t('system.user.deleteUserSuccess'));
        fetchData();
      })
    },
  })
}
const handleTagClick = (record: UserListItem) => {
  if (hasAllPermission(['SYSTEM_USER:READ+UPDATE', 'SYSTEM_USER_ROLE:READ'])) {
    record.selectUserGroupVisible = true;
  }
}
const {data, page, pageSize, total, send: fetchData, onSuccess} = usePagination(
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
      data: response => response.records,
      total: response => response.totalRow
    }
);
onSuccess(() => {
  data.value.map(item => {
    return {
      ...item,
      selectUserGroupVisible: false,
      selectUserGroupLoading: false,
    };
  })
})
const handleSetPage = (param: number) => page.value = param
const handleSetPageSize = (param: number) => {
  pageSize.value = param;
};
const handleSearch = (withKeyword = false) => {
  tableQueryParams.value.keyword = withKeyword ? keyword.value : ""
  fetchData()
}
const showUserModal = (mode: UserModalMode, record?: UserListItem) => {
  visible.value = true;
  userFormMode.value = mode;
  if (mode === 'edit' && record) {
    userForm.value.list = [
      {
        id: record.id,
        name: record.name,
        email: record.email,
        phone: record.phone ? record.phone.replace(/\s/g, '') : record.phone,
      },
    ];
    userForm.value.userGroup = record.userRoleList.map((e) => e.id);
  }
  init()
}
const {send: loadSystemRoles} = useRequest(() => getSystemRoles(), {immediate: false, force: true})
const init = () => {
  if (hasAnyPermission(['SYSTEM_USER_ROLE:READ'])) {
    // if (userGroupOptions.value.length) {
    //   console.log('111')
    //   loadSystemRoles().then(res => {
    //     userGroupOptions.value = res
    //     console.log(res)
    //   })
    //   userForm.value.userGroup = userGroupOptions.value.filter((e: SystemRole) => e.selected);
    //   console.log(userForm.value)
    // }
    loadSystemRoles().then(res => {
      userGroupOptions.value = res
      userForm.value.userGroup = userGroupOptions.value.filter((e: SystemRole) => e.selected)
          .map((e) => e.id);
    })
  }
}

function resetUserForm() {
  userForm.value.list = [];
  batchFormRef.value?.resetForm()
  userForm.value.userGroup = userGroupOptions.value.filter((e: SystemRole) => e.selected).map((e) => e.id);
}

const cancelCreate = () => {
  visible.value = false;
  resetUserForm()
}
const handleBeforeClose = () => {
  if (isBatchFormChange.value) {

  } else {
    cancelCreate();
  }
}
const userFormValidate = (cb: () => Promise<any>) => {
  userFormRef.value?.validate(errors => {
    if (errors) {
      return
    }
    batchFormRef.value?.formValidate(async (list: any) => {
      userForm.value.list = [...list];
      await cb();
    })
  })
}
const createUser = async (isContinue?: boolean) => {
  const params = {
    userInfoList: userForm.value.list,
    userRoleIdList: userForm.value.userGroup,
  };
  const res = await batchCreateUser(params);
  if (res.errorEmails !== null) {
    const errData: Record<string, any> = {};
    Object.keys(res.errorEmails).forEach((key) => {
      const filedIndex = userForm.value.list.findIndex((e) => e.email === key);
      if (filedIndex > -1) {
        errData[`list[${filedIndex}].email`] = {
          status: 'error',
          message: t('system.user.createUserEmailExist'),
        };
      }
    });
    // batchFormRef.value?.setFields(errData);
    console.log(errData)
  } else {
    window.$message.success(t('system.user.addUserSuccess'));
    if (!isContinue) {
      cancelCreate();
    }
    await fetchData();
  }
}
const updateUser = async () => {
  const activeUser = userForm.value.list[0];
  const params = {
    id: activeUser.id as string,
    name: activeUser.name,
    email: activeUser.email,
    phone: activeUser.phone,
    userRoleIdList: userForm.value.userGroup,
  };
  await updateUserInfo(params);
  window.$message.success(t('system.user.updateUserSuccess'));
  visible.value = false;
  await fetchData();
}
const beforeCreateUser = () => {
  if (userFormMode.value === 'create') {
    userFormValidate(createUser);
  } else {
    userFormValidate(updateUser);
  }
}
const showImportModal = () => {
  importVisible.value = true;
}
const handleCancelImport = () => {
  importVisible.value = false
}
onBeforeMount(() => {
  fetchData()
})
</script>
<template>
  <base-card simple>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <n-button v-permission.all="['SYSTEM_USER:READ+ADD', 'SYSTEM_USER_ROLE:READ']" type="primary" class="mr-3"
                  @click="showUserModal('create')">
          {{ t('system.user.createUser') }}
        </n-button>
        <n-button v-permission.all="['SYSTEM_USER:READ+IMPORT', 'SYSTEM_USER_ROLE:READ']" type="primary" ghost
                  class="mr-3"
                  @click="showImportModal">
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
    <pagination :count="total as number" :page-size="pageSize" :page="page"
                @update-page-size="handleSetPageSize"
                @update-page="handleSetPage"/>
  </base-card>
  <n-modal v-model:show="visible" preset="dialog" :mask-closable="false"
           :title="userFormMode === 'create' ? t('system.user.createUserModalTitle') : t('system.user.editUserModalTitle')"
           :closable="false">
    <n-form
        ref="userFormRef"
        :model="userForm"
        label-placement="left"
        label-width="auto"
        require-mark-placement="right-hanging"
    >
      <batch-form ref="batchFormRef"
                  :models="batchFormModels"
                  :form-mode="userFormMode"
                  add-text="system.user.addUser"
                  :default-vals="userForm.list"
                  max-height="250px"/>
      <n-form-item :label="t('system.user.createUserUserGroup')" path="userGroup">
        <n-select v-model:value="userForm.userGroup" multiple :options="userGroupOptions" label-field="name"
                  value-field="id"/>
      </n-form-item>
    </n-form>
    <template #action>
      <n-button secondary @click="handleBeforeClose">{{ t('system.user.editUserModalCancelCreate') }}</n-button>
      <n-button v-if="userFormMode === 'create'" secondary>
        {{ t('system.user.editUserModalSaveAndContinue') }}
      </n-button>
      <n-button type="primary" @click="beforeCreateUser">
        {{ t(userFormMode === 'create' ? 'system.user.editUserModalCreateUser' : 'system.user.editUserModalEditUser') }}
      </n-button>
    </template>
  </n-modal>
  <upload-modal ref="UploadModalRef" :visible="importVisible" accept="excel" :multiple="false"
                @cancel="handleCancelImport" @load-list="fetchData"/>
</template>


<style scoped></style>
