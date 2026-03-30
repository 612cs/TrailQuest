import path from 'node:path'

import { expect, test } from '@playwright/test'

const sampleTrackPath = path.resolve(__dirname, 'fixtures/sample-track.kml')

const fakeUser = {
  id: 'user-1001',
  username: '测试用户',
  avatar: '测',
  avatarBg: '#1f6b3b',
  role: '徒步爱好者',
  joinDate: '2026年3月',
  postCount: 3,
  savedCount: 2,
  bio: '',
  location: '萍乡',
  hikingProfile: null,
}

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('trailquest_access_token', 'test-token')
    localStorage.setItem('trailquest_user_profile', JSON.stringify({
      id: 'user-1001',
      username: '测试用户',
      avatar: '测',
      avatarBg: '#1f6b3b',
      role: '徒步爱好者',
      joinDate: '2026年3月',
      postCount: 3,
      savedCount: 2,
      bio: '',
      location: '萍乡',
      hikingProfile: null,
    }))

    ;(window as Window & { __TRAILQUEST_TEST_OSS_CLIENT__: unknown }).__TRAILQUEST_TEST_OSS_CLIENT__ = {
      multipartUpload: async (_objectKey: string, _file: File, options?: { progress?: (percentage: number) => void }) => {
        options?.progress?.(1)
        return { res: { status: 200 } }
      },
    }
  })

  await page.route('**/api/uploads/sts', async (route) => {
    const body = route.request().postDataJSON() as { bizType: string; fileName: string }
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        code: 'OK',
        message: 'STS 创建成功',
        data: {
          region: 'cn-shenzhen',
          endpoint: 'oss-cn-shenzhen.aliyuncs.com',
          bucket: 'trailquest-prod-media',
          accessKeyId: 'test-ak',
          accessKeySecret: 'test-sk',
          securityToken: 'test-sts-token',
          objectKey: `${body.bizType}/${body.fileName}`,
          publicUrlBase: 'https://trailquest-prod-media.oss-cn-shenzhen.aliyuncs.com',
        },
      }),
    })
  })

  await page.route('**/api/uploads/complete', async (route) => {
    const body = route.request().postDataJSON() as { bizType: string; url: string; originalName: string }
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        code: 'OK',
        message: '文件登记成功',
        data: {
          mediaId: `${body.bizType}-media-id`,
          url: body.url,
          originalName: body.originalName,
        },
      }),
    })
  })

  await page.route('**/api/geo/reverse', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        code: 'OK',
        message: '地点识别成功',
        data: {
          lng: 114.116147,
          lat: 27.454187,
          country: '中国',
          province: '江西',
          city: '萍乡',
          district: '芦溪',
          formattedLocation: '萍乡 芦溪',
        },
      }),
    })
  })

  await page.route('**/api/trails', async (route) => {
    if (route.request().method() !== 'POST') {
      await route.continue()
      return
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        code: 'OK',
        message: '路线已提交审核，请耐心等待',
        data: {
          id: 'trail-1001',
          image: 'https://trailquest-prod-media.oss-cn-shenzhen.aliyuncs.com/trail_cover/cover.png',
          name: '武功山反穿',
          location: '萍乡 芦溪',
          geoCountry: '中国',
          geoProvince: '江西',
          geoCity: '萍乡',
          geoDistrict: '芦溪',
          geoSource: 'track_reverse',
          difficulty: 'moderate',
          difficultyLabel: '适中',
          packType: 'light',
          durationType: 'single_day',
          rating: 0,
          reviewCount: 0,
          distance: '16.4 km',
          elevation: '+1128 m',
          duration: '5h 8m',
          description: '适合周末的一条山脊线路',
          tags: ['云海', '单日'],
          favorites: 0,
          likes: 0,
          likedByCurrentUser: false,
          favoritedByCurrentUser: false,
          authorId: fakeUser.id,
          publishTime: '刚刚',
          createdAt: '2026-03-30T10:00:00',
          ownedByCurrentUser: true,
          editableByCurrentUser: true,
          coverMediaId: 'trail_cover-media-id',
          gallery: [],
          author: {
            id: fakeUser.id,
            username: fakeUser.username,
            avatar: fakeUser.avatar,
            avatarBg: fakeUser.avatarBg,
          },
          track: {
            hasTrack: false,
          },
        },
      }),
    })
  })

  await page.route('**/api/trails/trail-1001', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        code: 'OK',
        message: '获取成功',
        data: {
          id: 'trail-1001',
          image: 'https://trailquest-prod-media.oss-cn-shenzhen.aliyuncs.com/trail_cover/cover.png',
          name: '武功山反穿',
          location: '萍乡 芦溪',
          difficulty: 'moderate',
          difficultyLabel: '适中',
          packType: 'light',
          durationType: 'single_day',
          rating: 0,
          reviewCount: 0,
          distance: '16.4 km',
          elevation: '+1128 m',
          duration: '5h 8m',
          description: '适合周末的一条山脊线路',
          tags: ['云海', '单日'],
          favorites: 0,
          likes: 0,
          likedByCurrentUser: false,
          favoritedByCurrentUser: false,
          authorId: fakeUser.id,
          publishTime: '刚刚',
          createdAt: '2026-03-30T10:00:00',
          ownedByCurrentUser: true,
          editableByCurrentUser: true,
          coverMediaId: 'trail_cover-media-id',
          gallery: [],
          author: {
            id: fakeUser.id,
            username: fakeUser.username,
            avatar: fakeUser.avatar,
            avatarBg: fakeUser.avatarBg,
          },
          track: {
            hasTrack: false,
          },
        },
      }),
    })
  })

  await page.route('**/api/trails/trail-1001/weather', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ success: true, code: 'OK', message: '天气加载成功', data: { locationContext: { lng: 114.12, lat: 27.45, resolvedFrom: 'start_coordinate' }, forecast: [], source: { provider: 'qweather', cached: false, dailyReady: true, currentReady: true, hourlyReady: true, astroReady: false, lightPollutionReady: false } } }),
    })
  })

  await page.route('**/api/trails/trail-1001/landscape-prediction**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ success: true, code: 'OK', message: '景观预测加载成功', data: { cloudSea: { enabled: true, score: 0.32, confidence: 0.62, bestWindow: '04:00-07:00', resolvedFrom: 'weather_hourly' }, rime: { enabled: true, score: 0.08, confidence: 0.58, bestWindow: '04:00-07:00', resolvedFrom: 'weather_hourly' }, icicle: { enabled: true, score: 0.05, confidence: 0.56, bestWindow: '00:00-06:00', resolvedFrom: 'weather_hourly' }, source: { provider: 'qweather' } } }),
    })
  })
})

test('should publish a trail after uploading cover and track files', async ({ page }) => {
  await page.goto('/publish')

  await page.getByTestId('publish-cover-input').setInputFiles({
    name: 'cover.png',
    mimeType: 'image/png',
    buffer: Buffer.from('fake-image-content'),
  })
  await page.getByTestId('publish-track-input').setInputFiles(sampleTrackPath)

  await page.getByTestId('publish-name-input').fill('武功山反穿')
  await expect(page.getByTestId('publish-location-input')).toHaveValue('萍乡 芦溪')
  await page.getByTestId('publish-description-input').fill('适合周末的一条山脊线路')

  await page.getByTestId('publish-submit-button').click()

  await expect(page).toHaveURL(/\/trail\/trail-1001$/)
  await expect(page.getByText('武功山反穿')).toBeVisible()
})
