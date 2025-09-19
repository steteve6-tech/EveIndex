// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 健康检查 检查关键词管理服务是否正常运行 GET /api/keyword-management/health */
export async function healthCheck3(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/keyword-management/health", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取所有关键词 获取爬虫搜索关键词列表 GET /api/keyword-management/keywords */
export async function getAllKeywords(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/keyword-management/keywords", {
    method: "GET",
    ...(options || {}),
  });
}

/** 添加关键词 添加新的搜索关键词 POST /api/keyword-management/keywords */
export async function addKeyword(
  body: Record<string, any>,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/keyword-management/keywords", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 清空所有关键词 清空所有搜索关键词 DELETE /api/keyword-management/keywords */
export async function clearAllKeywords(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/keyword-management/keywords", {
    method: "DELETE",
    ...(options || {}),
  });
}

/** 更新关键词 根据索引更新关键词 PUT /api/keyword-management/keywords/${param0} */
export async function updateKeyword(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateKeywordParams,
  body: Record<string, any>,
  options?: { [key: string]: any }
) {
  const { index: param0, ...queryParams } = params;
  return request<Record<string, any>>(
    `/api/keyword-management/keywords/${param0}`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      params: { ...queryParams },
      data: body,
      ...(options || {}),
    }
  );
}

/** 删除关键词 根据索引删除关键词 DELETE /api/keyword-management/keywords/${param0} */
export async function deleteKeyword(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteKeywordParams,
  options?: { [key: string]: any }
) {
  const { index: param0, ...queryParams } = params;
  return request<Record<string, any>>(
    `/api/keyword-management/keywords/${param0}`,
    {
      method: "DELETE",
      params: { ...queryParams },
      ...(options || {}),
    }
  );
}

/** 批量更新关键词 批量更新所有关键词 PUT /api/keyword-management/keywords/batch */
export async function batchUpdateKeywords(
  body: Record<string, any>,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/api/keyword-management/keywords/batch",
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      data: body,
      ...(options || {}),
    }
  );
}
