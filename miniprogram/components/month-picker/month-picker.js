/**
 * 月份选择器组件
 * 
 * 功能说明：
 * - 显示当前月份
 * - 左右箭头切换月份
 * - 触发月份变更事件
 */
Component({
  properties: {
    currentMonth: {
      type: String,
      value: ''
    }
  },

  methods: {
    /**
     * 上一个月
     */
    prevMonth() {
      const month = this.data.currentMonth;
      const [year, mon] = month.split('-').map(Number);
      let newYear = year;
      let newMon = mon - 1;
      if (newMon < 1) {
        newMon = 12;
        newYear--;
      }
      const newMonth = `${newYear}-${newMon.toString().padStart(2, '0')}`;
      this.triggerEvent('change', { month: newMonth });
    },

    /**
     * 下一个月
     */
    nextMonth() {
      const month = this.data.currentMonth;
      const [year, mon] = month.split('-').map(Number);
      let newYear = year;
      let newMon = mon + 1;
      if (newMon > 12) {
        newMon = 1;
        newYear++;
      }
      const newMonth = `${newYear}-${newMon.toString().padStart(2, '0')}`;
      this.triggerEvent('change', { month: newMonth });
    }
  }
});
