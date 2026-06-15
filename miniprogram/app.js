const config = require('./config');

App({
  globalData: {
    baseUrl: config.baseUrl,
    token: '',
    userId: null,
    nickname: '',
    avatarUrl: '',
    loginReady: null
  },

  onLaunch() {
    // 恢复本地缓存的用户信息
    this.globalData.nickname = wx.getStorageSync('nickname') || '';
    this.globalData.avatarUrl = wx.getStorageSync('avatarUrl') || '';

    // 清除旧 Token，重新登录
    wx.removeStorageSync('token');
    wx.removeStorageSync('userId');
    this.globalData.token = '';
    this.globalData.userId = null;

    // 延迟登录
    setTimeout(() => {
      const auth = require('./utils/auth');
      this.globalData.loginReady = auth.login()
        .then(data => {
          console.log('登录成功, userId:', data.userId);
          return data;
        })
        .catch(err => {
          console.error('登录失败:', err);
        });
    }, 100);
  }
});
