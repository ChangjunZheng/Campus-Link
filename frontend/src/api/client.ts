const BASE = '/api/v1'

/** 后端未认证错误码：401 / 4001（见 common/result/ResultCode.NOT_LOGGED_IN） */
const UNAUTHORIZED_CODE = 4001

/** 后端统一响应：{ code, message, data, traceId }，code=0 成功（技术方案第 5 节） */
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  traceId?: string
}

/** 统一业务错误：携带错误码与 traceId，便于上层区分处理与排障 */
export class ApiError extends Error {
  constructor(
    readonly code: number,
    message: string,
    readonly traceId?: string,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

/**
 * 401 全局处理器（BUG-004）：由 main.ts 注册（清会话 + 提示 + 跳登录）。
 * client.ts 不直接 import router/store，规避与二者的 ESM 循环依赖。
 */
let unauthorizedHandler: (() => void) | null = null
export function setUnauthorizedHandler(handler: (() => void) | null): void {
  unauthorizedHandler = handler
}

function authHeaders(): Record<string, string> {
  const token = localStorage.getItem('cl_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  let res: Response
  try {
    res = await fetch(BASE + path, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...authHeaders(),
        ...(options.headers as Record<string, string> | undefined),
      },
    })
  } catch {
    throw new ApiError(-1, '网络异常，请检查后端服务是否已启动')
  }
  const body = (await res.json()) as ApiResponse<T>
  if (body.code !== 0) {
    // 未认证：先触发全局退出处理器（清会话 + 跳登录），再按既有语义抛错，调用方的 catch 不受影响
    if (body.code === UNAUTHORIZED_CODE) {
      unauthorizedHandler?.()
    }
    throw new ApiError(body.code, body.message || `请求失败（${body.code}）`, body.traceId)
  }
  return body.data
}

export const get = <T>(path: string): Promise<T> => request<T>(path)

export const post = <T>(path: string, data?: unknown): Promise<T> =>
  request<T>(path, { method: 'POST', body: data === undefined ? undefined : JSON.stringify(data) })

export const put = <T>(path: string, data?: unknown): Promise<T> =>
  request<T>(path, { method: 'PUT', body: data === undefined ? undefined : JSON.stringify(data) })

export const del = <T>(path: string): Promise<T> => request<T>(path, { method: 'DELETE' })
