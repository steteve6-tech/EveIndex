// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 获取所有关键词 GET /keyword-management/keywords */
export async function getAllKeywords(
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/keyword-management/keywords", {
    method: "GET",
    ...(options || {}),
  });
}

/** 添加关键词 POST /keyword-management/keywords */
export async function addKeyword(
  params: {
    keyword: string;
    description?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/keyword-management/keywords", {
    method: "POST",
    data: params,
    ...(options || {}),
  });
}

/** 更新关键词 PUT /keyword-management/keywords/${param0} */
export async function updateKeyword(
  params: {
    id: number;
    keyword: string;
    description?: string;
    enabled?: boolean;
  },
  options?: { [key: string]: any }
) {
  const { id: param0, ...bodyParams } = params;
  return request<Record<string, any>>(`/keyword-management/keywords/${param0}`, {
    method: "PUT",
    data: bodyParams,
    ...(options || {}),
  });
}

/** 删除关键词 DELETE /keyword-management/keywords/${param0} */
export async function deleteKeyword(
  params: {
    id: number;
  },
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<Record<string, any>>(`/keyword-management/keywords/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 批量更新关键词 POST /keyword-management/keywords/batch */
export async function batchUpdateKeywords(
  params: {
    keywords: Array<{
      id?: number;
      keyword: string;
      description?: string;
      enabled?: boolean;
    }>;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/keyword-management/keywords/batch", {
    method: "POST",
    data: params,
    ...(options || {}),
  });
}

/** 清空所有关键词 DELETE /keyword-management/keywords */
export async function clearAllKeywords(
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/keyword-management/keywords", {
    method: "DELETE",
    ...(options || {}),
  });
}
