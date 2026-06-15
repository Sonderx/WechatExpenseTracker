/**
 * 分类管理页 - 新增/编辑/删除自定义分类
 * 
 * 功能说明：
 * - 显示系统预置分类（不可删除）
 * - 显示用户自定义分类（可编辑/删除）
 * - 支持新增自定义分类
 */
const api = require('../../utils/api');

Page({
  data: {
    type: 0,               // 0=支出，1=收入
    categories: [],        // 分类列表
    showModal: false,      // 是否显示弹窗
    editId: null,          // 编辑的分类ID
    formName: '',          // 分类名称
    formIcon: '📌'         // 分类图标
  },

  onLoad() {
    this.loadCategories();
  },

  /**
   * 切换类型
   */
  switchType(e) {
    const type = parseInt(e.currentTarget.dataset.type);
    this.setData({ type });
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
   * 显示新增弹窗
   */
  showAddModal() {
    this.setData({
      showModal: true,
      editId: null,
      formName: '',
      formIcon: '📌'
    });
  },

  /**
   * 显示编辑弹窗
   */
  showEditModal(e) {
    const item = e.currentTarget.dataset.item;
    this.setData({
      showModal: true,
      editId: item.id,
      formName: item.name,
      formIcon: item.icon
    });
  },

  /**
   * 隐藏弹窗
   */
  hideModal() {
    this.setData({ showModal: false });
  },

  /**
   * 名称输入
   */
  onNameInput(e) {
    this.setData({ formName: e.detail.value });
  },

  /**
   * 图标选择（简化版，使用 emoji）
   */
  selectIcon() {
    const icons = ['🍜', '🚌', '🛒', '🎮', '🏠', '💊', '📚', '🎁', '📌', '💰', '📈', '💵', '💪', '🎬', '✈️', '🐱', '🎵', '⚽', '📱', '🎁'];
    wx.showActionSheet({
      itemList: icons,
      success: (res) => {
        this.setData({ formIcon: icons[res.tapIndex] });
      }
    });
  },

  /**
   * 保存分类
   */
  onSave() {
    const { formName, formIcon, editId, type } = this.data;
    if (!formName.trim()) {
      wx.showToast({ title: '请输入分类名称', icon: 'none' });
      return;
    }

    const body = { name: formName.trim(), type, icon: formIcon };
    const request = editId
      ? api.put('/api/categories/' + editId, body)
      : api.post('/api/categories', body);

    request.then(() => {
      wx.showToast({ title: editId ? '修改成功' : '新增成功' });
      this.hideModal();
      this.loadCategories();
    });
  },

  /**
   * 删除分类
   */
  onDelete(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '确定要删除此分类吗？',
      success: (res) => {
        if (res.confirm) {
          api.del('/api/categories/' + id)
            .then(() => {
              wx.showToast({ title: '删除成功' });
              this.loadCategories();
            });
        }
      }
    });
  }
});
