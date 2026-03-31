export interface OptionConfigItem {
  code: string
  label: string
  subLabel?: string | null
  description?: string | null
  icon?: string | null
  sort: number
  enabled: boolean
  extra: Record<string, unknown>
}

export type OptionConfigGroupMap = Record<string, OptionConfigItem[]>
