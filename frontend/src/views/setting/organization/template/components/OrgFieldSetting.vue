<script setup lang="ts">
import BaseCard from '/@/components/base-card/index.vue';
import FieldSetting from "/@/views/setting/organization/template/components/FieldSetting.vue";
</script>

<template>
  <base-card simple has-breadcrumb>
    <field-setting mode="organization" :delete-permission="['ORGANIZATION_TEMPLATE:READ+DELETE']"
                   :update-permission="['ORGANIZATION_TEMPLATE:READ+UPDATE']"
                   :create-permission="['ORGANIZATION_TEMPLATE:READ+ADD']"/>
  </base-card>
</template>

<style scoped>

</style>