/**
 * 记账页 - 新建/编辑记录
 * 
 * 功能说明：
 * - 收入/支出类型切换
 * - 金额输入
 * - 分类网格选择
 * - 日期时间选择
 * - 备注输入
 * - 支持编辑模式（传入 id 参数）
 */
const api = require('../../utils/api');
const util = require('../../utils/util');

Page({
  data: {
    type: 0,               // 0=支出，1=收入
    amount: '',            // 金额
    categoryId: 0,         // 选中的分类ID
    categories: [],        // 分类列表
    date: '',              // 日期
    time: '',              // 时间
    remark: '',            // 备注
    isEdit: false,         // 是否编辑模式
    recordId: null         // 编辑的记录ID
  },

  /**
   * 页面加载
   * 如果有 id 参数则为编辑模式
   */
  onLoad(options) {
    const now = new Date();
    this.setData({
      date: util.formatDate(now),
      time: now.getHours().toString().padStart(2, '0') + ':' + now.getMinutes().toString().padStart(2, '0')
    });

    if (options.id) {
      this.setData({ isEdit: true, recordId: parseInt(options.id) });
      this.loadRecord(options.id);
    }
    
    this.loadCategories();
  },

  /**
   * 加载分类列表
   */
  loadCategories() {
    api.get('/api/categories', { type: this.data.type })
      .then(data => {
        this.setData({ categories: data });
      });
  },

  /**
   * 切换收入/支出类型
   */
  switchType(e) {
    const type = parseInt(e.currentTarget.dataset.type);
    this.setData({ type, categoryId: 0 });
    this.loadCategories();
  },

  /**
   * 金额输入
   */
  onAmountInput(e) {
    this.setData({ amount: e.detail.value });
  },

  /**
   * 选择分类
   */
  onCategorySelect(e) {
    this.setData({ categoryId: e.detail.id });
  },

  /**
   * 日期选择
   */
  onDateChange(e) {
    this.setData({ date: e.detail.value });
  },

  /**
   * 时间选择
   */
  onTimeChange(e) {
    this.setData({ time: e.detail.value });
  },

  /**
   * 备注输入
   */
  onRemarkInput(e) {
    this.setData({ remark: e.detail.value });
  },

  /**
   * 加载记录（编辑模式）
   */
  loadRecord(id) {
    // 通过查询接口获取记录详情
    // 由于没有单条查询接口，这里从列表中查找
    const month = this.data.date.substring(0, 7);
    api.get('/api/records', { month, page: 1, size: 100 })
      .then(data => {
        const record = data.records.find(r => r.id === parseInt(id));
        if (record) {
          this.setData({
            type: record.amount < 0 ? 0 : 1,
            amount: Math.abs(record.amount).toString(),
            categoryId: record.categoryId,
            date: record.recordTime.substring(0, 10),
            time: record.recordTime.substring(11, 16),
            remark: record.remark
          });
          this.loadCategories();
        }
      });
  },

  /**
   * 提交表单
   */
  onSubmit() {
    const { type, amount, categoryId, date, time, remark, isEdit, recordId } = this.data;

    // 校验
    if (!amount || parseFloat(amount) <= 0) {
      wx.showToast({ title: '请输入金额', icon: 'none' });
      return;
    }
    if (!categoryId) {
      wx.showToast({ title: '请选择分类', icon: 'none' });
      return;
    }

    const recordTime = date + 'T' + time + ':00';
    const body = {
      amount: parseFloat(amount),
      type: type,
      categoryId: categoryId,
      recordTime: recordTime,
      remark: remark
    };

    const request = isEdit
      ? api.put('/api/records/' + recordId, body)
      : api.post('/api/records', body);

    request.then(() => {
      wx.showToast({ title: isEdit ? '修改成功' : '记账成功' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    });
  }
});
