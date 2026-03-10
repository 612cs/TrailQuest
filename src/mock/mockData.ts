// ===== 评论数据（支持多级回复） =====
export interface Review {
    id: number
    trailId?: number // 顶级评论所属的路线 ID
    userId: number // 评论者 ID
    rating?: number // 顶级评论才有评分，回复没有
    time: string
    text: string
    images?: string[]
    replies?: Review[] // 子回复
    replyTo?: string // 回复的用户名
}

// ===== 用户数据 =====
export interface User {
    id: number
    username: string
    avatar: string
    avatarBg: string
    email?: string
    password?: string
}

export const mockUsers: User[] = [
    { id: 101, username: 'Sarah M.', avatar: 'SM', avatarBg: '#8b5cf6', email: 'sarah@example.com', password: 'password123' },
    { id: 102, username: '云游者', avatar: 'YY', avatarBg: '#0891b2', email: 'yunyou@example.com', password: 'password123' },
    { id: 103, username: 'Lisa W.', avatar: 'LW', avatarBg: '#2563eb', email: 'lisa@example.com', password: 'password123' },
    { id: 104, username: '@hiking_queen', avatar: 'HQ', avatarBg: '#8b5cf6', email: 'queen@example.com', password: 'password123' },
    { id: 105, username: '@forest_spirit', avatar: 'FS', avatarBg: '#059669', email: 'spirit@example.com', password: 'password123' },
    { id: 106, username: '摄影达人', avatar: 'SY', avatarBg: '#0284c7', email: 'photo@example.com', password: 'password123' },
    { id: 107, username: 'Mike R.', avatar: 'MR', avatarBg: '#ef4444', email: 'mike@example.com', password: 'password123' },
    { id: 108, username: 'Tom K.', avatar: 'TK', avatarBg: '#16a34a', email: 'tom@example.com', password: 'password123' },
    { id: 109, username: '山野清风', avatar: 'SY', avatarBg: '#7c3aed', email: 'shanye@example.com', password: 'password123' },
    { id: 110, username: '小背包', avatar: 'XB', avatarBg: '#ea580c', email: 'backpack@example.com', password: 'password123' },
    { id: 111, username: '独行侠', avatar: 'DX', avatarBg: '#4f46e5', email: 'loner@example.com', password: 'password123' },
    { id: 112, username: '峰行者', avatar: 'FX', avatarBg: '#dc2626', email: 'peak@example.com', password: 'password123' },
    { id: 113, username: '户外新人', avatar: 'HW', avatarBg: '#059669', email: 'newbie@example.com', password: 'password123' },
    { id: 114, username: '山之恋', avatar: 'SZ', avatarBg: '#7c3aed', email: 'mountainlove@example.com', password: 'password123' },
    { id: 115, username: '@mountain_jess', avatar: 'MJ', avatarBg: '#4f9a48', email: 'jess@example.com', password: 'password123' },
    { id: 116, username: '@river_wanderer', avatar: 'RW', avatarBg: '#2563eb', email: 'river@example.com', password: 'password123' },
]

// ===== 路线数据 =====
// ... (rest of mockData.ts)
