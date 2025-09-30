// 强制设置中文字体的JavaScript代码
(function() {
    'use strict';

    // 等待页面加载完成
    function initializeFonts() {
        console.log('Initializing Chinese font support...');

        // 创建样式标签强制设置字体
        const style = document.createElement('style');
        style.innerHTML = `
            * {
                font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Microsoft YaHei', '微软雅黑', sans-serif !important;
            }
            
            canvas, .skiko-canvas, .compose-web-canvas {
                font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Microsoft YaHei', '微软雅黑', sans-serif !important;
            }
        `;
        document.head.appendChild(style);

        // 尝试拦截和修改Skiko的字体设置
        if (window.skiko) {
            console.log('Found Skiko, attempting to set font...');
            try {
                // 尝试设置Skiko的默认字体
                if (window.skiko.setFont) {
                    window.skiko.setFont('Noto Sans SC');
                }
                if (window.skiko.FontMgr) {
                    window.skiko.FontMgr.setDefaultFont('Noto Sans SC');
                }
            } catch (e) {
                console.log('Could not set Skiko font directly:', e);
            }
        }

        // 监听全局Skiko对象的创建
        Object.defineProperty(window, 'skiko', {
            set: function(value) {
                console.log('Skiko object created, attempting font configuration...');
                this._skiko = value;
                try {
                    if (value.setFont) {
                        value.setFont('Noto Sans SC');
                    }
                    if (value.FontMgr) {
                        value.FontMgr.setDefaultFont('Noto Sans SC');
                    }
                } catch (e) {
                    console.log('Could not configure Skiko font:', e);
                }
            },
            get: function() {
                return this._skiko;
            }
        });

        // 监听Skiko Canvas元素的创建
        const observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                mutation.addedNodes.forEach(function(node) {
                    if (node.nodeType === Node.ELEMENT_NODE) {
                        if (node.tagName === 'CANVAS' ||
                            node.classList.contains('skiko-canvas') ||
                            node.classList.contains('compose-web-canvas')) {
                            console.log('Found canvas element, applying font...');
                            node.style.fontFamily = "'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Microsoft YaHei', '微软雅黑', sans-serif";

                            // 尝试通过Canvas 2D context设置字体
                            try {
                                const ctx = node.getContext('2d');
                                if (ctx) {
                                    ctx.font = "16px 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Microsoft YaHei', sans-serif";
                                    console.log('Set canvas 2D context font');
                                }
                            } catch (e) {
                                console.log('Could not set canvas 2D font:', e);
                            }
                        }

                        // 递归处理子元素
                        const canvases = node.querySelectorAll ? node.querySelectorAll('canvas, .skiko-canvas, .compose-web-canvas') : [];
                        canvases.forEach(function(canvas) {
                            console.log('Found nested canvas, applying font...');
                            canvas.style.fontFamily = "'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Microsoft YaHei', '微软雅黑', sans-serif";

                            try {
                                const ctx = canvas.getContext('2d');
                                if (ctx) {
                                    ctx.font = "16px 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Microsoft YaHei', sans-serif";
                                }
                            } catch (e) {
                                console.log('Could not set nested canvas 2D font:', e);
                            }
                        });
                    }
                });
            });
        });

        observer.observe(document.body, {
            childList: true,
            subtree: true
        });

        console.log('Chinese font support initialized');
    }

    // 在页面加载时和字体加载完成后初始化
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initializeFonts);
    } else {
        initializeFonts();
    }

    // 等待Google字体加载完成
    if (document.fonts && document.fonts.ready) {
        document.fonts.ready.then(function() {
            console.log('Google fonts loaded, reinitializing...');
            initializeFonts();
        });
    }

    // 尝试拦截WebGL相关调用
    const originalGetContext = HTMLCanvasElement.prototype.getContext;
    HTMLCanvasElement.prototype.getContext = function(contextType, contextAttributes) {
        const context = originalGetContext.call(this, contextType, contextAttributes);
        if (contextType === '2d' && context) {
            console.log('Intercepted Canvas 2D context creation');
            context.font = "16px 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Microsoft YaHei', sans-serif";
        }
        return context;
    };

})();
