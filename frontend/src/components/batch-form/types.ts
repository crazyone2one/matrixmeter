import type {FormItemRule, SelectOption, SelectGroupOption} from 'naive-ui'

export type FormItemType = 'input' | 'select' | 'inputNumber' | 'tagInput' | 'multiple' | 'switch' | 'textarea';
export type FormMode = 'create' | 'edit';
export type ValueType = 'Array' | 'string';

// 自定义检验器，为了传入动态渲染的表单项下标
export interface CustomValidator {
    notRepeat?: boolean;
}

export interface FormItemModel {
    field: string;
    type: FormItemType;
    rules?: (FormItemRule & CustomValidator)[];
    label?: string;
    placeholder?: string;
    min?: number;
    max?: number;
    maxLength?: number;
    hideAsterisk?: boolean;
    hideLabel?: boolean;
    children?: FormItemModel[];
    options?: (string | number | boolean | SelectOption | SelectGroupOption)[]; // select option 选项
    filedNames?: string; // select option 选项字段名
    className?: string; // 自定义样式
    defaultValue?: string | string[] | number | number[] | boolean; // 默认值
    hasRedStar?: boolean; // 是否有红星
    tooltip?: string;

    [key: string]: any;
}
