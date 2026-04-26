export type CampusStatus = 0 | 1 | 2

export type InternalUserCampusOptionVO = {
  id: number
  name: string
  status: CampusStatus | null
}
