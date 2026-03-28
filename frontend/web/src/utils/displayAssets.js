function encodeSvg(svg) {
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
}

function getServiceTheme(key) {
  const themeMap = {
    'daily-cleaning': { start: '#0f766e', end: '#14b8a6', badge: '日常保洁' },
    'deep-cleaning': { start: '#ea580c', end: '#fb923c', badge: '深度清洁' },
    'maternal-care': { start: '#db2777', end: '#f472b6', badge: '母婴护理' },
    'elderly-care': { start: '#2563eb', end: '#60a5fa', badge: '老人陪护' },
    'appliance-cleaning': { start: '#4f46e5', end: '#818cf8', badge: '家电清洗' }
  }
  return themeMap[key] || { start: '#0f766e', end: '#f59e0b', badge: '热门服务' }
}

export function getServiceImage(category = {}) {
  const slug = category.slug || ''
  const title = category.name || '家政服务'
  const theme = getServiceTheme(slug)
  return encodeSvg(`
    <svg xmlns="http://www.w3.org/2000/svg" width="960" height="640" viewBox="0 0 960 640">
      <defs>
        <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="${theme.start}" />
          <stop offset="100%" stop-color="${theme.end}" />
        </linearGradient>
      </defs>
      <rect width="960" height="640" rx="36" fill="url(#bg)" />
      <circle cx="760" cy="120" r="120" fill="rgba(255,255,255,0.12)" />
      <circle cx="180" cy="520" r="160" fill="rgba(255,255,255,0.08)" />
      <rect x="78" y="86" width="196" height="54" rx="27" fill="rgba(255,255,255,0.18)" />
      <text x="176" y="121" text-anchor="middle" font-size="24" fill="#ffffff" font-family="Microsoft YaHei, PingFang SC, sans-serif">${theme.badge}</text>
      <rect x="86" y="198" width="382" height="236" rx="28" fill="rgba(255,255,255,0.14)" />
      <path d="M172 378c36-56 82-96 138-120 34-14 70-22 122-26 12 0 20 8 20 20v24c0 12-8 20-20 20-52 4-92 12-132 30-44 18-82 50-116 96-6 10-18 12-28 6l-20-12c-10-6-12-20-4-30z" fill="#ffffff" opacity="0.88" />
      <rect x="510" y="220" width="330" height="24" rx="12" fill="rgba(255,255,255,0.74)" />
      <rect x="510" y="264" width="276" height="18" rx="9" fill="rgba(255,255,255,0.48)" />
      <rect x="510" y="296" width="236" height="18" rx="9" fill="rgba(255,255,255,0.48)" />
      <rect x="510" y="368" width="202" height="92" rx="24" fill="#ffffff" opacity="0.16" />
      <text x="86" y="520" font-size="54" font-weight="700" fill="#ffffff" font-family="Microsoft YaHei, PingFang SC, sans-serif">${title}</text>
      <text x="86" y="566" font-size="24" fill="rgba(255,255,255,0.88)" font-family="Microsoft YaHei, PingFang SC, sans-serif">标准化服务项目，支持在线预约与上门服务</text>
    </svg>
  `)
}

function getWorkerPalette(id = 0) {
  const palettes = [
    { start: '#0f766e', end: '#2dd4bf', accent: '#fef3c7' },
    { start: '#ea580c', end: '#fb923c', accent: '#ffedd5' },
    { start: '#1d4ed8', end: '#60a5fa', accent: '#dbeafe' },
    { start: '#7c3aed', end: '#c084fc', accent: '#f3e8ff' }
  ]
  return palettes[Math.abs(Number(id || 0)) % palettes.length]
}

export function getWorkerImage(worker = {}) {
  const palette = getWorkerPalette(worker.id)
  const name = worker.name || '服务人员'
  const city = worker.city || '同城服务'
  const initial = name.slice(0, 1)

  return encodeSvg(`
    <svg xmlns="http://www.w3.org/2000/svg" width="960" height="640" viewBox="0 0 960 640">
      <defs>
        <linearGradient id="workerBg" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="${palette.start}" />
          <stop offset="100%" stop-color="${palette.end}" />
        </linearGradient>
      </defs>
      <rect width="960" height="640" rx="36" fill="url(#workerBg)" />
      <circle cx="746" cy="112" r="104" fill="rgba(255,255,255,0.16)" />
      <rect x="96" y="82" width="172" height="48" rx="24" fill="rgba(255,255,255,0.2)" />
      <text x="182" y="114" text-anchor="middle" font-size="24" fill="#ffffff" font-family="Microsoft YaHei, PingFang SC, sans-serif">实名认证</text>
      <circle cx="284" cy="300" r="142" fill="${palette.accent}" />
      <circle cx="284" cy="258" r="68" fill="#ffffff" opacity="0.95" />
      <path d="M170 404c22-70 78-112 114-112 38 0 90 42 112 112v26H170z" fill="#ffffff" opacity="0.95" />
      <circle cx="714" cy="312" r="118" fill="rgba(255,255,255,0.12)" />
      <text x="714" y="336" text-anchor="middle" font-size="96" font-weight="700" fill="#ffffff" font-family="Microsoft YaHei, PingFang SC, sans-serif">${initial}</text>
      <text x="96" y="538" font-size="52" font-weight="700" fill="#ffffff" font-family="Microsoft YaHei, PingFang SC, sans-serif">${name}</text>
      <text x="96" y="580" font-size="24" fill="rgba(255,255,255,0.9)" font-family="Microsoft YaHei, PingFang SC, sans-serif">${city} · 平台推荐服务人员</text>
    </svg>
  `)
}
