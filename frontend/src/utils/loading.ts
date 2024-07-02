import { DirectiveBinding, createApp } from "vue";
import Loading from "/@/components/loading/index.vue";

function createLoading(el: HTMLElement) {
  // 创建div标签
  const loadingDom = document.createElement("div");
  // 添加自定义属性作为标识，避免重复loading
  loadingDom.setAttribute("data-v", "loading");
  // 创建APP实例，传入loading组件，并且挂载loading组件和创建的标签
  const app = createApp(Loading);
  const instance = app.mount(loadingDom);
  loadingDom.appendChild(instance.$el);
  el.appendChild(loadingDom);
}
// 创建自定义指令
const vLoading = {
  //mounted的时候，v-loading变量值为true时，加载loading
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    if (binding.value === true) {
      createLoading(el);
    }
  },
  //update的时候
  updated(el: any, binding: DirectiveBinding) {
    //v-loading 的值为false，并且该节点下最后一个元素是loading时，移除节点
    if (binding.value === false && el.lastChild?.dataset.v === "loading") {
      el.removeChild(el.lastChild);
      return;
    }
    //v-loading 的值为true，并且该节点下没有loading节点时，调用函数，挂载loading
    if (binding.value === true && el.lastChild?.dataset.v !== "loading") {
      createLoading(el);
    }
  },
};

//导出创建好的指令
export default vLoading;
