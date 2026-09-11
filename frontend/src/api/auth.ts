import { get, post } from './client'
import type { UserInfo } from '../types'

export interface AuthResponse {
  token: string
  expiresAtEpochSeconds: number
  user: UserInfo
}

/** 学籍核验（F-ACC-004）：通过返回一次性核验票据 */
export const verifyStudent = (studentId: string, name: string) =>
  post<{ ticket: string }>('/auth/verify-student', { studentId, name })

export const sendCaptcha = (target: string) => post<void>('/auth/captcha', { target })

export const register = (payload: { ticket: string; email: string; code: string; nickname: string }) =>
  post<AuthResponse>('/auth/register', payload)

export const login = (email: string, code: string) => post<AuthResponse>('/auth/login', { email, code })

export const fetchMe = () => get<UserInfo>('/users/me')
