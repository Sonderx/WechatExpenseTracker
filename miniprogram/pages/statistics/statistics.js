/**
 * 统计页 - ECharts 图表 v4
 * 单月模式 + 自定义范围模式（精确到日）
 */
const api = require('../../utils/api');
const util = require('../../utils/util');

const COLORS = ['#667EEA', '#00D2D3', '#FF6B6B', '#FFB300', '#9C27B0', '#FF5722', '#607D8B', '#795548', '#E91E63'];

Page({
  data: {
    quickMonths: [
      { month: 1, label: '1月', key: '01' },
      { month: 2, label: '2月', key: '02' },
      { month: 3, label: '3月', key: '03' },
      { month: 4, label: '4月', key: '04' },
      { month: 5, label: '5月', key: '05' },
      { month: 6, label: '6月', key: '06' },
      { month: 7, label: '7月', key: '07' },
      { month: 8, label: '8月', key: '08' },
      { month: 9, label: '9月', key: '09' },
      { month: 10, label: '10月', key: '10' },
      { month: 11, label: '11月', key: '11' },
      { month: 12, label: '12月', key: '12' }
    ],
    currentMonth: '',
    currentMode: 'single',      // 'single' | 'range'

    // 快速跳转
    showQuickJump: false,
    quickYear: new Date().getFullYear(),

    // 自定义范围（精确到日）
    startDate: '',              // YYYY-MM-DD
    endDate: '',                // YYYY-MM-DD
    startDateDisplay: '',
    endDateDisplay: '',

    // 统计粒度（范围模式下自动判断）
    rangeGranularity: 'day',    // 'day' | 'week' | 'month'

    // 数据
    income: 0,
    expense: 0,
    balance: 0,
    timeSeriesData: [],         // 时间序列数据（单月=月度，范围=按日）
    timeSeriesXLabel: '',       // 横轴标签
    categoryData: [],
    displayLabel: '',
    displaySubLabel: ''
  },

  _barChart: null,
  _pieChart: null,
  _barInited: false,
  _pieInited: false,

  onLoad() {
    const now = new Date();
    const currentMonth = util.getCurrentMonth();
    const today = util.formatDate(now);
    this.setData({
      currentMonth: currentMonth,
      quickYear: now.getFullYear(),
      startDate: today,
      endDate: today,
      displayLabel: currentMonth
    });
  },

  onShow() {
    this.loadData();
  },

  // ========== 图表初始化 ==========

  onBarChartInit(e) {
    this._barChart = e.detail.chart;
    this._barInited = true;
    this.renderBarChart();
  },

  onPieChartInit(e) {
    this._pieChart = e.detail.chart;
    this._pieInited = true;
    this.renderPieChart();
  },

  // ========== 数据加载 ==========

  loadData() {
    if (this.data.currentMode === 'single') {
      this.loadSingleMonthData();
    } else {
      this.loadRangeData();
    }
  },

  // ---------- 单月模式 ----------

  loadSingleMonthData() {
    this.setData({ displayLabel: this.data.currentMonth + ' 月统计', displaySubLabel: '' });
    Promise.all([
      this.loadOverview(),
      this.loadMonthlyData(),
      this.loadCategoryData()
    ]);
  },

  loadOverview() {
    return api.get('/api/statistics/overview', { month: this.data.currentMonth })
      .then(data => {
        this.setData({
          income: data.income || 0,
          expense: data.expense || 0,
          balance: data.balance || 0
        });
      }).catch(() => {});
  },

  loadMonthlyData() {
    const year = this.data.currentMonth.substring(0, 4);
    return api.get('/api/statistics/monthly', { year: parseInt(year) })
      .then(data => {
        const timeSeriesData = (data || []).map(item => ({
          ...item,
          label: item.yearMonth ? item.yearMonth.substring(5) + '月' : ''
        }));
        this.setData({ timeSeriesData, timeSeriesXLabel: '月份', rangeGranularity: 'month' });
        this.renderBarChart();
      }).catch(() => {});
  },

  loadCategoryData() {
    return api.get('/api/statistics/category', { month: this.data.currentMonth })
      .then(data => {
        const total = (data || []).reduce((sum, item) => sum + Math.abs(item.total || 0), 0);
        const categoryData = (data || []).map((item, index) => ({
          ...item,
          color: COLORS[index % COLORS.length],
          percent: total > 0 ? Math.round((Math.abs(item.total || 0) / total) * 100) : 0
        }));
        this.setData({ categoryData });
        this.renderPieChart();
      }).catch(() => {});
  },

  // ---------- 自定义范围模式（精确到日）----------

  loadRangeData() {
    const { startDate, endDate } = this.data;
    const days = this.getDaysDiff(startDate, endDate);
    let granularity = 'day';
    if (days > 90) granularity = 'month';
    else if (days > 31) granularity = 'week';

    const fmt = d => d.replace(/-/g, '/');
    this.setData({
      displayLabel: fmt(startDate) + ' ~ ' + fmt(endDate),
      displaySubLabel: days + ' 天',
      rangeGranularity: granularity
    });

    // 1. 拉取覆盖月份的分类数据，按日聚合
    this.loadRangeAggregatedData(startDate, endDate, granularity);
  },

  /**
   * 加载范围统计：通过 records API 拉取所有覆盖月份的数据，按日聚合
   */
  loadRangeAggregatedData(startDate, endDate, granularity) {
    const months = this.getMonthsCovering(startDate, endDate);
    const promises = months.map(m =>
      api.get('/api/records', { month: m, size: 500 }).catch(() => ({ records: [] }))
    );

    Promise.all(promises).then(results => {
      // 合并所有记录
      let allRecords = [];
      results.forEach(r => {
        if (r && r.records) allRecords = allRecords.concat(r.records);
      });

      // 按日期范围过滤
      const filtered = allRecords.filter(rec => {
        const d = this.getRecordDate(rec);
        return d >= startDate && d <= endDate;
      });

      // 计算总览
      let totalIncome = 0, totalExpense = 0;
      filtered.forEach(r => {
        if (r.amount > 0) totalIncome += r.amount;
        else totalExpense += Math.abs(r.amount);
      });
      this.setData({
        income: totalIncome,
        expense: totalExpense,
        balance: totalIncome - totalExpense
      });

      // 按粒度聚合时间序列
      const timeSeries = this.aggregateByTime(filtered, startDate, endDate, granularity);
      this.setData({
        timeSeriesData: timeSeries,
        timeSeriesXLabel: granularity === 'day' ? '日期' : (granularity === 'week' ? '周' : '月份')
      });
      this.renderBarChart();

      // 按分类聚合
      const categoryMap = {};
      filtered.forEach(r => {
        const key = r.categoryName || r.categoryId || '未分类';
        if (!categoryMap[key]) {
          categoryMap[key] = {
            categoryName: key,
            categoryIcon: r.categoryIcon || '',
            categoryId: r.categoryId,
            total: 0
          };
        }
        categoryMap[key].total += Math.abs(r.amount);
      });
      let categoryData = Object.values(categoryMap);
      const grandTotal = categoryData.reduce((sum, item) => sum + Math.abs(item.total || 0), 0);
      categoryData.forEach((item, index) => {
        item.color = COLORS[index % COLORS.length];
        item.percent = grandTotal > 0 ? Math.round((Math.abs(item.total || 0) / grandTotal) * 100) : 0;
      });
      categoryData.sort((a, b) => Math.abs(b.total) - Math.abs(a.total));
      this.setData({ categoryData });
      this.renderPieChart();

    }).catch(() => {
      // fallback: 使用月度汇总 API
      this.loadRangeByMonthlyApis(startDate, endDate);
    });
  },

  /**
   * 备用方案：通过月度统计 API 聚合
   */
  loadRangeByMonthlyApis(startDate, endDate) {
    const months = this.getMonthsCovering(startDate, endDate);
    const overviews = months.map(m =>
      api.get('/api/statistics/overview', { month: m }).catch(() => ({}))
    );
    const categories = months.map(m =>
      api.get('/api/statistics/category', { month: m }).catch(() => [])
    );

    Promise.all([
      Promise.all(overviews),
      Promise.all(categories)
    ]).then(([ovResults, catResults]) => {
      let income = 0, expense = 0, balance = 0;
      ovResults.forEach(r => {
        income += Math.abs(r.income || 0);
        expense += Math.abs(r.expense || 0);
        balance += r.balance || 0;
      });
      this.setData({ income, expense, balance });

      // 合并分类
      const merged = {};
      catResults.forEach(monthData => {
        (monthData || []).forEach(item => {
          const key = item.categoryName || item.categoryId;
          if (!merged[key]) merged[key] = { ...item, total: 0 };
          merged[key].total += Math.abs(item.total || 0);
        });
      });
      let categoryData = Object.values(merged);
      const total = categoryData.reduce((s, i) => s + Math.abs(i.total || 0), 0);
      categoryData.forEach((item, index) => {
        item.color = COLORS[index % COLORS.length];
        item.percent = total > 0 ? Math.round((Math.abs(item.total || 0) / total) * 100) : 0;
      });
      categoryData.sort((a, b) => Math.abs(b.total) - Math.abs(a.total));
      this.setData({ categoryData });
      this.renderPieChart();

      // 时间序列：用月度数据
      const timeSeriesData = months.map((m, i) => ({
        label: m.substring(5) + '月',
        income: ovResults[i] ? Math.abs(ovResults[i].income || 0) : 0,
        expense: ovResults[i] ? Math.abs(ovResults[i].expense || 0) : 0
      }));
      this.setData({ timeSeriesData, timeSeriesXLabel: '月份', rangeGranularity: 'month' });
      this.renderBarChart();
    }).catch(() => {});
  },

  // ========== 数据聚合工具 ==========

  /**
   * 按粒度聚合时间序列
   */
  aggregateByTime(records, startDate, endDate, granularity) {
    const map = {};
    records.forEach(r => {
      const d = this.getRecordDate(r);
      let key;
      if (granularity === 'day') {
        key = d.substring(5); // MM-DD
      } else if (granularity === 'week') {
        const date = new Date(d);
        const dayOfWeek = date.getDay() || 7;
        const monday = new Date(date);
        monday.setDate(date.getDate() - dayOfWeek + 1);
        key = util.formatDate(monday).substring(5); // "MM-DD(周一)"
      } else {
        key = d.substring(0, 7); // YYYY-MM
      }

      if (!map[key]) map[key] = { label: key, income: 0, expense: 0 };
      if (r.amount > 0) map[key].income += r.amount;
      else map[key].expense += Math.abs(r.amount);
    });

    // 补充范围中缺失的日期（显示为0）
    if (granularity === 'day') {
      let cursor = new Date(startDate);
      const end = new Date(endDate);
      while (cursor <= end) {
        const d = util.formatDate(cursor).substring(5);
        if (!map[d]) map[d] = { label: d, income: 0, expense: 0 };
        cursor.setDate(cursor.getDate() + 1);
      }
    }

    // 按 label 排序
    return Object.values(map).sort((a, b) => a.label.localeCompare(b.label));
  },

  /**
   * 获取记录日期（提取 recordTime 的日期部分）
   */
  getRecordDate(record) {
    return (record.recordTime || '').split(' ')[0];
  },

  /**
   * 获取覆盖日期范围的所有月份
   */
  getMonthsCovering(startDate, endDate) {
    const months = new Set();
    const start = new Date(startDate);
    const end = new Date(endDate);
    const cursor = new Date(start.getFullYear(), start.getMonth(), 1);
    while (cursor <= end) {
      months.add(cursor.getFullYear() + '-' + (cursor.getMonth() + 1).toString().padStart(2, '0'));
      cursor.setMonth(cursor.getMonth() + 1);
    }
    return Array.from(months);
  },

  /**
   * 计算两个日期间隔天数
   */
  getDaysDiff(startDate, endDate) {
    const s = new Date(startDate);
    const e = new Date(endDate);
    return Math.round((e - s) / 86400000);
  },

  // ========== 图表渲染 ==========

  renderBarChart() {
    if (!this._barChart || !this._barInited) return;
    const data = this.data.timeSeriesData;
    if (!data.length) return;

    const labels = data.map(d => d.label);
    const incomeData = data.map(d => Math.abs(d.income || 0));
    const expenseData = data.map(d => Math.abs(d.expense || 0));

    // 范围模式下，如果 label 太多，每 N 个显示一个
    let axisLabels = labels;
    if (labels.length > 31) {
      const step = Math.ceil(labels.length / 12);
      axisLabels = labels.map((l, i) => i % step === 0 ? l : '');
    }

    this._barChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        backgroundColor: 'rgba(26, 31, 54, 0.9)',
        borderColor: 'transparent',
        textStyle: { color: '#fff', fontSize: 12 }
      },
      legend: {
        data: ['收入', '支出'],
        top: 0,
        textStyle: { color: '#8792A2', fontSize: 12 }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: 40,
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: axisLabels,
        axisLine: { lineStyle: { color: '#EDF0F7' } },
        axisTick: { show: false },
        axisLabel: { color: '#8792A2', fontSize: 10 }
      },
      yAxis: {
        type: 'value',
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { lineStyle: { color: '#EDF0F7', type: 'dashed' } },
        axisLabel: { color: '#8792A2', fontSize: 10 }
      },
      series: [
        {
          name: '收入',
          type: 'bar',
          barWidth: labels.length > 20 ? '60%' : '30%',
          itemStyle: { color: '#00D2D3', borderRadius: [4, 4, 0, 0] },
          data: incomeData
        },
        {
          name: '支出',
          type: 'bar',
          barWidth: labels.length > 20 ? '60%' : '30%',
          itemStyle: { color: '#FF6B6B', borderRadius: [4, 4, 0, 0] },
          data: expenseData
        }
      ]
    });
  },

  renderPieChart() {
    if (!this._pieChart || !this._pieInited) return;
    const data = this.data.categoryData;
    if (!data.length) return;

    const pieData = data.map(item => ({
      name: item.categoryName,
      value: Math.abs(item.total || 0),
      itemStyle: { color: item.color }
    }));

    this._pieChart.setOption({
      tooltip: {
        trigger: 'item',
        backgroundColor: 'rgba(26, 31, 54, 0.9)',
        borderColor: 'transparent',
        textStyle: { color: '#fff', fontSize: 12 },
        formatter: '{b}: ¥{c} ({d}%)'
      },
      series: [{
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 14, fontWeight: 'bold' }
        },
        data: pieData
      }]
    });
  },

  // ========== 月份选择 ==========

  onMonthChange(e) {
    this.setData({
      currentMonth: e.detail.month,
      currentMode: 'single'
    });
    this.loadData();
  },

  toggleQuickJump() {
    this.setData({ showQuickJump: !this.data.showQuickJump });
  },

  onQuickYearPrev() {
    this.setData({ quickYear: this.data.quickYear - 1 });
  },

  onQuickYearNext() {
    this.setData({ quickYear: this.data.quickYear + 1 });
  },

  onQuickMonthTap(e) {
    const month = e.currentTarget.dataset.month;
    const yearMonth = this.data.quickYear + '-' + month.toString().padStart(2, '0');
    this.setData({
      currentMonth: yearMonth,
      currentMode: 'single',
      showQuickJump: false
    });
    this.loadData();
  },

  // ========== 模式切换 ==========

  onModeSingle() {
    this.setData({ currentMode: 'single' });
    this.loadData();
  },

  onModeRange() {
    const today = util.formatDate(new Date());
    this.setData({
      currentMode: 'range',
      startDate: this.data.startDate || today,
      endDate: this.data.endDate || today
    });
    this.loadData();
  },

  /**
   * 选择开始日期（精确到日）
   */
  onStartDateChange(e) {
    this.setData({ startDate: e.detail.value });
  },

  /**
   * 选择结束日期（精确到日）
   */
  onEndDateChange(e) {
    this.setData({ endDate: e.detail.value });
  },

  /**
   * 应用自定义范围
   */
  onApplyRange() {
    const { startDate, endDate } = this.data;
    if (!startDate || !endDate) {
      wx.showToast({ title: '请选择起止日期', icon: 'none' });
      return;
    }
    if (startDate > endDate) {
      wx.showToast({ title: '开始日期不能晚于结束日期', icon: 'none' });
      return;
    }
    this.loadData();
    wx.showToast({ title: '统计范围已更新', icon: 'none' });
  },

  /**
   * 快捷范围：最近7天
   */
  onQuickRange7() {
    const end = new Date();
    const start = new Date(end.getTime() - 6 * 86400000);
    this.setDateRange(util.formatDate(start), util.formatDate(end));
    this.loadData();
  },

  /**
   * 快捷范围：最近30天
   */
  onQuickRange30() {
    const end = new Date();
    const start = new Date(end.getTime() - 29 * 86400000);
    this.setDateRange(util.formatDate(start), util.formatDate(end));
    this.loadData();
  },

  /**
   * 快捷范围：本月
   */
  onQuickRangeMonth() {
    const now = new Date();
    const start = new Date(now.getFullYear(), now.getMonth(), 1);
    this.setDateRange(util.formatDate(start), util.formatDate(now));
    this.loadData();
  },

  setDateRange(start, end) {
    this.setData({ startDate: start, endDate: end });
  }
});
