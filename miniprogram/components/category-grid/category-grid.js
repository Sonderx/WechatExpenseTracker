/**
 * 分类图标网格组件
 * 
 * 功能说明：
 * - 以网格形式展示分类列表
 * - 选中的分类高亮显示
 * - 点击分类触发选择事件
 */
Component({
  properties: {
    categories: {
      type: Array,
      value: []
    },
    selectedId: {
      type: Number,
      value: 0
    }
  },

  methods: {
    /**
     * 点击分类项
     */
    onSelect(e) {
      const id = e.currentTarget.dataset.id;
      this.triggerEvent('select', { id });
    }
  }
});
