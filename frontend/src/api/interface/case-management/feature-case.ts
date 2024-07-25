export interface CustomAttributes {
    fieldId: string;
    fieldName: string;
    required: boolean;
    apiFieldId: null | undefined | 'string'; // 三方API
    defaultValue: string;
    type: string;
    options: OptionsField[];
}

export interface OptionsField {
    fieldId: string;
    value: string;
    text: string;
    internal: boolean; // 是否是内置
}