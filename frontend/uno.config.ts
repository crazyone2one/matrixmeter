// uno.config.ts
import { FileSystemIconLoader } from "@iconify/utils/lib/loader/node-loaders";
import {
  defineConfig,
  presetAttributify,
  presetIcons,
  presetTypography,
  presetUno,
  presetWebFonts,
  transformerDirectives,
  transformerVariantGroup,
} from "unocss";

export default defineConfig({
  shortcuts: [
    // ...
  ],
  theme: {
    colors: {
      // ...
    },
  },
  presets: [
    presetUno(),
    presetAttributify(),
    presetIcons({
      collections: {
        matrix: FileSystemIconLoader("./src/assets/icons", (svg) =>
          svg.replace(/#fff/, "currentColor")
        ),
      },
    }),
    presetTypography(),
    presetWebFonts({
      fonts: {
        // ...
      },
    }),
  ],
  transformers: [transformerDirectives(), transformerVariantGroup()],
  content: {
    // filesystem: ["**/*.{html,js,ts,jsx,tsx,vue,svelte,astro}"],
  },
});
