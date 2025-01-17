var App = getApp();

Page({
  data: {
    address: "",
    content: "",
    images: [],
    disabled: false
  },

  onLoad: function (options) {
    this.setData({
      fireId: options.fireId,
      currentId: wx.getStorageSync("userId")
    });
    this.getFireDetail(options.fireId);
  },

  /**
   * 隐患详情
   */
  getFireDetail: function (fireId) {
    var _this = this;
    App.get("fire/detail", {
      fireId: fireId
    }, function (result) {
      _this.setData(result.data)
    })
  },

  /**
   * 预览图片
   */
  previewImg: function (e) {
    var url = e.currentTarget.dataset.url,
      imageList = this.data.imageList;
    wx.previewImage({
      current: url,
      urls: imageList
    });
  },

  /**
   * 打开地图
   */
  openLocation: function () {
    var _this = this;
    wx.openLocation({
      latitude: Number(_this.data.latitude),
      longitude: Number(_this.data.longitude),
      name: _this.data.address
    })
  },

  /**
   * 输入备注
   */
  remark: function (e) {
    this.setData({
      remark: e.detail.value
    });
  },

  /**
   * 提交
   */
  submit: function () {
    var _this = this,
      data = {
        fireId: _this.data.fireId,
        remark: _this.data.remark
      };
    _this.setData({
      disabled: true
    });
    App.post("fire/audit", data, function () {
      App.showSuccess('提交成功', function () {
        wx.navigateBack()
      })
    }, false, function () {
      _this.setData({
        disabled: false
      });
    });
  }
});