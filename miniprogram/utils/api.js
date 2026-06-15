/**
 * API 请求封装
 * 
 * 功能说明：
 * - 统一封装 wx.request，自动携带 JWT Token
 * - 统一处理响应格式和错误
 * - 支持 GET、POST、PUT、DELETE 请求
 */

/**
 * 发送 API 请求
 * @param {string} url - API 路径（如 /api/records）
 * @param {string} method - HTTP 方法
 * @param {object} data - 请求参数
 * @returns {Promise} 返回响应数据
 */
function request(url, method = 'GET', data = {}) {
  const app = getApp();  // 每次请求时获取，避免模块加载时 App 未初始化
  return new Promise((resolve, reject) => {
    wx.request({
      url: app.globalData.baseUrl + url,
      method: method,
      data: data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + app.globalData.token
      },
      success(res) {
        if (res.statusCode === 200) {
          const result = res.data;
          if (result.code === 0) {
            resolve(result.data);
          } else {
            wx.showToast({ title: result.msg || '请求失败', icon: 'none' });
            reject(result);
          }
        } else if (res.statusCode === 401) {
          // Token 过期，跳转登录
          wx.removeStorageSync('token');
          wx.removeStorageSync('userId');
          app.globalData.token = '';
          app.globalData.userId = null;
          wx.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
          reject(res);
        } else {
          wx.showToast({ title: '服务器错误', icon: 'none' });
          reject(res);
        }
      },
      fail(err) {
        wx.showToast({ title: '网络错误', icon: 'none' });
        reject(err);
      }
    });
  });
}

// GET 请求
function get(url, data) {
  return request(url, 'GET', data);
}

// POST 请求
function post(url, data) {
  return request(url, 'POST', data);
}

// PUT 请求
function put(url, data) {
  return request(url, 'PUT', data);
}

// DELETE 请求
function del(url, data) {
  return request(url, 'DELETE', data);
}

module.exports = { request, get, post, put, del };
