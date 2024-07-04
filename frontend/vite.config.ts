import vue from "@vitejs/plugin-vue";
import * as path from "node:path";
import { defineConfig, loadEnv } from "vite";

// https://vitejs.dev/config/
export default defineConfig(({ command, mode }) => {
  // 根据当前工作目录中的 `mode` 加载 .env 文件
  // 设置第三个参数为 '' 来加载所有环境变量，而不管是否有 `VITE_` 前缀。
  const env = loadEnv(mode, process.cwd(), "");
  return {
    // vite 配置
    plugins: [vue()],
    server: {
      host: true,
      proxy: {
        [env.VITE_APP_BASE_API]: {
          target: env.VITE_APP_PROXY_URL,
          changeOrigin: true,
          rewrite: (path) =>
            path.replace(new RegExp("^" + env.VITE_APP_BASE_API), ""),
        },
      },
    },
    resolve: {
      alias: [
        {
          find: /\/@\//,
          replacement: path.resolve(__dirname, ".", "src") + "/",
        },
      ],
      extensions: [".mjs", ".js", ".ts", ".jsx", ".tsx", ".json", ".vue"], // 自动匹配文件后缀名
    },
    build: {
      rollupOptions: {
        output: {
          manualChunks: {
            "naive-ui": ["naive-ui"],
          },
        },
      },
    },
    define: {
      __APP_ENV__: JSON.stringify(env.APP_ENV),
    },
  };
});
