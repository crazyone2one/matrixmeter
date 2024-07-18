<script setup lang="ts">
import {useRequest} from "alova/client";
import {computed, inject, ref} from "vue";
import {CurrentUserGroupItem, PopVisible, UserGroupItem,} from "/@/api/interface/setting/user-group.ts";
import {deleteOrgUserGroup, deleteUserGroup, getUserGroupList} from "/@/api/modules/setting/user-group.ts";
import MmIcon from "/@/components/icon/index.vue";
import CreateUserGroupPopup from "/@/components/user-group-comp/CreateOrUpdateUserGroup.vue";
import {AuthScopeEnum} from "/@/enums/commonEnum.ts";
import {useI18n} from "/@/hooks/use-i18n.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";
import AddUserModal from "/@/components/user-group-comp/AddUserModal.vue";
import MoreAction from '/@/components/more-action/index.vue'
import {ActionsItem} from "/@/components/more-action/types.ts";

const {t} = useI18n();
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
// 组织用户组列表
const orgUserGroupList = computed(() => {
  return userGroupList.value.filter((ele) => ele.type === AuthScopeEnum.ORGANIZATION);
});
// 项目用户组列表
const projectUserGroupList = computed(() => {
  return userGroupList.value.filter((ele) => ele.type === AuthScopeEnum.PROJECT);
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
const userModalVisible = ref(false);

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
  const {id, name, type, internal} = element;
  currentItem.value = {id, name, type, internal};
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
const orgMoreAction: ActionsItem[] = [
  {
    label: 'system.userGroup.rename',
    danger: false,
    eventTag: 'rename',
    permission: props.updatePermission,
  },
  {
    isDivider: true,
  },
  {
    label: 'system.userGroup.delete',
    danger: true,
    eventTag: 'delete',
    permission: ['ORGANIZATION_USER_ROLE:READ+DELETE'],
  },
];
const projectMoreAction: ActionsItem[] = [
  {
    label: 'system.userGroup.rename',
    danger: false,
    eventTag: 'rename',
    permission: props.updatePermission,
  },
  {
    isDivider: true,
  },
  {
    label: 'system.userGroup.delete',
    danger: true,
    eventTag: 'delete',
    permission: ['PROJECT_GROUP:READ+DELETE'],
  },
];
const handleAddMember = () => userModalVisible.value = true;
const handleAddUserCancel = (shouldSearch: boolean) => {
  userModalVisible.value = false;
  if (shouldSearch) {
    emit('addUserSuccess', currentId.value);
  }
}
const {send} = useRequest(() => getUserGroupList(), {immediate: false, force: true});
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
const handleCreateUserGroup = (id: string) => {
  console.log('handleCreateUserGroup', id)
  initData(id);
};

const handleMoreAction = (item: ActionsItem, id: string, authScope: AuthScopeEnum) => {
  const tmpObj = userGroupList.value.filter((ele) => ele.id === id)[0];
  if (item.eventTag === 'rename') {
    popVisible.value[id] = {visible: true, authScope, defaultName: tmpObj.name, id};
  }
  if (item.eventTag === 'delete') {
    let content = '';
    switch (authScope) {
      case AuthScopeEnum.SYSTEM:
        content = t('system.userGroup.beforeDeleteUserGroup');
        break;
      case AuthScopeEnum.ORGANIZATION:
        content = t('org.userGroup.beforeDeleteUserGroup');
        break;
      default:
        content = t('project.userGroup.beforeDeleteUserGroup');
        break;
    }
    window.$dialog.error({
      title: t('system.userGroup.isDeleteUserGroup', {name: tmpObj.name}),
      content,
      positiveText: t('system.userGroup.confirmDelete'),
      negativeText: t('system.userGroup.cancel'),
      maskClosable: false,
      async onPositiveClick() {
        if (systemType === AuthScopeEnum.SYSTEM) {
          await deleteUserGroup(id);
        }
        if (systemType === AuthScopeEnum.ORGANIZATION) {
          await deleteOrgUserGroup(id);
        }
        window.$message.error(t('system.user.deleteUserSuccess'))
        await initData();
      },
    })
  }
}
const handleRenameCancel = (element: UserGroupItem, id?: string) => {
  if (id) {
    initData(id, true);
  }
  popVisible.value[element.id].visible = false;
};
defineExpose({
  initData,
});
</script>

<template>
  <div class="flex flex-col px-[16px] pb-[16px]">
    <div class="sticky top-0 z-[999] pb-[8px] pt-[16px]">
      <n-input :placeholder="$t('system.userGroup.searchHolder')"/>
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
              @click="systemToggle = false"
              type="i-mdi-arrow-expand-down"
          />
          <mm-icon
              v-else
              class="cursor-pointer"
              @click="systemToggle = true"
              type="i-mdi-arrow-expand-right"
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
                  type="i-mdi-plus-circle-outline"
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
                :default-name="popVisible[element.id].defaultName"
                :id="popVisible[element.id].id"
                @cancel="systemUserGroupVisible = false"
                @submit="handleCreateUserGroup"
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
                        type="i-mdi-plus-circle-outline"
                        @click="handleAddMember"
                    />
                  </div>
                  <more-action v-if="
                      isSystemShowAll &&
                      !element.internal &&
                      (element.scopeId !== 'global' || !isGlobalDisable) &&
                      systemMoreAction.length > 0
                    "
                               :list="systemMoreAction"
                               @select="(value) => handleMoreAction(value, element.id, AuthScopeEnum.SYSTEM)">
                    <div class="icon-button"/>
                  </more-action>
                </div>
              </div>
            </create-user-group-popup>
          </div>
          <n-divider/>
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
              @click="orgToggle = false"
              type="i-mdi-arrow-expand-down"
          />
          <mm-icon
              v-else
              class="cursor-pointer"
              @click="orgToggle = true"
              type="i-mdi-arrow-expand-right"
          />
          <div class="text-[14px]">
            {{ $t("system.userGroup.orgUserGroup") }}
          </div>
        </div>
        <create-user-group-popup :list="orgUserGroupList"
                                 :visible="orgUserGroupVisible"
                                 :auth-scope="AuthScopeEnum.ORGANIZATION"
                                 @cancel="orgUserGroupVisible = false"
                                 @submit="handleCreateUserGroup">
          <n-tooltip trigger="hover" placement="right">
            <template #trigger>
              <mm-icon
                  v-permission="props.addPermission"
                  type="i-mdi-plus-circle-outline"
                  class="cursor-pointer"
                  @click="orgUserGroupVisible = true"
              />
            </template>
            {{ `创建${$t("system.userGroup.orgUserGroup")}` }}
          </n-tooltip>
        </create-user-group-popup>
      </div>
      <transition>
        <div v-if="orgToggle">
          <div
              v-for="element in orgUserGroupList"
              :key="element.id"
              class="list-item"
              :class="{ 'bg-lime-200': element.id === currentId }"
              @click="handleListItemClick(element)"
          >
            <create-user-group-popup
                :list="orgUserGroupList"
                :auth-scope="popVisible[element.id].authScope"
                :visible="popVisible[element.id].visible"
                :default-name="popVisible[element.id].defaultName"
                :id="popVisible[element.id].id"
                @cancel="handleRenameCancel(element)"
                @submit="handleRenameCancel(element, element.id)"
            >
              <div
                  class="flex max-w-[100%] grow flex-row items-center justify-between"
              >
                <n-tooltip trigger="hover">
                  <template #trigger>
                    <div>{{ element.name }}</div>
                    <div v-if="systemType === AuthScopeEnum.ORGANIZATION">{{
                        `(${
                            element.internal
                                ? t('common.internal')
                                : element.scopeId === 'global'
                                    ? t('common.system.custom')
                                    : t('common.custom')
                        })`
                      }}
                    </div>
                  </template>
                  {{
                    systemType === AuthScopeEnum.ORGANIZATION ? element.name +
                        `(${
                            element.internal
                                ? t('common.internal')
                                : element.scopeId === 'global'
                                    ? t('common.system.custom')
                                    : t('common.custom')
                        })`
                        : element.name
                  }}
                </n-tooltip>

                <div
                    v-if="
                    element.type === systemType ||
                    (isOrdShowAll &&
                      !element.internal &&
                      (element.scopeId !== 'global' || !isGlobalDisable) &&
                      orgMoreAction.length > 0)
                  "
                    class="list-item-action flex flex-row items-center gap-[8px] opacity-0"
                    :class="{ '!opacity-100': element.id === currentId }"
                >
                  <div v-if="element.type === systemType" class="icon-button">
                    <mm-icon
                        v-permission="props.updatePermission"
                        type="i-mdi-plus-circle-outline"
                        @click="handleAddMember"
                    />
                  </div>
                  <more-action v-if="
                      isOrdShowAll &&
                      !element.internal &&
                      (element.scopeId !== 'global' || !isGlobalDisable) &&
                      orgMoreAction.length > 0
                    "
                               :list="orgMoreAction"
                               @select="(value) => handleMoreAction(value, element.id, AuthScopeEnum.ORGANIZATION)">
                    <div class="icon-button">

                    </div>
                  </more-action>
                </div>
              </div>
            </create-user-group-popup>
          </div>
          <n-divider/>
        </div>
      </transition>
    </div>
    <div v-if="showProject" v-permission="['PROJECT_GROUP:READ']" class="mt-2">
      <div class="flex items-center justify-between px-[4px] py-[7px]">
        <div class="flex flex-row items-center gap-1">
          <mm-icon
              v-if="projectToggle"
              class="cursor-pointer"
              @click="projectToggle = false"
              type="i-mdi-arrow-expand-down"
          />
          <mm-icon
              v-else
              class="cursor-pointer"
              @click="projectToggle = true"
              type="i-mdi-arrow-expand-right"
          />
          <div class="text-[14px]">
            {{ $t("system.userGroup.projectUserGroup") }}
          </div>
        </div>
        <create-user-group-popup :visible="projectUserGroupVisible" :list="projectUserGroupList"
                                 :auth-scope="AuthScopeEnum.PROJECT">
          <n-tooltip trigger="hover" placement="right">
            <template #trigger>
              <mm-icon
                  v-permission="props.addPermission"
                  type="i-mdi-plus-circle-outline"
                  class="cursor-pointer"
                  @click="projectUserGroupVisible = true"
              />
            </template>
            {{ `创建${$t("system.userGroup.projectUserGroup")}` }}
          </n-tooltip>
        </create-user-group-popup>
      </div>
      <transition>
        <div v-if="projectToggle">
          <div
              v-for="element in projectUserGroupList"
              :key="element.id"
              class="list-item"
              :class="{ 'bg-lime-200': element.id === currentId }"
              @click="handleListItemClick(element)"
          >
            <create-user-group-popup
                :list="projectUserGroupList"
                :auth-scope="popVisible[element.id].authScope"
                :visible="popVisible[element.id].visible"
                :default-name="popVisible[element.id].defaultName"
                :id="popVisible[element.id].id"
                @cancel="handleRenameCancel(element)"
                @submit="handleRenameCancel(element, element.id)"
            >
              <div
                  class="flex max-w-[100%] grow flex-row items-center justify-between"
              >
                <n-tooltip trigger="hover">
                  <template #trigger>
                    <div>{{ element.name }}</div>
                  </template>
                  {{ element.name }}
                </n-tooltip>

                <div
                    v-if="
                    element.type === systemType ||
                    (isProjectShowAll &&
                      !element.internal &&
                      (element.scopeId !== 'global' || !isGlobalDisable) &&
                      projectMoreAction.length > 0)
                  "
                    class="list-item-action flex flex-row items-center gap-[8px] opacity-0"
                    :class="{ '!opacity-100': element.id === currentId }"
                >
                  <div v-if="element.type === systemType" class="icon-button">
                    <mm-icon
                        v-permission="props.updatePermission"
                        type="i-mdi-plus-circle-outline"
                        @click="handleAddMember"
                    />
                  </div>
                  <more-action v-if="
                      isProjectShowAll &&
                      !element.internal &&
                      (element.scopeId !== 'global' || !isGlobalDisable) &&
                      projectMoreAction.length > 0
                    "
                               :list="projectMoreAction"
                               @select="(value) => handleMoreAction(value, element.id, AuthScopeEnum.PROJECT)">
                    <div class="icon-button">

                    </div>
                  </more-action>
                </div>
              </div>
            </create-user-group-popup>
          </div>
          <n-divider/>
        </div>
      </transition>
    </div>
  </div>
  <add-user-modal :visible="userModalVisible" :current-id="currentItem.id" @cancel="handleAddUserCancel"/>
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

.icon-button {
  display: flex;
  box-sizing: border-box;
  justify-content: center;
  align-items: center;
}
</style>
