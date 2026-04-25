export type AuthLoginMethod =
  | 'USERNAME_PASSWORD'
  | 'PHONE_SMS_CODE'
  | 'USERNAME_PASSWORD_SMS_CODE'

export type AuthLoginRequest = {
  loginMethod?: AuthLoginMethod
  username?: string
  password?: string
  phone?: string
  smsCode?: string
}

export type AccountUserType = 'ADMIN' | 'TEACHER' | 'STUDENT'

export type AuthLoginResultVO = {
  userId: number
  userType: AccountUserType
  username: string
  name: string
  mustChangePassword?: boolean
}

export type AuthRefreshResultVO = {
  userId: number
  userType: AccountUserType
}
