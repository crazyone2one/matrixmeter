<template>
  <n-h1 style="--font-size: 60px; --font-weight: 100">MatrixMeter</n-h1>
  <n-card size="large" style="--padding-bottom: 30px">
    <n-h2 style="--font-weight: 400">
      {{ $t("login.form.login.success") }}
    </n-h2>
    <n-form size="large" :rules="rules" :model="model">
      <n-form-item-row :label="$t('login.form.username')" path="username">
        <n-input
          v-model:value="model.username"
          :placeholder="$t('login.form.userName.placeholderOther')"
        />
      </n-form-item-row>
      <n-form-item-row :label="$t('login.form.password')" path="password">
        <n-input
          v-model:value="model.password"
          type="password"
          :placeholder="$t('login.form.password.placeholder')"
        />
      </n-form-item-row>
    </n-form>
    <n-button
      type="primary"
      size="large"
      block
      :loading="loading"
      :disabled="disabled"
      @click="handleLogin"
    >
      {{ $t("login.form.login") }}
    </n-button>
    <br />
  </n-card>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";

const rules = {
  username: {
    required: true,
    message: "Username is required.",
    trigger: "blur",
  },
  password: {
    required: true,
    message: "Password is required.",
    trigger: "blur",
  },
};

const model = ref({
  username: "",
  password: "",
});
const loading = ref(false);

const disabled = computed<boolean>(
  () => model.value.username === "" || model.value.password === ""
);
const handleLogin = () => {};
</script>

<style scoped>
.n-h1 {
  margin: 20vh auto 20px;
  text-align: center;
  letter-spacing: 5px;
  opacity: 0.8;
}
.n-h2 {
  text-align: center;
  letter-spacing: 5px;
  opacity: 0.8;
}

.n-card {
  margin: 0 auto;
  max-width: 380px;
  box-shadow: var(--box-shadow);
}
</style>
