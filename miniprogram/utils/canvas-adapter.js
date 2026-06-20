/**
 * Canvas 适配器 - 为微信小程序 Canvas 2D 添加浏览器 API
 *
 * ECharts 需要以下浏览器 API，但微信小程序 Canvas 2D 不支持：
 * - addEventListener / removeEventListener
 * - style 属性
 * - ownerDocument 引用
 */

/**
 * 创建 Canvas 适配器
 * @param {Object} canvas - 微信小程序 Canvas 2D 节点
 * @returns {Object} - 添加了浏览器 API 的 Canvas 节点
 */
function createCanvasAdapter(canvas) {
  // 存储事件监听器
  if (!canvas._listeners) {
    canvas._listeners = {};
  }

  // 添加 addEventListener 方法
  canvas.addEventListener = function (type, listener) {
    if (!this._listeners[type]) {
      this._listeners[type] = [];
    }
    this._listeners[type].push(listener);
  };

  // 添加 removeEventListener 方法
  canvas.removeEventListener = function (type, listener) {
    if (!this._listeners[type]) return;
    const index = this._listeners[type].indexOf(listener);
    if (index > -1) {
      this._listeners[type].splice(index, 1);
    }
  };

  // 添加 dispatchEvent 方法
  canvas.dispatchEvent = function (event) {
    if (!this._listeners[event.type]) return;
    this._listeners[event.type].forEach(listener => listener(event));
  };

  // 添加 style 属性（模拟 CSSStyleDeclaration）
  canvas.style = {
    width: canvas.width + 'px',
    height: canvas.height + 'px',
    setProperty: function (prop, value) {
      this[prop] = value;
    },
    getPropertyValue: function (prop) {
      return this[prop] || '';
    }
  };

  // 添加 ownerDocument 引用
  canvas.ownerDocument = {
    createElement: function (tagName) {
      if (tagName === 'canvas') {
        return canvas;
      }
      // 返回一个简单的 DOM 元素模拟
      return {
        style: {},
        setAttribute: function () {},
        getAttribute: function () { return null; }
      };
    },
    documentElement: {
      style: {}
    }
  };

  // 添加 getBoundingClientRect 方法
  canvas.getBoundingClientRect = function () {
    return {
      top: 0,
      left: 0,
      width: canvas.width,
      height: canvas.height,
      right: canvas.width,
      bottom: canvas.height
    };
  };

  // 添加 tagName 属性
  canvas.tagName = 'CANVAS';

  // 添加 parentNode 模拟
  canvas.parentNode = {
    tagName: 'DIV',
    style: {},
    appendChild: function () {},
    removeChild: function () {}
  };

  return canvas;
}

/**
 * 创建事件对象
 * @param {string} type - 事件类型
 * @param {Object} data - 事件数据
 * @returns {Object} - 事件对象
 */
function createEvent(type, data = {}) {
  return {
    type: type,
    target: null,
    currentTarget: null,
    timeStamp: Date.now(),
    touches: data.touches || [],
    changedTouches: data.changedTouches || [],
    preventDefault: function () {},
    stopPropagation: function () {},
    ...data
  };
}

module.exports = {
  createCanvasAdapter,
  createEvent
};
