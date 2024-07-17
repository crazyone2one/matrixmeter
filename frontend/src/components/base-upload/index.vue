<script setup lang="ts">
import {NUpload, NUploadDragger} from 'naive-ui'
import {MmFileItem, UploadType} from "/@/components/base-upload/types.ts";
import {UploadAcceptEnum} from "/@/enums/uploadEnum.ts";
import type {UploadFileInfo} from 'naive-ui'
import {ref} from "vue";
// 上传 组件 props
type UploadProps = Partial<{
  fileList: MmFileItem[];
  mainText: string; // 主要文案
  subText: string; // 次要文案
  showSubText: boolean; // 是否显示次要文案
  class: string;
  multiple: boolean;
  imagePreview: boolean;
  showFileList: boolean;
  disabled: boolean;
  iconType: string;
  maxSize: number; // 文件大小限制，单位 MB
  sizeUnit: 'MB' | 'KB'; // 文件大小单位
  isLimit: boolean; // 是否限制文件大小
  draggable: boolean; // 是否支持拖拽上传
  isAllScreen?: boolean; // 是否是全屏显示拖拽上传
  fileTypeTip?: string; // 上传文件类型错误提示
  limit: number; // 限制上传文件数量
  allowRepeat?: boolean; // 自定义上传文件框，是否允许重复文件名替换
}> & {
  accept: UploadType;
};
const props = withDefaults(defineProps<UploadProps>(), {
  showSubText: true,
  isLimit: true,
  isAllScreen: false,
  cutHeight: 110,
  allowRepeat: false,
});
const emit = defineEmits(['update:fileList', 'change']);

const defaultMaxSize = 50;
const fileListRef = ref<UploadFileInfo[]>([])
const innerFileList = defineModel<MmFileItem[]>('fileList', {
  default: () => [],
});
const handleFileListChange = (data: { fileList: UploadFileInfo[] }) => {
  console.log(data.fileList)
  fileListRef.value = data.fileList
  console.log('fileListRef.value', fileListRef.value)
  // innerFileList = data.fileList
  emit('change', data.fileList)
}
</script>

<template>
  <n-upload
      v-bind="{ ...props }"
      :multiple="props.multiple"
      directory-dnd
      v-model:file-list="innerFileList"
      action="https://www.mocky.io/v2/5e4bafc63100007100d8b70f"
      :max="props.limit"
      :default-upload="false"
      :accept="
      [UploadAcceptEnum.none, UploadAcceptEnum.unknown].includes(UploadAcceptEnum[props.accept])
        ? '*'
        : UploadAcceptEnum[props.accept]
    "
      @update:file-list="handleFileListChange"
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
</template>

<style scoped>

</style>