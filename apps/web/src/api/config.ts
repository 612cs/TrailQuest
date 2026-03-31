import { http } from './http'
import type { OptionConfigGroupMap } from '../types/config'

export function fetchOptionConfigs(groups: string[]) {
  return http.get<OptionConfigGroupMap>('/api/config/options', {
    params: {
      groups: groups.join(','),
    },
  })
}
