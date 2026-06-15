const api = require('./api');

/**
 * 微信登录
 * 1. wx.login() 获取 code
 * 2. 尝试 wx.getUserInfo() 获取头像和昵称
 * 3. 发送到后端换取 Token
 * 4. 保存用户信息到本地和全局
 */
function login() {
  return new Promise((resolve, reject) => {
    const app = getApp();

    wx.login({
      success(loginRes) {
        if (!loginRes.code) {
          reject(loginRes);
          return;
        }

        // 尝试获取微信用户信息（头像、昵称）
        let userInfo = null;
        try {
          // wx.getUserInfo 在部分版本仍可获取基础信息
          const res = wx.getUserInfo && wx.getUserInfo();
          if (res && res.userInfo) {
            userInfo = res.userInfo;
          }
        } catch (e) {
          console.log('wx.getUserInfo 不可用，需手动设置');
        }

        // 构造登录参数
        const loginData = { code: loginRes.code };
        if (userInfo) {
          loginData.nickname = userInfo.nickName || '';
          loginData.avatarUrl = userInfo.avatarUrl || '';
        }

        // 发送到后端
        api.post('/api/auth/login', loginData)
          .then(data => {
            // 保存 Token
            wx.setStorageSync('token', data.token);
            wx.setStorageSync('userId', data.userId);
            app.globalData.token = data.token;
            app.globalData.userId = data.userId;

            // 保存用户信息（后端返回的优先，其次本地获取的）
            const nickname = data.nickname || (userInfo ? userInfo.nickName : '') || '';
            const avatarUrl = data.avatarUrl || (userInfo ? userInfo.avatarUrl : '') || '';

            if (nickname) {
              wx.setStorageSync('nickname', nickname);
              app.globalData.nickname = nickname;
            }
            if (avatarUrl) {
              wx.setStorageSync('avatarUrl', avatarUrl);
              app.globalData.avatarUrl = avatarUrl;
            }

            // 如果本地有新信息，同步到后端
            if (userInfo && (userInfo.nickName || userInfo.avatarUrl)) {
              api.post('/api/auth/updateProfile', {
                nickname: userInfo.nickName || '',
                avatarUrl: userInfo.avatarUrl || ''
              }).catch(() => {});
            }

            resolve(data);
          })
          .catch(err => {
            console.error('登录失败:', err);
            reject(err);
          });
      },
      fail(err) {
        console.error('wx.login 失败:', err);
        reject(err);
      }
    });
  });
}

function isLoggedIn() {
  return !!getApp().globalData.token;
}

function logout() {
  const app = getApp();
  wx.removeStorageSync('token');
  wx.removeStorageSync('userId');
  wx.removeStorageSync('nickname');
  wx.removeStorageSync('avatarUrl');
  app.globalData.token = '';
  app.globalData.userId = null;
  app.globalData.nickname = '';
  app.globalData.avatarUrl = '';
}

module.exports = { login, isLoggedIn, logout };
