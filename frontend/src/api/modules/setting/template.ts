import {alovaInstance} from '/@/api/index.ts'
import {getOrdTemplateStateUrl, getProjectTemplateStateUrl} from "/@/api/requrls/setting/template.ts";

/**
 * 获取模板列表的状态(组织)
 * @param scopedId
 */
export const getOrdTemplate = (scopedId: string) => alovaInstance.Get<Record<string, boolean>>(`${getOrdTemplateStateUrl}/${scopedId}`)
/**
 * 获取模板列表的状态(项目)
 * @param scopedId
 */
export const getProTemplate = (scopedId: string) => alovaInstance.Get<Record<string, boolean>>(`${getProjectTemplateStateUrl}/${scopedId}`)