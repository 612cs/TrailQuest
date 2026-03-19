export interface HikingProfile {
  experienceLevel: 'beginner' | 'intermediate' | 'expert'
  trailStyle: 'city_weekend' | 'long_distance' | 'both'
  packPreference: 'light' | 'heavy' | 'both'
}

export interface HikingProfileFormValue {
  experienceLevel: HikingProfile['experienceLevel'] | ''
  trailStyle: HikingProfile['trailStyle'] | ''
  packPreference: HikingProfile['packPreference'] | ''
  location: string
}
