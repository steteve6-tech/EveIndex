/**
 * Cron表达式工具类
 * 用于解析和生成Cron表达式，以及将其转换为可读格式
 */

export interface CronExpression {
  second: string;
  minute: string;
  hour: string;
  dayOfMonth: string;
  month: string;
  dayOfWeek: string;
  year?: string;
}

export interface ReadableCron {
  description: string;  // 中文描述
  type: 'once' | 'daily' | 'weekly' | 'monthly' | 'custom';  // 任务类型
  details: {
    time?: string;  // HH:mm格式
    dayOfWeek?: number;  // 1-7 (周一到周日)
    dayOfMonth?: number;  // 1-31
    interval?: number;  // 间隔（分钟/小时）
  };
}

/**
 * 解析Cron表达式为对象
 */
export function parseCronExpression(cron: string): CronExpression | null {
  if (!cron || typeof cron !== 'string') {
    return null;
  }

  const parts = cron.trim().split(/\s+/);
  
  if (parts.length < 6 || parts.length > 7) {
    return null;
  }

  return {
    second: parts[0],
    minute: parts[1],
    hour: parts[2],
    dayOfMonth: parts[3],
    month: parts[4],
    dayOfWeek: parts[5],
    year: parts.length === 7 ? parts[6] : undefined
  };
}

/**
 * 将Cron表达式转换为可读的中文描述
 */
export function cronToReadable(cron: string): ReadableCron {
  const expr = parseCronExpression(cron);
  
  if (!expr) {
    return {
      description: '无效的Cron表达式',
      type: 'custom',
      details: {}
    };
  }

  // 每天执行 (0 0 2 * * ?)
  if (expr.dayOfMonth === '*' && expr.month === '*' && (expr.dayOfWeek === '?' || expr.dayOfWeek === '*')) {
    const hour = parseInt(expr.hour);
    const minute = parseInt(expr.minute);
    return {
      description: `每天 ${padZero(hour)}:${padZero(minute)} 执行`,
      type: 'daily',
      details: {
        time: `${padZero(hour)}:${padZero(minute)}`
      }
    };
  }

  // 每周执行 (0 0 2 ? * 1)
  if (expr.dayOfMonth === '?' && expr.month === '*' && expr.dayOfWeek !== '*' && expr.dayOfWeek !== '?') {
    const hour = parseInt(expr.hour);
    const minute = parseInt(expr.minute);
    const dayOfWeek = parseInt(expr.dayOfWeek);
    const dayName = getDayName(dayOfWeek);
    return {
      description: `每周${dayName} ${padZero(hour)}:${padZero(minute)} 执行`,
      type: 'weekly',
      details: {
        time: `${padZero(hour)}:${padZero(minute)}`,
        dayOfWeek: dayOfWeek
      }
    };
  }

  // 每月执行 (0 0 2 1 * ?)
  if (expr.dayOfMonth !== '*' && expr.dayOfMonth !== '?' && expr.month === '*' && (expr.dayOfWeek === '?' || expr.dayOfWeek === '*')) {
    const hour = parseInt(expr.hour);
    const minute = parseInt(expr.minute);
    const day = parseInt(expr.dayOfMonth);
    return {
      description: `每月${day}日 ${padZero(hour)}:${padZero(minute)} 执行`,
      type: 'monthly',
      details: {
        time: `${padZero(hour)}:${padZero(minute)}`,
        dayOfMonth: day
      }
    };
  }

  // 每N分钟执行 (0 */5 * * * ?)
  if (expr.minute.startsWith('*/') && expr.hour === '*' && expr.dayOfMonth === '*' && expr.month === '*') {
    const interval = parseInt(expr.minute.substring(2));
    return {
      description: `每 ${interval} 分钟执行一次`,
      type: 'custom',
      details: {
        interval: interval
      }
    };
  }

  // 每N小时执行 (0 0 */2 * * ?)
  if (expr.minute === '0' && expr.hour.startsWith('*/') && expr.dayOfMonth === '*' && expr.month === '*') {
    const interval = parseInt(expr.hour.substring(2));
    return {
      description: `每 ${interval} 小时执行一次`,
      type: 'custom',
      details: {
        interval: interval * 60
      }
    };
  }

  // 自定义表达式
  return {
    description: `自定义: ${cron}`,
    type: 'custom',
    details: {}
  };
}

/**
 * 根据配置生成Cron表达式
 */
export function generateCronExpression(config: {
  type: 'daily' | 'weekly' | 'monthly' | 'interval';
  time?: string;  // HH:mm
  dayOfWeek?: number;  // 1-7
  dayOfMonth?: number;  // 1-31
  intervalMinutes?: number;
}): string {
  const { type, time, dayOfWeek, dayOfMonth, intervalMinutes } = config;

  // 解析时间
  let hour = 0;
  let minute = 0;
  if (time) {
    const [h, m] = time.split(':').map(Number);
    hour = h;
    minute = m;
  }

  switch (type) {
    case 'daily':
      // 每天指定时间执行
      return `0 ${minute} ${hour} * * ?`;

    case 'weekly':
      // 每周指定星期几的指定时间执行
      return `0 ${minute} ${hour} ? * ${dayOfWeek || 1}`;

    case 'monthly':
      // 每月指定日期的指定时间执行
      return `0 ${minute} ${hour} ${dayOfMonth || 1} * ?`;

    case 'interval':
      // 每隔N分钟执行
      if (intervalMinutes && intervalMinutes < 60) {
        return `0 */${intervalMinutes} * * * ?`;
      } else if (intervalMinutes) {
        const hours = Math.floor(intervalMinutes / 60);
        return `0 0 */${hours} * * ?`;
      }
      return `0 0 */1 * * ?`;

    default:
      return `0 0 2 * * ?`;  // 默认每天凌晨2点
  }
}

/**
 * 获取星期几的中文名称
 */
function getDayName(dayOfWeek: number): string {
  const days = ['', '一', '二', '三', '四', '五', '六', '日'];
  return days[dayOfWeek] || '一';
}

/**
 * 数字补零
 */
function padZero(num: number): string {
  return num < 10 ? `0${num}` : `${num}`;
}

/**
 * 验证Cron表达式是否有效
 */
export function validateCronExpression(cron: string): boolean {
  try {
    const expr = parseCronExpression(cron);
    return expr !== null;
  } catch {
    return false;
  }
}

/**
 * 获取预设的Cron表达式模板
 */
export function getCronPresets(): Array<{ label: string; value: string; description: string }> {
  return [
    { label: '每天凌晨2点', value: '0 0 2 * * ?', description: '适合数据量大的爬虫' },
    { label: '每天早上8点', value: '0 0 8 * * ?', description: '适合工作时间更新' },
    { label: '每天中午12点', value: '0 0 12 * * ?', description: '适合中午数据更新' },
    { label: '每天晚上10点', value: '0 0 22 * * ?', description: '适合晚间数据抓取' },
    { label: '每6小时', value: '0 0 */6 * * ?', description: '适合频繁更新的数据' },
    { label: '每12小时', value: '0 0 */12 * * ?', description: '适合中等频率更新' },
    { label: '每周一早上9点', value: '0 0 9 ? * 1', description: '适合每周汇总' },
    { label: '每月1号凌晨2点', value: '0 0 2 1 * ?', description: '适合每月统计' },
    { label: '每30分钟', value: '0 */30 * * * ?', description: '适合高频数据抓取' },
    { label: '每小时', value: '0 0 */1 * * ?', description: '适合实时性要求高的数据' }
  ];
}

