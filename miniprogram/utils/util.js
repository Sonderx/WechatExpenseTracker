/**
 * 通用工具函数
 */

/**
 * 格式化日期为 YYYY-MM-DD 格式
 * @param {Date} date - 日期对象
 * @returns {string} 格式化后的日期字符串
 */
function formatDate(date) {
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/**
 * 格式化日期为 YYYY-MM-DD HH:mm 格式
 * @param {Date} date - 日期对象
 * @returns {string} 格式化后的日期时间字符串
 */
function formatDateTime(date) {
  const dateStr = formatDate(date);
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  return `${dateStr} ${hours}:${minutes}`;
}

/**
 * 获取当前月份（YYYY-MM 格式）
 * @returns {string} 当前月份
 */
function getCurrentMonth() {
  return formatDate(new Date()).substring(0, 7);
}

/**
 * 格式化金额显示
 * @param {number} amount - 金额（负数为支出）
 * @returns {string} 格式化后的金额字符串
 */
function formatAmount(amount) {
  const num = Math.abs(amount);
  return num.toFixed(2);
}

module.exports = { formatDate, formatDateTime, getCurrentMonth, formatAmount };
