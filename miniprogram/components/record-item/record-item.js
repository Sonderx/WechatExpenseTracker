/**
 * 记录列表项组件
 * 
 * 功能说明：
 * - 显示单条账目记录
 * - 显示分类图标、金额、备注、时间
 * - 支持点击编辑、左滑删除
 */
Component({
  properties: {
    record: {
      type: Object,
      value: {}
    }
  },

  data: {
    startX: 0,      // 触摸起始X坐标
    moveX: 0,       // 滑动距离
    isSliding: false // 是否正在滑动
  },

  methods: {
    /**
     * 点击记录项
     */
    onTap() {
      this.triggerEvent('tap', { id: this.data.record.id });
    },

    /**
     * 触摸开始
     */
    onTouchStart(e) {
      this.setData({
        startX: e.touches[0].clientX
      });
    },

    /**
     * 触摸移动
     */
    onTouchMove(e) {
      const moveX = e.touches[0].clientX - this.data.startX;
      if (moveX < 0) {
        this.setData({ moveX: Math.max(moveX, -160) });
      }
    },

    /**
     * 触摸结束
     */
    onTouchEnd() {
      const moveX = this.data.moveX;
      if (moveX < -80) {
        this.setData({ isSliding: true });
      } else {
        this.setData({ isSliding: false, moveX: 0 });
      }
    },

    /**
     * 删除按钮点击
     */
    onDelete() {
      this.triggerEvent('delete', { id: this.data.record.id });
      this.setData({ isSliding: false, moveX: 0 });
    }
  }
});
