<script setup lang="ts">
import {NAvatar, NIcon, NText} from "naive-ui";
import {h} from "vue";
import {useUserStore} from "/@/store";
import {useRouter} from "vue-router";
import {useI18n} from "/@/hooks/use-i18n.ts";

const userStore = useUserStore();
const router = useRouter();
const {t} = useI18n()
const renderCustomHeader = () => {
  return h(
      'div',
      {
        style: 'display: flex; align-items: center; padding: 8px 12px;'
      },
      [
        h(NAvatar, {
          round: true,
          style: 'margin-right: 12px;',
          src: 'https://07akioni.oss-cn-beijing.aliyuncs.com/demo1.JPG'
        }),
        h('div', null, [
          h('div', null, [h(NText, {depth: 2}, {default: () => '打工仔'})]),
          h('div', {style: 'font-size: 12px;'}, [
            h(
                NText,
                {depth: 3},
                {default: () => '毫无疑问，你是办公室里最亮的星'}
            )
          ])
        ])
      ]
  )
}
const options = [
  {
    key: 'header',
    type: 'render',
    render: renderCustomHeader
  },
  {
    key: 'header-divider',
    type: 'divider'
  },
  {
    label: () => t('personal.center'),
    key: 'stmt1'
  },
  {
    label: () => t('personal.switchOrg'),
    key: 'stmt2',
    icon: () => {
      return h(NIcon, {}, {
        default: () => {
          return h('div', {class: 'i-matrix:change-circle'})
        }
      })
    }
  },
  {
    label: () => t('personal.exit'),
    key: 'stmt3',
    icon: () => {
      return h(NIcon, {}, {
        default: () => {
          return h('div', {class: 'i-matrix:logout'})
        }
      })
    }
  }
]
const handleSelect = async (key: string) => {
  switch (key) {
    case "stmt3":
      const d = window.$dialog.warning({
        title: t('common.tip'),
        content: t('message.logouting'),
        positiveText: '确定',
        negativeText: '不确定',
        onPositiveClick() {
          d.loading = true
          return new Promise(resolve => {
            setTimeout(async () => {
              resolve(true)
              await userStore.logout();
              const currentRoute = router.currentRoute.value;
              window.$message.success(t('message.logoutSuccess'))
              router.push({
                name: 'login',
                query: {
                  ...router.currentRoute.value.query,
                  redirect: currentRoute.name as string,
                }
              }).then(r => {
                console.log(r)
              });
            }, 1000)
          })
        },
      })
      break;
  }
};
</script>

<template>
  <n-dropdown trigger="click" :options="options" @select="handleSelect">
    <n-button>2021年 第36周</n-button>
  </n-dropdown>
</template>

<style scoped>

</style>