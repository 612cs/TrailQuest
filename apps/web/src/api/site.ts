import { http } from './http'
import type { HomeHeroSetting } from '../types/site'

export function fetchHomeHeroSetting() {
  return http.get<HomeHeroSetting>('/api/site/home-hero')
}
