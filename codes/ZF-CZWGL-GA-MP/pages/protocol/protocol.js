Page({
    data: {
        time: 0,
        interval: 0
    },
    onLoad: function(n) {
        var t = this;
        wx.hideShareMenu();
        var e = t.data.time, a = setInterval(function() {
            e++, t.setData({
                time: e,
                interval: a
            });
        }, 1e3);
        wx.showModal({
            title: "温馨提示",
            content: "如果您没有阅读此告知书，我们将无法继续为您提供房屋出租备案的服务！",
            showCancel: !0,
            cancelText: "暂时跳过",
            confirmText: "继续阅读",
            confirmColor: "#1ed451",
            success: function(n) {
                n.confirm;
            }
        });
    },
    onReady: function() {},
    onShow: function() {},
    onHide: function() {
        var n = this.data.interval;
        clearInterval(n);
    },
    onUnload: function() {
        var n = this.data.interval;
        clearInterval(n);
    },
    onPullDownRefresh: function() {},
    onReachBottom: function() {},
    onShareAppMessage: function() {}
});