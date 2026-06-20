const echarts = require('../../lib/echarts.min.js');
const { createCanvasAdapter } = require('../../utils/canvas-adapter');

Component({
  properties: {
    canvasId: {
      type: String,
      value: 'ec-canvas'
    }
  },

  data: {
    width: 0,
    height: 0
  },

  lifetimes: {
    ready() {
      this.init();
    }
  },

  methods: {
    init(callback) {
      const query = wx.createSelectorQuery().in(this);
      query.select('.ec-canvas-canvas')
        .fields({ node: true, size: true })
        .exec((res) => {
          if (!res[0]) {
            setTimeout(() => this.init(callback), 100);
            return;
          }
          const canvas = res[0].node;
          const width = res[0].width;
          const height = res[0].height;
          if (width === 0 || height === 0) {
            setTimeout(() => this.init(callback), 100);
            return;
          }
          const dpr = wx.getWindowInfo().pixelRatio;
          canvas.width = width * dpr;
          canvas.height = height * dpr;
          const ctx = canvas.getContext('2d');
          ctx.scale(dpr, dpr);

          // 使用适配器为 Canvas 添加浏览器 API
          const adaptedCanvas = createCanvasAdapter(canvas);

          const chart = echarts.init(adaptedCanvas, null, {
            width: width,
            height: height,
            devicePixelRatio: dpr
          });
          this.chart = chart;
          this.canvas = adaptedCanvas;
          this.triggerEvent('init', { chart });
          if (callback) callback(chart);
        });
    },

    getChart() {
      return this.chart;
    }
  }
});
