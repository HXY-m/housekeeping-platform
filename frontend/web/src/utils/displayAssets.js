function encodeSvg(svg) {
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
}

function hasCustomImage(url) {
  return typeof url === 'string' && url.trim().length > 0
}

function getServiceTheme(key) {
  const themeMap = {
    'daily-cleaning': { start: '#8f6a49', end: '#d6b48e', badge: '日常保洁' },
    'deep-cleaning': { start: '#6e564c', end: '#c79a86', badge: '深度清洁' },
    'maternal-care': { start: '#9d6d79', end: '#debec7', badge: '母婴护理' },
    'elderly-care': { start: '#6b7763', end: '#bcc7ad', badge: '老人陪护' },
    'appliance-cleaning': { start: '#5d6d7d', end: '#b8c5d5', badge: '家电清洗' }
  }
  return themeMap[key] || { start: '#8f6a49', end: '#d6b48e', badge: '热门服务' }
}

export function getServiceImage(category = {}) {
  if (hasCustomImage(category.imageUrl)) {
    return category.imageUrl
  }

  const slug = category.slug || ''
  const title = category.name || '家政服务'
  const theme = getServiceTheme(slug)

  return encodeSvg(`
    <svg xmlns="http://www.w3.org/2000/svg" width="960" height="640" viewBox="0 0 960 640">
      <defs>
        <linearGradient id="serviceBg" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="${theme.start}" />
          <stop offset="100%" stop-color="${theme.end}" />
        </linearGradient>
      </defs>
      <rect width="960" height="640" rx="44" fill="url(#serviceBg)" />
      <circle cx="780" cy="120" r="132" fill="rgba(255,255,255,0.14)" />
      <circle cx="180" cy="520" r="184" fill="rgba(255,255,255,0.08)" />
      <rect x="76" y="78" width="182" height="52" rx="26" fill="rgba(255,255,255,0.18)" />
      <text x="167" y="111" text-anchor="middle" font-size="24" fill="#ffffff" font-family="Microsoft YaHei, PingFang SC, sans-serif">${theme.badge}</text>
      <rect x="86" y="178" width="380" height="236" rx="30" fill="rgba(255,255,255,0.12)" />
      <path d="M170 372c42-54 92-92 150-114 34-14 72-22 120-24 12 0 20 8 20 20v26c0 12-8 20-20 20-48 4-88 12-128 28-48 18-90 50-128 96-8 10-20 12-30 6l-18-12c-10-6-12-20-4-30z" fill="#ffffff" opacity="0.88" />
      <rect x="526" y="214" width="292" height="24" rx="12" fill="rgba(255,255,255,0.76)" />
      <rect x="526" y="258" width="264" height="16" rx="8" fill="rgba(255,255,255,0.46)" />
      <rect x="526" y="292" width="224" height="16" rx="8" fill="rgba(255,255,255,0.46)" />
      <rect x="526" y="358" width="204" height="92" rx="24" fill="rgba(255,255,255,0.14)" />
      <text x="88" y="522" font-size="56" font-weight="700" fill="#ffffff" font-family="Microsoft YaHei, PingFang SC, sans-serif">${title}</text>
      <text x="88" y="566" font-size="24" fill="rgba(255,255,255,0.92)" font-family="Microsoft YaHei, PingFang SC, sans-serif">标准化上门服务，支持在线预约与履约跟踪</text>
    </svg>
  `)
}

function getWorkerPalette(id = 0) {
  const palettes = [
    { start: '#8b6c52', end: '#d3b897', accent: '#f6efe6' },
    { start: '#7d615d', end: '#cfaca2', accent: '#f7ece9' },
    { start: '#5f7184', end: '#b8c8d8', accent: '#edf2f7' },
    { start: '#6e7662', end: '#bfc7ae', accent: '#eef1e7' }
  ]
  return palettes[Math.abs(Number(id || 0)) % palettes.length]
}

export function getWorkerImage(worker = {}) {
  if (hasCustomImage(worker.avatarUrl)) {
    return worker.avatarUrl
  }

  const palette = getWorkerPalette(worker.id)
  const name = worker.name || '服务人员'
  const city = worker.city || '同城上门'
  const initial = name.slice(0, 1)

  return encodeSvg(`
    <svg xmlns="http://www.w3.org/2000/svg" width="960" height="640" viewBox="0 0 960 640">
      <defs>
        <linearGradient id="workerBg" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="${palette.start}" />
          <stop offset="100%" stop-color="${palette.end}" />
        </linearGradient>
      </defs>
      <rect width="960" height="640" rx="44" fill="url(#workerBg)" />
      <circle cx="748" cy="114" r="104" fill="rgba(255,255,255,0.16)" />
      <rect x="90" y="78" width="176" height="48" rx="24" fill="rgba(255,255,255,0.18)" />
      <text x="178" y="111" text-anchor="middle" font-size="22" fill="#ffffff" font-family="Microsoft YaHei, PingFang SC, sans-serif">实名认证</text>
      <circle cx="286" cy="300" r="144" fill="${palette.accent}" />
      <circle cx="286" cy="258" r="68" fill="#ffffff" opacity="0.96" />
      <path d="M168 406c24-70 82-112 118-112s92 42 116 112v24H168z" fill="#ffffff" opacity="0.96" />
      <circle cx="716" cy="314" r="118" fill="rgba(255,255,255,0.12)" />
      <text x="716" y="342" text-anchor="middle" font-size="96" font-weight="700" fill="#ffffff" font-family="Microsoft YaHei, PingFang SC, sans-serif">${initial}</text>
      <text x="92" y="534" font-size="52" font-weight="700" fill="#ffffff" font-family="Microsoft YaHei, PingFang SC, sans-serif">${name}</text>
      <text x="92" y="578" font-size="24" fill="rgba(255,255,255,0.9)" font-family="Microsoft YaHei, PingFang SC, sans-serif">${city} · 平台推荐服务人员</text>
    </svg>
  `)
}
