const api = require('../../utils/api');

Page({
  data: {
    nickname: '微信用户',
    avatarUrl: '',
    userId: ''
  },

  onShow() {
    this.loadUserInfo();
  },

  /**
   * 加载用户信息
   * 优先从后端获取，回退到本地缓存
   */
  loadUserInfo() {
    const app = getApp();
    
    // 先显示本地缓存（快速反馈）
    const localNickname = wx.getStorageSync('nickname') || '';
    const localAvatar = wx.getStorageSync('avatarUrl') || '';
    const localUserId = wx.getStorageSync('userId') || '';
    if (localNickname || localAvatar) {
      this.setData({
        nickname: localNickname || '微信用户',
        avatarUrl: localAvatar,
        userId: localUserId
      });
    }

    // 从后端获取最新数据
    if (!app.globalData.token) return;

    api.get('/api/auth/profile').then(data => {
      const nickname = data.nickname || '';
      const avatarUrl = data.avatarUrl || '';
      const userId = data.userId || app.globalData.userId || '';
      this.setData({
        nickname: nickname || '微信用户',
        avatarUrl: avatarUrl,
        userId: userId
      });
      // 同步到本地缓存
      wx.setStorageSync('nickname', nickname);
      wx.setStorageSync('avatarUrl', avatarUrl);
      wx.setStorageSync('userId', userId);
    }).catch(() => {});
  },

  /**
   * 选择头像并上传
   */
  onChooseAvatar(e) {
    const tempFilePath = e.detail.avatarUrl;
    if (!tempFilePath) return;

    // 先显示本地临时路径
    this.setData({ avatarUrl: tempFilePath });

    const app = getApp();
    if (!app.globalData.token) return;

    wx.showLoading({ title: '上传中...' });
    wx.uploadFile({
      url: app.globalData.baseUrl + '/api/auth/uploadAvatar',
      filePath: tempFilePath,
      name: 'file',
      header: { 'Authorization': 'Bearer ' + app.globalData.token },
      success: (res) => {
        wx.hideLoading();
        const data = JSON.parse(res.data);
        if (data.code === 0) {
          const avatarUrl = data.data.avatarUrl;
          this.setData({ avatarUrl });
          wx.setStorageSync('avatarUrl', avatarUrl);
          wx.showToast({ title: '头像更新成功' });
        } else {
          wx.showToast({ title: '上传失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '上传失败', icon: 'none' });
      }
    });
  },

  /**
   * 昵称输入完成
   */
  onNicknameInput(e) {
    const nickname = e.detail.value;
    if (nickname) {
      this.setData({ nickname });
      wx.setStorageSync('nickname', nickname);
      api.post('/api/auth/updateProfile', { nickname })
        .then(() => wx.showToast({ title: '昵称更新成功' }))
        .catch(() => {});
    }
  },

  goCategoryManage() {
    wx.navigateTo({ url: '/pages/category-manage/category-manage' });
  },

  goBudgetSetting() {
    wx.showModal({
      title: '设置月度预算',
      editable: true,
      placeholderText: '请输入预算金额',
      success: (res) => {
        if (res.confirm && res.content) {
          const amount = parseFloat(res.content);
          if (isNaN(amount) || amount <= 0) {
            wx.showToast({ title: '请输入有效金额', icon: 'none' });
            return;
          }
          const month = new Date().toISOString().substring(0, 7);
          api.post('/api/budget', { yearMonth: month, amount })
            .then(() => wx.showToast({ title: '预算设置成功' }));
        }
      }
    });
  },

  about() {
    wx.showModal({ title: '关于', content: '日常记账助手 v1.0', showCancel: false });
  }
});
