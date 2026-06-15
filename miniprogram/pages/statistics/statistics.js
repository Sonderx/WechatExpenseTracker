/**
 * 统计页 - 柱状图 + 饼图
 * 
 * 功能说明：
 * - 顶部显示当月收支概览
 * - 柱状图：年度月度收支对比
 * - 饼图：当月分类支出占比
 * - 支持月份切换
 */
const api = require('../../utils/api');
const util = require('../../utils/util');

Page({
  data: {
    currentMonth: '',       // 当前月份 YYYY-MM
    income: 0,              // 当月收入
    expense: 0,             // 当月支出
    balance: 0,             // 结余
    monthlyData: [],        // 年度月度数据（柱状图）
    categoryData: [],       // 分类支出数据（饼图）
    canvasWidth: 0,         // 画布宽度
    canvasHeight: 0         // 画布高度
  },

  onLoad() {
    this.setData({ currentMonth: util.getCurrentMonth() });
    this.loadData();
  },

  onShow() {
    this.loadData();
  },

  /**
   * 加载所有统计数据
   */
  loadData() {
    this.loadOverview();
    this.loadMonthlyData();
    this.loadCategoryData();
  },

  /**
   * 加载月度概览
   */
  loadOverview() {
    api.get('/api/statistics/overview', { month: this.data.currentMonth })
      .then(data => {
        this.setData({
          income: data.income || 0,
          expense: data.expense || 0,
          balance: data.balance || 0
        });
      });
  },

  /**
   * 加载年度月度数据（柱状图）
   */
  loadMonthlyData() {
    const year = this.data.currentMonth.substring(0, 4);
    api.get('/api/statistics/monthly', { year: parseInt(year) })
      .then(data => {
        this.setData({ monthlyData: data || [] });
        this.drawBarChart();
      });
  },

  /**
   * 加载分类支出数据（饼图）
   */
  loadCategoryData() {
    api.get('/api/statistics/category', { month: this.data.currentMonth })
      .then(data => {
        this.setData({ categoryData: data || [] });
        this.drawPieChart();
      });
  },

  /**
   * 绘制柱状图（简化版）
   */
  drawBarChart() {
    // 使用 Canvas 2D 绘制柱状图
    const query = wx.createSelectorQuery();
    query.select('#barCanvas')
      .fields({ node: true, size: true })
      .exec((res) => {
        if (!res[0]) return;
        const canvas = res[0].node;
        const ctx = canvas.getContext('2d');
        const dpr = wx.getWindowInfo().pixelRatio;
        canvas.width = res[0].width * dpr;
        canvas.height = res[0].height * dpr;
        ctx.scale(dpr, dpr);

        const width = res[0].width;
        const height = res[0].height;
        const data = this.data.monthlyData;
        
        if (!data.length) return;

        // 清空画布
        ctx.clearRect(0, 0, width, height);

        // 计算最大值
        const maxVal = Math.max(
          ...data.map(d => Math.abs(d.income || 0)),
          ...data.map(d => Math.abs(d.expense || 0))
        );
        if (maxVal === 0) return;

        // 绘制柱状图
        const barWidth = (width - 60) / data.length / 2 - 5;
        const chartHeight = height - 60;

        data.forEach((item, index) => {
          const x = 40 + index * (width - 60) / data.length;
          
          // 收入柱（绿色）
          const incomeHeight = (Math.abs(item.income || 0) / maxVal) * chartHeight;
          ctx.fillStyle = '#1AAD19';
          ctx.fillRect(x, chartHeight - incomeHeight + 20, barWidth, incomeHeight);
          
          // 支出柱（红色）
          const expenseHeight = (Math.abs(item.expense || 0) / maxVal) * chartHeight;
          ctx.fillStyle = '#EE5A5A';
          ctx.fillRect(x + barWidth + 5, chartHeight - expenseHeight + 20, barWidth, expenseHeight);
          
          // 月份标签
          ctx.fillStyle = '#999';
          ctx.font = '10px sans-serif';
          ctx.textAlign = 'center';
          ctx.fillText(item.yearMonth ? item.yearMonth.substring(5) : '', x + barWidth, height - 5);
        });
      });
  },

  /**
   * 绘制饼图（简化版）
   */
  drawPieChart() {
    const query = wx.createSelectorQuery();
    query.select('#pieCanvas')
      .fields({ node: true, size: true })
      .exec((res) => {
        if (!res[0]) return;
        const canvas = res[0].node;
        const ctx = canvas.getContext('2d');
        const dpr = wx.getWindowInfo().pixelRatio;
        canvas.width = res[0].width * dpr;
        canvas.height = res[0].height * dpr;
        ctx.scale(dpr, dpr);

        const width = res[0].width;
        const height = res[0].height;
        const data = this.data.categoryData;
        
        if (!data.length) return;

        // 清空画布
        ctx.clearRect(0, 0, width, height);

        // 计算总和
        const total = data.reduce((sum, item) => sum + Math.abs(item.total || 0), 0);
        if (total === 0) return;

        const centerX = width / 2;
        const centerY = height / 2;
        const radius = Math.min(width, height) / 2 - 20;
        const colors = ['#EE5A5A', '#1AAD19', '#FFB300', '#2196F3', '#9C27B0', '#FF5722', '#607D8B', '#795548', '#E91E63'];
        
        let startAngle = -Math.PI / 2;
        data.forEach((item, index) => {
          const sliceAngle = (Math.abs(item.total || 0) / total) * 2 * Math.PI;
          
          ctx.beginPath();
          ctx.moveTo(centerX, centerY);
          ctx.arc(centerX, centerY, radius, startAngle, startAngle + sliceAngle);
          ctx.closePath();
          ctx.fillStyle = colors[index % colors.length];
          ctx.fill();
          
          startAngle += sliceAngle;
        });
      });
  },

  /**
   * 月份切换
   */
  onMonthChange(e) {
    this.setData({ currentMonth: e.detail.month });
    this.loadData();
  }
});
