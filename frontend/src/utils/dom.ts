/**
 * 判断是否为服务端渲染
 */
export const isServerRendering = (() => {
    try {
        return !(typeof window !== 'undefined' && document !== undefined);
    } catch (e) {
        return true;
    }
})();

export const mergeStyles=(element: HTMLElement | Element | null, stylesToAdd: string)=>{
    if (element) {
        const originalStyles = element.getAttribute('style') || '';
        const mergedStyles: Record<string, string> = {};
        const originalStylePairs = originalStyles.split(';').filter((style) => style.trim() !== '');

        // 解析原有的 style 属性
        originalStylePairs.forEach((pair) => {
            const [key, value] = pair.split(':').map((item) => item.trim());
            mergedStyles[key] = value;
        });

        // 解析要添加的样式属性
        const stylesToAddPairs = stylesToAdd.split(';').filter((style) => style.trim() !== '');
        stylesToAddPairs.forEach((pair) => {
            const [key, value] = pair.split(':').map((item) => item.trim());
            mergedStyles[key] = value;
        });

        // 构造新的 style 属性字符串
        const mergedStyleString = Object.entries(mergedStyles)
            .map(([key, value]) => `${key}: ${value}`)
            .join(';');

        // 设置新的 style 属性值
        element.setAttribute('style', mergedStyleString);
    }
}