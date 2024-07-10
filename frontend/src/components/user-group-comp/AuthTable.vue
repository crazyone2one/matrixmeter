<script setup lang="ts">
import {useRequest} from "alova/client";
import type {DataTableColumns} from "naive-ui";
import {NCheckbox, NCheckboxGroup} from "naive-ui";
import {computed, h, inject, ref, watchEffect} from "vue";
import {
  AuthScopeType,
  AuthTableItem,
  CurrentUserGroupItem,
  SavePermissions,
  UserGroupAuthSetting,
} from "/@/api/interface/setting/user-group.ts";
import {getGlobalUSetting} from "/@/api/modules/setting/user-group.ts";
import {AuthScopeEnum} from "/@/enums/commonEnum.ts";
import {useI18n} from "/@/hooks/use-i18n.ts";

const {t} = useI18n();
const systemType = inject<AuthScopeEnum>("systemType");
const props = withDefaults(
    defineProps<{
      current: CurrentUserGroupItem;
      savePermission?: string[];
      showBottom?: boolean;
      disabled?: boolean;
      scroll?: {
        x?: number | string;
        y?: number | string;
        minWidth?: number | string;
        maxHeight?: number | string;
      };
    }>(),
    {
      showBottom: true,
      disabled: false,
      scroll() {
        return {
          x: "800px",
          y: "100%",
        };
      },
    }
);
const systemAdminDisabled = computed(() => {
  const adminArr = ["admin", "org_admin", "project_admin"];
  const {id} = props.current;
  if (adminArr.includes(id)) {
    // 系统管理员,组织管理员，项目管理员都不可编辑
    return true;
  }

  return props.disabled;
});
const tableData = ref<AuthTableItem[]>();
const handleCellAuthChange = (
    values: (string | number | boolean)[],
    rowIndex: number,
    record: AuthTableItem,
    e: Event
) => {
  setAutoRead(record, (e as unknown as { actionType: string, value: string }).value);
  if (!tableData.value) return;
  const tmpArr = tableData.value;
  const length = tmpArr[rowIndex].permissions?.length || 0;
  if (record.perChecked?.length === length) {
    tmpArr[rowIndex].enable = true;
    tmpArr[rowIndex].indeterminate = false;
  } else if (record.perChecked?.length === 0) {
    tmpArr[rowIndex].enable = false;
    tmpArr[rowIndex].indeterminate = false;
  } else {
    tmpArr[rowIndex].enable = false;
    tmpArr[rowIndex].indeterminate = true;
  }
  handleAllChange();
};
const setAutoRead = (record: AuthTableItem, currentValue: string) => {
  if (!record.perChecked?.includes(currentValue)) {
    // 如果当前没有选中则执行自动添加查询权限逻辑
    // 添加权限值
    record.perChecked?.push(currentValue);
    const preStr = currentValue.split(":")[0];
    const postStr = currentValue.split(":")[1];
    const existRead = record.perChecked?.some(
        (item: string) =>
            item.split(":")[0] === preStr && item.split(":")[1] === "READ"
    );
    if (!existRead && postStr !== "READ") {
      record.perChecked?.push(`${preStr}:READ`);
    }
  } else {
    // 删除权限值
    const preStr = currentValue.split(":")[0];
    const postStr = currentValue.split(":")[1];
    if (postStr === "READ") {
      // 当前是查询 那 移除所有相关的
      record.perChecked = record.perChecked.filter(
          (item: string) => !item.includes(preStr)
      );
    } else {
      record.perChecked.splice(record.perChecked.indexOf(currentValue), 1);
    }
  }
};
const handleRowAuthChange = (
    value: boolean | (string | number | boolean)[],
    rowIndex: number
) => {
  if (!tableData.value) return;
  const tmpArr = tableData.value;
  tmpArr[rowIndex].indeterminate = false;
  if (value) {
    tmpArr[rowIndex].enable = true;
    tmpArr[rowIndex].perChecked = tmpArr[rowIndex].permissions?.map(
        (item) => item.id
    );
  } else {
    tmpArr[rowIndex].enable = false;
    tmpArr[rowIndex].perChecked = [];
  }
  tableData.value = [...tmpArr];
  handleAllChange();
  if (!canSave.value) canSave.value = true;
};
const columns: DataTableColumns<AuthTableItem> = [
  {
    title: t("system.userGroup.function"),
    key: "ability",
    width: 100,
  },
  {
    title: t("system.userGroup.operationObject"),
    key: "operationObject",
    width: 150,
  },
  {
    title: t("system.userGroup.auth"),
    key: "permissions",
    render(record, rowIndex) {
      return h(
          "div",
          {class: "flex flex-row items-center justify-between"},
          {
            default: () => {
              const result = [
                h(
                    NCheckboxGroup,
                    {
                      value: record.perChecked as string[],
                      onUpdateValue: (v, e) => handleCellAuthChange(v, rowIndex, record, e),
                    },
                    {
                      default: () => {
                        const tmp: any[] = [];
                        if (record.permissions) {
                          record.permissions.forEach((item) => {
                            // console.log('item',item.id,item.name)
                            tmp.push(h(
                                NCheckbox,
                                {
                                  value: item.id,
                                  disabled:
                                      systemAdminDisabled.value || props.disabled,
                                },
                                {default: () => item.name}
                            ));
                          });
                        }
                        return tmp;
                      },
                    }
                ),
              ];
              result.push(
                  h(
                      NCheckbox,
                      {
                        class: "mr-[7px]",
                        checked: record.enable,
                        indeterminate: record.indeterminate,
                        disabled: systemAdminDisabled.value || props.disabled,
                        "onUpdate:checked": (value) =>
                            handleRowAuthChange(value, rowIndex),
                      },
                      {}
                  )
              );
              return result;
            },
          }
      );
    },
  },
];
const systemSpan = ref(1);
const projectSpan = ref(1);
const organizationSpan = ref(1);
const workstationSpan = ref(1);
const testPlanSpan = ref(1);
const bugManagementSpan = ref(1);
const caseManagementSpan = ref(1);
const uiTestSpan = ref(1);
const apiTestSpan = ref(1);
const loadTestSpan = ref(1);
const personalSpan = ref(1);
// 表格的总全选
const allChecked = ref(false);
const allIndeterminate = ref(false);
// 是否可以保存
const canSave = ref(false);
const {send} = useRequest((id) => getGlobalUSetting(id), {
  immediate: false,
  force: true,
});
const initData = async (id: string) => {
  tableData.value = []; // 重置数据，可以使表格滚动条重新计算
  let res: UserGroupAuthSetting[] = [];
  if (systemType === AuthScopeEnum.SYSTEM) {
    res = await send(id);
  }
  tableData.value = transformData(res);
  // console.log("tableData.value", tableData.value);
  handleAllChange(true);
};
const transformData = (data: UserGroupAuthSetting[]) => {
  const result: AuthTableItem[] = [];
  data.forEach((item) => {
    if (item.id === "SYSTEM") {
      systemSpan.value = item.children?.length || 0;
    } else if (item.id === "PROJECT") {
      projectSpan.value = item.children?.length || 0;
    } else if (item.id === "ORGANIZATION") {
      organizationSpan.value = item.children?.length || 0;
    } else if (item.id === "WORKSTATION") {
      workstationSpan.value = item.children?.length || 0;
    } else if (item.id === "TEST_PLAN") {
      testPlanSpan.value = item.children?.length || 0;
    } else if (item.id === "BUG_MANAGEMENT") {
      bugManagementSpan.value = item.children?.length || 0;
    } else if (item.id === "CASE_MANAGEMENT") {
      caseManagementSpan.value = item.children?.length || 0;
    } else if (item.id === "UI_TEST") {
      uiTestSpan.value = item.children?.length || 0;
    } else if (item.id === "API_TEST") {
      apiTestSpan.value = item.children?.length || 0;
    } else if (item.id === "LOAD_TEST") {
      loadTestSpan.value = item.children?.length || 0;
    } else if (item.id === "PERSONAL") {
      personalSpan.value = item.children?.length || 0;
    }
    result.push(...makeData(item, item.id));
  });
  return result;
};
const makeData = (item: UserGroupAuthSetting, type: AuthScopeType) => {
  // debugger
  const result: AuthTableItem[] = [];
  item.children?.forEach((child, index) => {
    if (!child.license) {
      const perChecked =
          child?.permissions?.reduce((acc: string[], cur) => {
            if (cur.enable) {
              acc.push(cur.id);
            }
            return acc;
          }, []) || [];
      const perCheckedLength = perChecked.length;
      let indeterminate = false;
      if (child?.permissions) {
        indeterminate =
            perCheckedLength > 0 && perCheckedLength < child?.permissions?.length;
      }
      result.push({
        id: child?.id,
        license: child?.license,
        enable: child?.enable,
        permissions: child?.permissions,
        indeterminate,
        perChecked,
        ability: index === 0 ? item.name : undefined,
        operationObject: t(child.name),
        isSystem: index === 0 && type === "SYSTEM",
        isOrganization: index === 0 && type === "ORGANIZATION",
        isProject: index === 0 && type === "PROJECT",
        isWorkstation: index === 0 && type === "WORKSTATION",
        isTestPlan: index === 0 && type === "TEST_PLAN",
        isBugManagement: index === 0 && type === "BUG_MANAGEMENT",
        isCaseManagement: index === 0 && type === "CASE_MANAGEMENT",
        isUiTest: index === 0 && type === "UI_TEST",
        isLoadTest: index === 0 && type === "LOAD_TEST",
        isApiTest: index === 0 && type === "API_TEST",
        isPersonal: index === 0 && type === "PERSONAL",
      });
    }
  });
  return result;
};
const handleAllChange = (isInit = false) => {
  if (!tableData.value) return;
  const tmpArr = tableData.value;
  const {length: allLength} = tmpArr;
  const {length} = tmpArr.filter((item) => item.enable);
  if (length === allLength) {
    allChecked.value = true;
    allIndeterminate.value = false;
  } else if (length === 0) {
    allChecked.value = false;
    allIndeterminate.value = false;
  } else {
    allChecked.value = false;
    allIndeterminate.value = true;
  }
  if (!isInit && !canSave.value) canSave.value = true;
};
watchEffect(() => {
  if (props.current.id) {
    initData(props.current.id);
  }
});
const handleReset = () => {
  if (props.current.id) {
    initData(props.current.id);
  }
};
const handleSave = () => {
  if (!tableData.value) return;
  const permissions: SavePermissions[] = [];

  const tmpArr = tableData.value;
  tmpArr.forEach((item) => {
    item.permissions?.forEach((ele) => {
      ele.enable = item.perChecked?.includes(ele.id) || false;
      permissions.push({
        id: ele.id,
        enable: ele.enable,
      });
    });
  });
  console.log(permissions);
};
defineExpose({
  canSave,
  handleSave,
  handleReset,
});
</script>

<template>
  <div class="flex h-full flex-col gap-[16px] overflow-hidden">
    <n-data-table :data="tableData" :columns="columns"/>
    <div
        v-if="props.showBottom"
        v-permission="props.savePermission || []"
        class="footer"
    >
      <n-button :disabled="!canSave" @click="handleReset"
      >{{ t("system.userGroup.reset") }}
      </n-button>
      <n-button
          v-permission="props.savePermission || []"
          :disabled="!canSave"
          type="primary"
          @click="handleSave"
      >
        {{ t("system.userGroup.save") }}
      </n-button>
    </div>
  </div>
</template>

<style scoped>
.footer {
  display: flex;
  justify-content: flex-end;
  padding: 24px;
  background-color: #ffffff;
  box-shadow: 0 -1px 4px rgb(2 2 2 / 10%);
  gap: 16px;
}
</style>
