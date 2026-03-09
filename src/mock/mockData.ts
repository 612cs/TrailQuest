// ===== 评论数据（支持多级回复） =====
export interface Review {
    id: number
    user: string
    avatar: string
    avatarBg: string
    rating?: number // 顶级评论才有评分，回复没有
    time: string
    text: string
    images?: string[]
    replies?: Review[] // 子回复
    replyTo?: string // 回复的用户名
}

// ===== 路线数据 =====
export interface Trail {
    id: number
    image: string
    name: string
    location: string
    difficulty: 'easy' | 'moderate' | 'hard'
    difficultyLabel: string
    rating: number
    reviewCount: number
    distance: string
    elevation: string
    duration: string
    description: string
    tags: string[]
    favorites: number
}

// ===== 路线详情（含评论数据） =====
export interface TrailDetail extends Trail {
    reviews: Review[]
}

// ===== 完整路线数据库 =====
export const mockTrailDetails: TrailDetail[] = [
    {
        id: 1,
        image: '/trail-pine.png',
        name: '老鹰峰顶',
        location: '浙江 杭州 临安',
        difficulty: 'moderate',
        difficultyLabel: '适中',
        rating: 4.9,
        reviewCount: 1200,
        distance: '6.4 km',
        elevation: '+420 m',
        duration: '3h 15m',
        description: '老鹰峰顶是临安地区最受欢迎的徒步路线之一，沿途可欣赏壮丽的山谷景色。山顶视野开阔，晴天可远眺天目山脉。',
        tags: ['日出', '山顶', '摄影'],
        favorites: 3842,
        reviews: [
            {
                id: 1,
                user: 'Sarah M.',
                avatar: 'SM',
                avatarBg: '#8b5cf6',
                rating: 5,
                time: '1 周前',
                text: '壮观的景色！山顶的日出令人叹为观止。路径标记清晰,但最后一段比较陡峭。建议穿登山鞋。',
                images: ['/trail-pine.png', '/trail-lake.png'],
                replies: [
                    {
                        id: 101,
                        user: 'Mike R.',
                        avatar: 'MR',
                        avatarBg: '#ef4444',
                        time: '6 天前',
                        text: '同意！日出真的太美了。请问你是几点出发的？',
                        replyTo: 'Sarah M.',
                        replies: [
                            {
                                id: 102,
                                user: 'Sarah M.',
                                avatar: 'SM',
                                avatarBg: '#8b5cf6',
                                time: '6 天前',
                                text: '我凌晨4点半从停车场出发的，大概5点50到山顶，刚好赶上日出🌅',
                                replyTo: 'Mike R.',
                            },
                        ],
                    },
                    {
                        id: 103,
                        user: 'Lisa W.',
                        avatar: 'LW',
                        avatarBg: '#2563eb',
                        time: '5 天前',
                        text: '请问这条路线适合带7岁的小孩去吗？',
                        replyTo: 'Sarah M.',
                    },
                ],
            },
            {
                id: 2,
                user: 'Mike R.',
                avatar: 'MR',
                avatarBg: '#ef4444',
                rating: 4,
                time: '2 周前',
                text: '很棒的路线！沿途有很多拍照点。只是周末人会比较多,建议工作日前往。停车位充足。',
                images: ['/trail-foggy.png'],
            },
            {
                id: 3,
                user: 'Lisa W.',
                avatar: 'LW',
                avatarBg: '#2563eb',
                rating: 5,
                time: '3 周前',
                text: '这条路线非常适合家庭出行。孩子们特别喜欢沿途的小溪和野花。空气很清新,值得再来。',
                replies: [
                    {
                        id: 104,
                        user: 'Tom K.',
                        avatar: 'TK',
                        avatarBg: '#16a34a',
                        time: '2 周前',
                        text: '请问有推荐的停车地点吗？',
                        replyTo: 'Lisa W.',
                    },
                ],
            },
            {
                id: 4,
                user: 'Tom K.',
                avatar: 'TK',
                avatarBg: '#16a34a',
                rating: 3,
                time: '1 个月前',
                text: '路线本身还不错，但指示牌有些模糊，中间有一段岔路容易走错。建议提前下载离线地图。',
            },
        ],
    },
    {
        id: 2,
        image: '/trail-lake.png',
        name: '镜湖环线',
        location: '云南 大理 苍山',
        difficulty: 'easy',
        difficultyLabel: '简单',
        rating: 4.7,
        reviewCount: 856,
        distance: '3.4 km',
        elevation: '+50 m',
        duration: '45m',
        description: '轻松的环湖步道，适合全家出行。湖水清澈见底，四周被苍翠的山林环绕。早晨湖面如镜，倒映雪山，是摄影爱好者的天堂。',
        tags: ['湖泊', '家庭', '休闲'],
        favorites: 5126,
        reviews: [
            {
                id: 10,
                user: '云游者',
                avatar: 'YY',
                avatarBg: '#0891b2',
                rating: 5,
                time: '3 天前',
                text: '湖水真的像镜子一样！早上7点去人少景美，强烈推荐。带了小朋友也完全没问题，步道非常平整。',
                images: ['/trail-lake.png'],
                replies: [
                    {
                        id: 110,
                        user: '山野清风',
                        avatar: 'SY',
                        avatarBg: '#7c3aed',
                        time: '2 天前',
                        text: '确实！我也是早上去的，拍到了雪山倒影，美极了。',
                        replyTo: '云游者',
                    },
                ],
            },
            {
                id: 11,
                user: '小背包',
                avatar: 'XB',
                avatarBg: '#ea580c',
                rating: 4,
                time: '1 周前',
                text: '景色很美，但洗手间设施比较少，建议出发前做好准备。沿途有小卖部可以补给。',
            },
            {
                id: 12,
                user: '独行侠',
                avatar: 'DX',
                avatarBg: '#4f46e5',
                rating: 5,
                time: '2 周前',
                text: '来大理必走的一条步道！环湖一圈很轻松，适合散步发呆。秋天来颜色更漂亮。',
            },
        ],
    },
    {
        id: 3,
        image: '/trail-foggy.png',
        name: '布莱克伍德峡谷',
        location: '四川 雅安 牛背山',
        difficulty: 'hard',
        difficultyLabel: '困难',
        rating: 4.8,
        reviewCount: 2400,
        distance: '18.0 km',
        elevation: '+1,200 m',
        duration: '6h 20m',
        description: '极具挑战性的峡谷穿越路线，需要较好的体力和经验。云海日出是最大的亮点，贡嘎雪山尽收眼底。',
        tags: ['云海', '挑战', '露营'],
        favorites: 8930,
        reviews: [
            {
                id: 20,
                user: '峰行者',
                avatar: 'FX',
                avatarBg: '#dc2626',
                rating: 5,
                time: '5 天前',
                text: '人生必去系列！虽然爬得累但绝对值得。在山顶看到了360度云海环绕，贡嘎金山也清晰可见。记得带厚衣服，山顶温差大。',
                images: ['/trail-foggy.png', '/hero-mountain.png'],
                replies: [
                    {
                        id: 120,
                        user: '户外新人',
                        avatar: 'HW',
                        avatarBg: '#059669',
                        time: '4 天前',
                        text: '请问新手适合走这条路线吗？需要什么装备？',
                        replyTo: '峰行者',
                        replies: [
                            {
                                id: 121,
                                user: '峰行者',
                                avatar: 'FX',
                                avatarBg: '#dc2626',
                                time: '4 天前',
                                text: '不太建议纯新手。至少要有3-4次中等难度的徒步经验。装备方面：登山鞋必须、登山杖推荐、冲锋衣必须、头灯必须（如果要看日出）。',
                                replyTo: '户外新人',
                            },
                            {
                                id: 122,
                                user: '户外新人',
                                avatar: 'HW',
                                avatarBg: '#059669',
                                time: '3 天前',
                                text: '谢谢详细的建议！那我先去练练中等难度的路线再来挑战💪',
                                replyTo: '峰行者',
                            },
                        ],
                    },
                ],
            },
            {
                id: 21,
                user: '山之恋',
                avatar: 'SZ',
                avatarBg: '#7c3aed',
                rating: 4,
                time: '2 周前',
                text: '路线确实有难度，特别是最后2公里的爬升。建议带足水和食物，途中补给点很少。不过风景绝对是顶级的。',
            },
            {
                id: 22,
                user: '摄影达人',
                avatar: 'SY',
                avatarBg: '#0284c7',
                rating: 5,
                time: '3 周前',
                text: '作为风光摄影师，这是我拍过最震撼的地方。日出时分的云海和星空都是绝佳题材。建议山顶露营过夜。',
                images: ['/trail-pine.png'],
            },
        ],
    },
]

// 向后兼容：导出扁平路线列表（供首页、搜索页使用）
export const mockTrails: Trail[] = mockTrailDetails.map(({ reviews, ...trail }) => trail)

// 根据 ID 查找路线详情
export function getTrailById(id: number): TrailDetail | undefined {
    return mockTrailDetails.find((t) => t.id === id)
}

// ===== 预设标签 =====
export const presetTags = [
    '日出', '日落', '湖泊', '森林', '山顶', '瀑布',
    '云海', '雾景', '野花', '露营', '摄影', '家庭',
    '挑战', '休闲', '古道', '溪流',
]

// ===== AI 对话数据 =====
export interface ChatMessage {
    id: string
    role: 'user' | 'assistant'
    content: string
    timestamp: number
}

export const mockSuggestedQuestions = [
    { icon: 'Compass', text: '推荐适合新手的徒步路线' },
    { icon: 'Mountain', text: '登山需要准备哪些装备？' },
    { icon: 'CloudSun', text: '如何判断天气是否适合登山？' },
    { icon: 'Heart', text: '有哪些适合亲子的户外路线？' },
]

export const mockChatReplies: Record<string, string> = {
    '推荐适合新手的徒步路线':
        '为新手推荐以下路线：\n\n🥾 **镜湖环线**（云南大理）\n- 距离：3.4 km | 海拔：+50 m\n- 难度：简单 | 时长：约 45 分钟\n- 亮点：湖水清澈，环境优美，非常适合首次徒步\n\n🌲 **银杉古道**（浙江临安）\n- 距离：5.2 km | 海拔：+180 m\n- 难度：简单 | 时长：约 2 小时\n- 亮点：古树参天，空气清新，路面平整\n\n建议新手首次徒步选择距离在 5km 以内、海拔变化不超过 300m 的路线。出发前记得查看天气预报，穿舒适的运动鞋即可。',

    '登山需要准备哪些装备？':
        '登山装备清单如下：\n\n**必备装备：**\n🎒 背包（20-40L，带防雨罩）\n👟 登山鞋（防滑、防水）\n🧥 冲锋衣/防风外套\n💧 水壶（至少 1.5L）\n🍫 能量补给（坚果、巧克力、能量棒）\n📱 充电宝 + 离线地图\n\n**建议携带：**\n🧢 帽子 + 太阳镜\n🧴 防晒霜\n🩹 急救包（创可贴、绷带）\n🔦 头灯（如行程较长）\n🥢 登山杖（减轻膝盖压力）\n\n根据路线难度和时长，适当调整装备。短途轻装即可，长线徒步需要更专业的装备。',

    '如何判断天气是否适合登山？':
        '判断天气是否适合登山，可以从以下几个方面考虑：\n\n**⛈️ 不建议出行的天气：**\n- 雷暴、暴雨天气\n- 大风（风速 > 6 级）\n- 浓雾（能见度 < 100m）\n- 极端高温（> 38°C）\n\n**✅ 理想的登山天气：**\n- 多云或晴天\n- 温度 15-28°C\n- 风速 1-3 级\n- 降水概率 < 20%\n\n**📱 实用工具：**\n- 查看「TrailQuest」路线页面的实时天气预报\n- 关注当地气象台的预警信息\n- 出发前 2 小时再确认一次天气\n\n⚠️ 山区天气变化快，即使预报晴天也建议携带雨具！',

    '有哪些适合亲子的户外路线？':
        '以下是精选的亲子友好路线：\n\n👨‍👩‍👧‍👦 **镜湖环线**\n- 难度：⭐ 简单 | 约 45 分钟\n- 适合年龄：3 岁以上\n- 亮点：平坦步道，可观察水鸟和鱼群\n\n🌸 **桃花谷自然步道**\n- 难度：⭐ 简单 | 约 1 小时\n- 适合年龄：5 岁以上\n- 亮点：春季桃花盛开，有自然教育标牌\n\n🦋 **蝴蝶谷探险径**\n- 难度：⭐⭐ 简单偏中 | 约 1.5 小时\n- 适合年龄：6 岁以上\n- 亮点：生态丰富，适合自然观察和科普教育\n\n**亲子徒步小贴士：**\n- 选择距离短、坡度缓的路线\n- 多准备零食和水\n- 带上放大镜、望远镜增加趣味性\n- 控制节奏，随时观察孩子状态',
}

// 默认 AI 回复（当问题不匹配预设时）
export const defaultChatReply =
    '感谢你的提问！作为 TrailQuest AI 助手，我可以帮你：\n\n- 🗺️ 推荐适合你的徒步路线\n- 🎒 提供装备和安全建议\n- ☀️ 分析天气对出行的影响\n- 📸 推荐最佳拍照时间和地点\n\n你可以尝试问我更具体的问题，比如"推荐杭州周边的登山路线"或"雨天可以徒步吗"。'

// ===== 工具函数 =====
let nextReviewId = 1000

export function generateReviewId(): number {
    return nextReviewId++
}

export function generateMessageId(): string {
    return `msg_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
}

export function getAIReply(question: string): string {
    // 先精确匹配
    if (mockChatReplies[question]) {
        return mockChatReplies[question]
    }
    // 再模糊匹配关键词
    for (const [key, value] of Object.entries(mockChatReplies)) {
        if (question.includes(key.slice(0, 4)) || key.includes(question.slice(0, 4))) {
            return value
        }
    }
    return defaultChatReply
}

// 格式化收藏数
export function formatFavorites(count: number): string {
    if (count >= 10000) return `${(count / 10000).toFixed(1)}w`
    if (count >= 1000) return `${(count / 1000).toFixed(1)}k`
    return count.toString()
}
