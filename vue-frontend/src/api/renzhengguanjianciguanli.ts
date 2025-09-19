// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 添加关键词 添加新的关键词 POST /api/cert-keywords/add */
export async function addKeyword1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.addKeyword1Params,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("api/api/cert-keywords/add", {
    method: "POST",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 检查文本关键词 检查文本是否包含任何关键词 POST /api/cert-keywords/check */
export async function checkKeywords(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.checkKeywordsParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("api/api/cert-keywords/check", {
    method: "POST",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 删除关键词 根据关键词内容删除关键词 DELETE /api/cert-keywords/delete */
export async function deleteKeyword1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteKeyword1Params,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("api/api/cert-keywords/delete", {
    method: "DELETE",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 删除0匹配关键词 删除所有匹配数量为0的关键词 DELETE /api/cert-keywords/delete-zero-match */
export async function deleteZeroMatchKeywords(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>("api/api/cert-keywords/delete-zero-match", {
    method: "DELETE",
    ...(options || {}),
  });
}

/** 获取启用的关键词 获取所有启用的关键词字符串列表 GET /api/cert-keywords/enabled */
export async function getEnabledKeywords(options?: { [key: string]: any }) {
  return request<Record<string, any>>("api/api/cert-keywords/enabled", {
    method: "GET",
    ...(options || {}),
  });
}

/** 初始化默认关键词 初始化系统默认的关键词列表 POST /api/cert-keywords/initialize */
export async function initializeKeywords(options?: { [key: string]: any }) {
  return request<Record<string, any>>("api/api/cert-keywords/initialize", {
    method: "POST",
    ...(options || {}),
  });
}

/** 获取所有关键词 获取所有启用的关键词列表 GET api/api/cert-keywords/list */
export async function getAllKeywords1(options?: { [key: string]: any }) {
  return request<Record<string, any>>("api/api/cert-keywords/list", {
    method: "GET",
    ...(options || {}),
  });
}

/** 更新关键词 更新关键词信息 PUT /api/cert-keywords/update */
export async function updateKeyword1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateKeyword1Params,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("api/api/cert-keywords/update", {
    method: "PUT",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取关键词匹配数量 获取关键词及其匹配数量 GET /api/cert-keywords/with-match-counts */
export async function getKeywordsWithMatchCounts(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>("api/api/cert-keywords/with-match-counts", {
    method: "GET",
    ...(options || {}),
  });
}
