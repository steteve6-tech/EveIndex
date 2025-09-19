// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 此处后端没有提供注释 GET /notifications */
export async function getNotifications(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getNotificationsParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/notifications", {
    method: "GET",
    params: {
      // page has a default value: 1
      page: "1",
      // limit has a default value: 20
      limit: "20",
      ...params,
    },
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 DELETE /notifications/${param0} */
export async function deleteNotification(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteNotificationParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<Record<string, any>>(`/notifications/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /notifications/email */
export async function createEmailNotification(
  body: Record<string, any>,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/notifications/email", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /notifications/pending */
export async function getPendingNotifications(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>("/notifications/pending", {
    method: "GET",
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /notifications/retry-failed */
export async function retryFailedNotifications(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>("/notifications/retry-failed", {
    method: "POST",
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /notifications/statistics */
export async function getNotificationStatistics(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>("/notifications/statistics", {
    method: "GET",
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /notifications/webhook */
export async function createWebhookNotification(
  body: Record<string, any>,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/notifications/webhook", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
