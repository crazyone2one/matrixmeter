<script setup lang="ts">
import { useRequest } from "alova/client";
import { computed, inject, ref } from "vue";
import {
  CurrentUserGroupItem,
  PopVisible,
  UserGroupItem,
} from "/@/api/interface/setting/user-group.ts";
import { getUserGroupList } from "/@/api/modules/setting/user-group.ts";
import MmIcon from "/@/components/icon/index.vue";
import CreateUserGroupPopup from "/@/components/user-group-comp/CreateOrUpdateUserGroup.vue";
import { AuthScopeEnum } from "/@/enums/commonEnum.ts";
import { useI18n } from "/@/hooks/use-i18n.ts";
import { hasAnyPermission } from "/@/utils/permission.ts";

const { t } = useI18n();
const systemType = inject<AuthScopeEnum>("systemType");
const props = defineProps<{
  addPermission: string[];
  updatePermission: string[];
  isGlobalDisable: boolean;
}>();
const emit = defineEmits<{
  (e: "handleSelect", element: UserGroupItem): void;
  (e: "addUserSuccess", id: string): void;
}>();
const currentId = ref("");
const currentItem = ref<CurrentUserGroupItem>({
  id: "",
  name: "",
  internal: false,
  type: AuthScopeEnum.SYSTEM,
});
const showSystem = computed(() => systemType === AuthScopeEnum.SYSTEM);
const showOrg = computed(
  () =>
    systemType === AuthScopeEnum.SYSTEM ||
    systemType === AuthScopeEnum.ORGANIZATION
);
const showProject = computed(
  () =>
    systemType === AuthScopeEnum.SYSTEM || systemType === AuthScopeEnum.PROJECT
);
// 用户组列表
const userGroupList = ref<UserGroupItem[]>([]);
// 系统用户组Toggle
const systemToggle = ref(true);
// 气泡弹窗
const popVisible = ref<PopVisible>({});
// 系统用户组列表
const systemUserGroupList = computed(() => {
  return userGroupList.value.filter((ele) => ele.type === AuthScopeEnum.SYSTEM);
});
// 组织用户组Toggle
const orgToggle = ref(true);
// 项目用户组Toggle
const projectToggle = ref(true);
// 系统用户创建用户组visible
const systemUserGroupVisible = ref(false);
// 组织用户创建用户组visible
const orgUserGroupVisible = ref(false);
// 项目用户创建用户组visible
const projectUserGroupVisible = ref(false);

const handleCreateUG = (scoped: AuthScopeEnum) => {
  if (scoped === AuthScopeEnum.SYSTEM) {
    systemUserGroupVisible.value = true;
  } else if (scoped === AuthScopeEnum.ORGANIZATION) {
    orgUserGroupVisible.value = true;
  } else if (scoped === AuthScopeEnum.PROJECT) {
    projectUserGroupVisible.value = true;
  }
};
const handleListItemClick = (element: UserGroupItem) => {
  const { id, name, type, internal } = element;
  currentItem.value = { id, name, type, internal };
  currentId.value = id;
  emit("handleSelect", element);
};
const isSystemShowAll = computed(() => {
  return hasAnyPermission([
    ...props.updatePermission,
    "SYSTEM_USER_ROLE:READ+DELETE",
  ]);
});
const isOrdShowAll = computed(() => {
  return hasAnyPermission([
    ...props.updatePermission,
    "ORGANIZATION_USER_ROLE:READ+DELETE",
  ]);
});
const isProjectShowAll = computed(() => {
  return hasAnyPermission([
    ...props.updatePermission,
    "PROJECT_GROUP:READ+DELETE",
  ]);
});
const systemMoreAction = [
  {
    label: "system.userGroup.rename",
    danger: false,
    eventTag: "rename",
    permission: props.updatePermission,
  },
  {
    isDivider: true,
  },
  {
    label: "system.userGroup.delete",
    danger: true,
    eventTag: "delete",
    permission: ["SYSTEM_USER_ROLE:READ+DELETE"],
  },
];
const handleAddMember = () => {};
const { send } = useRequest(() => getUserGroupList(), { immediate: false });
const initData = async (id?: string, isSelect = true) => {
  let res: UserGroupItem[] = [];
  if (
    systemType === AuthScopeEnum.SYSTEM &&
    hasAnyPermission(["SYSTEM_USER_ROLE:READ"])
  ) {
    res = await send();
  }
  if (res.length > 0) {
    userGroupList.value = res;
    if (isSelect) {
      if (id) {
        const item = res.find((i) => i.id === id);
        if (item) {
          handleListItemClick(item);
        } else {
          window.$message.warning(t("common.resourceDeleted"));
          handleListItemClick(res[0]);
        }
      } else {
        handleListItemClick(res[0]);
      }
    }
    // 弹窗赋值
    const tmpObj: PopVisible = {};
    res.forEach((element) => {
      tmpObj[element.id] = {
        visible: false,
        authScope: element.type,
        defaultName: "",
        id: element.id,
      };
    });
    popVisible.value = tmpObj;
  }
};
defineExpose({
  initData,
});
</script>

<template>
  <div class="flex flex-col px-[16px] pb-[16px]">
    <div class="sticky top-0 z-[999] pb-[8px] pt-[16px]">
      <n-input :placeholder="$t('system.userGroup.searchHolder')" />
    </div>
    <div
      v-if="showSystem"
      v-permission="['SYSTEM_USER_ROLE:READ']"
      class="mt-2"
    >
      <div class="flex items-center justify-between px-[4px] py-[7px]">
        <div class="flex flex-row items-center gap-1">
          <mm-icon
            v-if="systemToggle"
            class="cursor-pointer"
            :size="20"
            @click="systemToggle = false"
            type="expand-down"
          />
          <mm-icon
            v-else
            class="cursor-pointer"
            :size="20"
            @click="systemToggle = true"
            type="expand-right"
          />
          <div class="text-[14px]">
            {{ $t("system.userGroup.systemUserGroup") }}
          </div>
        </div>
        <create-user-group-popup
          :visible="systemUserGroupVisible"
          :list="systemUserGroupList"
          :auth-scope="AuthScopeEnum.SYSTEM"
          @cancel="systemUserGroupVisible = false"
        >
          <n-tooltip trigger="hover" placement="right">
            <template #trigger>
              <mm-icon
                v-permission="props.addPermission"
                :size="20"
                type="expand-right"
                class="cursor-pointer"
                @click="handleCreateUG(AuthScopeEnum.SYSTEM)"
              />
            </template>
            {{ `创建${$t("system.userGroup.systemUserGroup")}` }}
          </n-tooltip>
        </create-user-group-popup>
      </div>
      <transition>
        <div v-if="systemToggle">
          <div
            v-for="element in systemUserGroupList"
            :key="element.id"
            class="list-item"
            :class="{ 'bg-lime-200': element.id === currentId }"
            @click="handleListItemClick(element)"
          >
            <create-user-group-popup
              :list="systemUserGroupList"
              :auth-scope="popVisible[element.id].authScope"
              :visible="popVisible[element.id].visible"
            >
              <div
                class="flex max-w-[100%] grow flex-row items-center justify-between"
              >
                {{ element.name }}
                <div
                  v-if="
                    element.type === systemType ||
                    (isSystemShowAll &&
                      !element.internal &&
                      (element.scopeId !== 'global' || !isGlobalDisable) &&
                      systemMoreAction.length > 0)
                  "
                  class="list-item-action flex flex-row items-center gap-[8px] opacity-0"
                  :class="{ '!opacity-100': element.id === currentId }"
                >
                  <div v-if="element.type === systemType" class="icon-button">
                    <mm-icon
                      v-permission="props.updatePermission"
                      type="plus-circle"
                      size="16"
                      @click="handleAddMember"
                    />
                  </div>
                </div>
              </div>
            </create-user-group-popup>
          </div>
          <n-divider />
        </div>
      </transition>
    </div>
    <div
      v-if="showOrg"
      v-permission="['ORGANIZATION_USER_ROLE:READ']"
      class="mt-2"
    >
      <div class="flex items-center justify-between px-[4px] py-[7px]">
        <div class="flex flex-row items-center gap-1">
          <mm-icon
            v-if="orgToggle"
            class="cursor-pointer"
            :size="20"
            @click="orgToggle = false"
            type="expand-down"
          />
          <mm-icon
            v-else
            class="cursor-pointer"
            :size="20"
            @click="orgToggle = true"
            type="expand-right"
          />
          <div class="text-[14px]">
            {{ $t("system.userGroup.orgUserGroup") }}
          </div>
        </div>
      </div>
    </div>
    <div v-if="showProject" v-permission="['PROJECT_GROUP:READ']" class="mt-2">
      <div class="flex items-center justify-between px-[4px] py-[7px]">
        <div class="flex flex-row items-center gap-1">
          <mm-icon
            v-if="projectToggle"
            class="cursor-pointer"
            :size="20"
            @click="projectToggle = false"
            type="expand-down"
          />
          <mm-icon
            v-else
            class="cursor-pointer"
            :size="20"
            @click="projectToggle = true"
            type="expand-right"
          />
          <div class="text-[14px]">
            {{ $t("system.userGroup.projectUserGroup") }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.list-item {
  padding: 7px 4px 7px 10px;
  height: 15px;

  @apply flex cursor-pointer items-center;
  &:hover .list-item-action {
    opacity: 1;
  }
}
</style>
