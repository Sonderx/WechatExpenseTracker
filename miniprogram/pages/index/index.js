/**
 * 首页 - 记录列表 + 月度收支汇总
 * 
 * 功能说明：
 * - 顶部显示当月收支汇总卡片（收入、支出、结余）
 * - 下方为记录列表，按时间倒序
 * - 支持下拉刷新、上拉加载更多
 * - 悬浮"+"按钮跳转记账页
 */
const api = require('../../utils/api');
const auth = require('../../utils/auth');
const util = require('../../utils/util');

Page({
  data: {
    currentMonth: '',       // 当前月份 YYYY-MM
    income: 0,              // 当月收入
    expense: 0,             // 当月支出
    balance: 0,             // 结余
    records: [],            // 记录列表
    page: 1,                // 当前页码
    pageSize: 20,           // 每页条数
    hasMore: true,          // 是否有更多数据
    loading: false          // 加载状态
  },

  /**
   * 页面加载
   */
  onLoad() {
    this.setData({ currentMonth: util.getCurrentMonth() });
  },

  /**
   * 页面显示时刷新数据（等待登录完成）
   */
  onShow() {
    const app = getApp();
    if (app.globalData.loginReady) {
      app.globalData.loginReady.then(() => {
        this.refreshData();
      }).catch(() => {
        console.error('登录未完成，无法加载数据');
      });
    }
  },

  /**
   * 下拉刷新
   */
  onPullDownRefresh() {
    this.refreshData().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  /**
   * 上拉加载更多
   */
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadMore();
    }
  },

  /**
   * 刷新所有数据
   */
  refreshData() {
    this.setData({ page: 1, records: [], hasMore: true });
    return Promise.all([
      this.loadOverview(),
      this.loadRecords()
    ]);
  },

  /**
   * 加载月度收支概览
   */
  loadOverview() {
    return api.get('/api/statistics/overview', { month: this.data.currentMonth })
      .then(data => {
        this.setData({
          income: data.income || 0,
          expense: data.expense || 0,
          balance: data.balance || 0
        });
      })
      .catch(() => {});
  },

  /**
   * 加载记录列表
   */
  loadRecords() {
    this.setData({ loading: true });
    return api.get('/api/records', {
      month: this.data.currentMonth,
      page: this.data.page,
      size: this.data.pageSize
    }).then(data => {
      const newRecords = this.data.page === 1 ? data.records : this.data.records.concat(data.records);
      this.setData({
        records: newRecords,
        hasMore: this.data.page < data.pages,
        loading: false
      });
    }).catch(() => {
      this.setData({ loading: false });
    });
  },

  /**
   * 加载更多记录
   */
  loadMore() {
    this.setData({ page: this.data.page + 1 });
    this.loadRecords();
  },

  /**
   * 点击悬浮按钮，跳转记账页
   */
  goToAdd() {
    wx.navigateTo({ url: '/pages/record/record' });
  },

  /**
   * 点击记录项，跳转编辑页
   */
  onRecordTap(e) {
    const id = e.detail.id;
    wx.navigateTo({ url: '/pages/record/record?id=' + id });
  },

  /**
   * 删除记录
   */
  onRecordDelete(e) {
    const id = e.detail.id;
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条记录吗？',
      success: (res) => {
        if (res.confirm) {
          api.del('/api/records/' + id).then(() => {
            wx.showToast({ title: '删除成功' });
            this.refreshData();
          });
        }
      }
    });
  }
});
