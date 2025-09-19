// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 更新实体风险等级和关键词 PUT /device-data/update/${entityType}/${id} */
export async function updateEntityRiskLevelAndKeywords(
  params: {
    id: string;
    riskLevel?: string;
    keywords?: string[];
  },
  options?: { [key: string]: any }
) {
  const { id, ...bodyParams } = params;
  // 注意：这里需要传递entityType，但当前API设计中没有entityType参数
  // 我们需要修改API调用方式
  return request<Record<string, any>>(`/device-data/update/DeviceRegistrationRecord/${id}`, {
    method: "PUT",
    data: bodyParams,
    ...(options || {}),
  });
}

/** 重置所有数据为中等风险 POST /device-data-update/reset-all-medium */
export async function resetAllDataToMediumRisk(
  params?: {
    dataType?: string;
    confirm?: boolean;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data-update/reset-all-medium", {
    method: "POST",
    data: params,
    ...(options || {}),
  });
}

/** 批量更新风险等级 POST /device-data-update/batch-risk-level */
export async function batchUpdateRiskLevel(
  params: {
    ids: string[];
    riskLevel: string;
    dataType?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data-update/batch-risk-level", {
    method: "POST",
    data: params,
    ...(options || {}),
  });
}

/** 批量更新关键词 POST /device-data-update/batch-keywords */
export async function batchUpdateKeywords(
  params: {
    ids: string[];
    keywords: string[];
    dataType?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data-update/batch-keywords", {
    method: "POST",
    data: params,
    ...(options || {}),
  });
}
