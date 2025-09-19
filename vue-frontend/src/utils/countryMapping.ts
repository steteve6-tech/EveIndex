// 国家代码到中文名称的映射
export const countryMapping: Record<string, string> = {
  'OVERSEAS': '海外国家',
  'CN': '中国',
  'US': '美国',
  'IN': '印度',
  'TH': '泰国',
  'EU': '欧盟',
  'SG': '新加坡',
  'JP': '日本',
  'TW': '台湾',
  'AU': '澳大利亚',
  'KR': '韩国',
  'CL': '智利',
  'MY': '马来西亚',
  'AE': '阿联酋',
  'PE': '秘鲁',
  'ZA': '南非',
  'IL': '以色列',
  'ID': '印度尼西亚'
}

// 获取国家中文名称
export const getCountryName = (code: string | undefined): string => {
  if (!code) return '未设置'
  return countryMapping[code] || code
}

// 获取所有国家选项（用于下拉选择）
export const getCountryOptions = () => {
  return Object.entries(countryMapping).map(([code, name]) => ({
    value: code,
    label: name
  }))
}

// 获取所有国家代码
export const getAllCountryCodes = (): string[] => {
  return Object.keys(countryMapping)
}
