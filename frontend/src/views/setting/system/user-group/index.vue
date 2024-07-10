<script setup lang="ts">
import BaseCard from '/@/components/base-card/index.vue'
import SplitBox from '/@/components/split-box/index.vue'
import UserGroupLeft from "/@/components/user-group-comp/UserGroupLeft.vue";
import {computed, onMounted, provide, ref, watchEffect} from "vue";
import {AuthScopeEnum} from "/@/enums/commonEnum.ts";
import {CurrentUserGroupItem} from "/@/api/interface/setting/user-group.ts";
import {useRouter} from "vue-router";
import AuthTable from "/@/components/user-group-comp/AuthTable.vue";
import {hasAnyPermission} from "/@/utils/permission.ts";

const currentTable = ref('auth');
provide('systemType', AuthScopeEnum.SYSTEM);
const router = useRouter();
const ugLeftRef = ref<InstanceType<typeof UserGroupLeft>>();
const currentUserGroupItem = ref<CurrentUserGroupItem>({
  id: '',
  name: '',
  type: AuthScopeEnum.SYSTEM,
  internal: true,
});
const couldShowUser = computed(() => currentUserGroupItem.value.type === AuthScopeEnum.SYSTEM);
const couldShowAuth = computed(() => currentUserGroupItem.value.id !== 'admin');
const handleSelect = (item: CurrentUserGroupItem) => {
  currentUserGroupItem.value = item;
  console.log(currentUserGroupItem.value)
};
watchEffect(() => {
  if (!couldShowAuth.value) {
    currentTable.value = 'user';
  } else if (!couldShowUser.value) {
    currentTable.value = 'auth';
  } else {
    currentTable.value = 'auth';
  }
});

onMounted(() => {
  ugLeftRef.value?.initData(router.currentRoute.value.query.id as string, true);
});
</script>
<template>
  <base-card simple>
    <split-box>
      <template #first>
        <user-group-left ref="ugLeftRef"
                         :add-permission="['SYSTEM_USER_ROLE:READ+ADD']"
                         :is-global-disable="false"
                         :update-permission="['SYSTEM_USER_ROLE:READ+UPDATE']"
                         @handle-select="handleSelect">

        </user-group-left>
      </template>
      <template #second>
        <div class="flex h-full flex-col overflow-hidden pt-[16px]">
          <div class="mb-4 flex flex-row items-center justify-between px-[16px]">
            <div class="one-line-text max-w-[300px] font-medium">{{ currentUserGroupItem.name }}</div>
            <div class="flex items-center">
              <n-input v-if="currentTable === 'user'" :placeholder="$t('system.user.searchUser')" class="w-[240px]"/>
              <n-radio-group v-if="couldShowUser && couldShowAuth" v-model:value="currentTable" name="radiogroup" class="ml-[14px] w-full">
                <n-flex>
                  <n-radio v-if="couldShowAuth" value="auth">
                    {{ $t('system.userGroup.auth') }}
                  </n-radio>
                  <n-radio v-if="couldShowUser" value="user">
                    {{ $t('system.userGroup.user') }}
                  </n-radio>
                </n-flex>
              </n-radio-group>
            </div>
          </div>
          <div class="flex-1 overflow-hidden">
            <div v-if="currentTable === 'user' && couldShowUser">
              UserTable
            </div>
            <auth-table v-if="currentTable === 'auth' && couldShowAuth" :current="currentUserGroupItem"
                        :save-permission="['SYSTEM_USER_ROLE:READ+UPDATE']"
                        :disabled="!hasAnyPermission(['SYSTEM_USER_ROLE:READ+UPDATE'])"/>
          </div>
        </div>
      </template>
    </split-box>
  </base-card>
</template>


<style scoped></style>
