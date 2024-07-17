<script setup lang="ts">
import {ref, watchEffect} from "vue";
import type {UploadFileInfo, UploadInst} from 'naive-ui'
import {NAlert, NUpload, NUploadDragger} from 'naive-ui'
import {UploadAcceptEnum} from "/@/enums/uploadEnum.ts";
import {UploadType} from "/@/components/base-upload/types.ts";
import {ImportUserUrl} from "/@/api/requrls/setting/user.ts";
import {downloadImportTemplate, importUserInfo} from "/@/api/modules/setting/user.ts";
import {useRequest} from "alova/client";
import {useI18n} from "/@/hooks/use-i18n.ts";
import MmIcon from '/@/components/icon/index.vue'

const props = defineProps<{
  visible: boolean;
  multiple?: boolean;
  accept: UploadType;
}>();
const showModal = ref(props.visible);
const {t} = useI18n()
const userImportFile = ref<UploadFileInfo[]>([]);
const uploadRef = ref<UploadInst | null>(null)
const importSuccessCount = ref(0);
const importFailCount = ref(0);
const importResultVisible = ref(false);
const importResult = ref<'success' | 'allFail' | 'fail'>('allFail');
const importResultTitle = ref(t('system.user.importSuccessTitle'));
const importErrorMessages = ref<Record<string, any>>({});
const emit = defineEmits<{
  (e: 'cancel'): void;
  (e: 'loadList'): void;
}>();
const actionUrl = `${import.meta.env.VITE_APP_BASE_API}` + ImportUserUrl
const handleCancel = () => {
  emit('cancel')
  userImportFile.value = [];
  importResultVisible.value = false
}
const continueImport = () => {
  importResultVisible.value = false;
  userImportFile.value = [];
  showModal.value = true;
}
const {loading: importLoading, send: upload} = useRequest((param) => importUserInfo(param), {immediate: false})
const importUser = () => {
  const formData = new FormData()
  formData.append('file', userImportFile.value[0].file as File);
  upload(formData).then((data) => {
    const failCount = data.importCount - data.successCount;
    if (failCount === data.importCount) {
      importResult.value = 'allFail';
    } else if (failCount > 0) {
      importResult.value = 'fail';
    } else {
      importResult.value = 'success';
    }
    importSuccessCount.value = data.successCount;
    importFailCount.value = failCount;
    importErrorMessages.value = data.errorMessages;
    showImportResult();
  })
  // uploadRef.value?.submit()
}
const showImportResult = () => {
  emit('cancel')
  switch (importResult.value) {
    case 'success':
      importResultTitle.value = t('system.user.importSuccessTitle');
      emit(('loadList'))
      break;
    case 'allFail':
      importResultTitle.value = t('system.user.importModalTitle');
      break;
    case 'fail':
      importResultTitle.value = t('system.user.importModalTitle');
      emit(('loadList'))
      break;
    default:
      break;
  }
  importResultVisible.value = true;
}
const {send: downloadTemplate} = useRequest(() => downloadImportTemplate(), {immediate: false})
const downLoadUserTemplate = () => {
  downloadTemplate().then(res => {
    const blob = new Blob([res as BlobPart], {type: 'application/vnd.ms-excel'})
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '用户导入模板.xlsx'
    a.click()
    window.URL.revokeObjectURL(url)
  })
}
watchEffect(() => {
  showModal.value = props.visible;
});
</script>

<template>
  <n-modal v-model:show="showModal" preset="dialog" title="Dialog" @close="emit('cancel')">
    <template #header>
      <div>{{ $t('system.user.importModalTitle') }}</div>
    </template>
    <n-alert :show-icon="false" class="mb-[16px]">
      {{ $t('system.user.importModalTip') }}
      <n-button text type="warning" size="small" @click="downLoadUserTemplate">
        {{ $t('system.user.importDownload') }}
      </n-button>
    </n-alert>
    <n-upload
        ref="uploadRef"
        directory-dnd
        :max="1"
        multiple
        :accept="
      [UploadAcceptEnum.none, UploadAcceptEnum.unknown].includes(UploadAcceptEnum[props.accept])
        ? '*'
        : UploadAcceptEnum[props.accept]
    "
        :action="actionUrl"
        v-model:file-list="userImportFile"
        :default-upload="false"
    >
      <n-upload-dragger>
        <div style="margin-bottom: 12px">
          <n-icon size="48" :depth="3">
            <div class="i-mdi-archive-arrow-down-outline"/>
          </n-icon>
        </div>
        <n-text style="font-size: 16px">
          点击或者拖动文件到该区域来上传
        </n-text>
        <n-p depth="3" style="margin: 8px 0 0 0">
          请不要上传敏感数据，比如你的银行卡号和密码，信用卡号有效期和安全码
        </n-p>
      </n-upload-dragger>
    </n-upload>
    <template #action>
      <n-button secondary :disabled="importLoading" @click="handleCancel">
        {{ $t('system.user.importModalCancel') }}
      </n-button>
      <n-button type="primary" :loading="importLoading" :disabled="userImportFile.length === 0" @click="importUser">
        {{ $t('system.user.importModalConfirm') }}
      </n-button>
    </template>
  </n-modal>
  <!--  数据导入结果展示-->
  <n-modal v-model:show="importResultVisible" preset="dialog" type="warning" :closable="false">
    <template #header>
      <div>{{ importResultTitle }}</div>
    </template>
    <div v-if="importResult === 'success'" class="flex flex-col items-center justify-center">
      <mm-icon type="i-mdi-checkbox-marked-circle-outline" class="text-[32px] text-green-6"/>
      <div class="mb-[8px] mt-[16px] text-[16px] font-medium leading-[24px] text-[var(--color-text-000)]">
        {{ t('system.user.importSuccess') }}
      </div>
      <div class="sub-text">
        {{ t('system.user.importResultSuccessContent', {successNum: importSuccessCount}) }}
      </div>
    </div>
    <div v-else>
      <n-alert>
        <template #icon>
          <mm-icon type="i-mdi-exclamation-thick" class="text-red-6"/>
        </template>
        <div class="flex items-center">
          {{ t('system.user.importResultContent', {successNum: importSuccessCount}) }}
          <div class="mx-[4px] text-[rgb(var(--danger-6))]">{{ importFailCount }}</div>
          {{ t('system.user.importResultContentEnd') }}
          <n-popover trigger="hover">
            <template #trigger>
              <n-button text>{{ t('system.user.importErrorDetail') }}</n-button>
            </template>
            <div class="px-[16px] pt-[16px] text-[14px]">
              <div class="flex items-center font-medium">
                <div class="text-[var(--color-text-1)]">{{ t('system.user.importErrorMessageTitle') }}</div>
                <div class="ml-[4px] text-[var(--color-text-4)]">({{ importFailCount }})</div>
              </div>
              <div class="import-error-message-list mt-[8px]">
                <div
                    v-for="key of Object.keys(importErrorMessages)"
                    :key="key"
                    class="mb-[16px] flex items-center text-[var(--color-text-2)]"
                >
                  {{ t('system.user.num') }}
                  <div class="mx-[4px] font-medium">{{ key }}</div>
                  {{ t('system.user.line') }}：
                  {{ importErrorMessages[key] }}
                </div>
              </div>
            </div>
            <div v-if="Object.keys(importErrorMessages).length > 8" class="import-error-message-footer">
              <n-button text>
                {{ t('system.user.seeMore') }}
              </n-button>
            </div>
          </n-popover>
        </div>
      </n-alert>
    </div>

    <template #action>
      <n-button text @click="handleCancel">
        {{ $t('system.user.importResultReturn') }}
      </n-button>
      <n-button text @click="continueImport">
        {{ $t('system.user.importResultContinue') }}
      </n-button>
    </template>
  </n-modal>
</template>

<style scoped>

</style>