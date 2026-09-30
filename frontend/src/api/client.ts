import type { ProblemDetails } from '../types/api'

const API_BASE = '/api/v1'

export class ApiError extends Error {
  readonly status: number
  readonly problem?: ProblemDetails

  constructor(status: number, message: string, problem?: ProblemDetails) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.problem = problem
  }
}

export async function apiRequest<T>(path: string, signal?: AbortSignal): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    signal,
    headers: { Accept: 'application/json' }
  })

  if (!response.ok) {
    const contentType = response.headers.get('content-type') ?? ''
    const problem = contentType.includes('json')
      ? await response.json().catch(() => undefined) as ProblemDetails | undefined
      : undefined
    throw new ApiError(
      response.status,
      problem?.detail || problem?.title || `请求失败（${response.status}）`,
      problem
    )
  }

  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export async function apiPost<T>(path: string, body: unknown, signal?: AbortSignal): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    body: JSON.stringify(body),
    signal,
    headers: { Accept: 'application/json', 'Content-Type': 'application/json' }
  })
  if (!response.ok) {
    const contentType = response.headers.get('content-type') ?? ''
    const problem = contentType.includes('json')
      ? await response.json().catch(() => undefined) as ProblemDetails | undefined
      : undefined
    throw new ApiError(response.status, problem?.detail || problem?.title || `请求失败（${response.status}）`, problem)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export function getErrorMessage(error: unknown): string {
  if (error instanceof DOMException && error.name === 'AbortError') return '请求等待超时，请稍后重试。'
  if (error instanceof ApiError) {
    if (error.status === 404) return '没有找到请求的内容。'
    if (error.status >= 500) return '服务器暂时无法完成请求，请稍后重试。'
    return error.message
  }
  if (error instanceof TypeError) return '暂时无法连接内容服务，请检查网络后重试。'
  return '加载内容时发生未知错误，请稍后重试。'
}
