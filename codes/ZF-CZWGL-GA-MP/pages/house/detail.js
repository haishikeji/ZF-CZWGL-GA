var App = getApp(),
  QQMapWX = require("../../utils/qqmap-wx-jssdk.js");

Page({
  data: {
    houseId: "",
    houseTypeIndex: 0,
    rentalTypeIndex: 0,
    houseNo: "",
    imageList: [],
    disabled: false,
    showProtol: true,
    time: 5,
    isAgree: false,
    next: "已阅读(5s)",
    houseTypeArry: [{
      name: "住宅",
      value: 1
    }, {
      name: "办公",
      value: 2
    }, {
      name: "商用",
      value: 3
    }, {
      name: "其他",
      value: 4
    }],
    rentalTypeArry: [{
      name: "居住",
      value: 1
    }, {
      name: "商业",
      value: 2
    }, {
      name: "办公",
      value: 3
    }, {
      name: "宿舍",
      value: 4
    }, {
      name: "仓库",
      value: 5
    }, {
      name: "厂房",
      value: 6
    }, {
      name: "其他",
      value: 7
    }],
    address: "",
    street: "",
    isCheck: true,
    isIllegal: false,
    isRecord: true
  },

  bindHouseType: function(e) {
    this.setData({
      houseTypeIndex: e.detail.value
    });
  },

  bindRentalType: function(e) {
    this.setData({
      rentalTypeIndex: e.detail.value
    });
  },

  bindSpace: function(e) {
    this.data.space = e.detail.value;
  },

  bindRoom: function(e) {
    this.data.room = e.detail.value;
  },

  bindHouseNo: function(e) {
    this.data.houseNo = e.detail.value;
  },

  bindStreet: function(e) {
    this.data.street = e.detail.value;
  },

  isCheckChange: function (e) {
    this.data.isCheck = e.detail.value
  },

  isIllegalChange: function (e) {
    this.data.isIllegal = e.detail.value
  },

  isRecordChange: function(e) {
    this.data.isRecord = e.detail.value
  },

  onLoad: function(options) {
    var _this = this;
    wx.hideShareMenu();
    if (options.houseId) {
      _this.data.houseId = options.houseId;
      _this.setData({
        showProtol: false
      });
      _this.getHouseDetail(options.houseId);
    } else {
      _this.getLocation();
      _this.getProtol();
    }
    if (_this.data.showProtol) {
      var time = _this.data.time;
      var interval = setInterval(function() {
        time >= 1 ? (time--, _this.setData({
          next: "已阅读(" + time + "s)",
          time: time
        })) : (_this.setData({
          next: "确定"
        }), clearInterval(interval));
      }, 1000);
    } else {
      _this.setData({
        showProtol: false
      });
    }
  },

  /**
   * 已阅读
   */
  read: function(e) {
    this.setData({
      showProtol: false
    });
  },

/**
 * 治安管理主体责任书
 */
  getProtol: function () {
    var _this = this;
    App.get("/protol", {
      key: 'security'
    }, function (result) {
      _this.setData(result.data);
    });
  },

  /**
   * 房屋信息
   */
  getHouseDetail: function(houseId) {
    var _this = this;
    App.get("house/detail", {
      houseId: houseId
    }, function(result) {
      _this.setData(result.data);
      _this.data.houseTypeArry.forEach(function(item, index) {
        if (item.value == _this.data.houseType) {
          _this.setData({
            houseTypeIndex: index
          });
        }
      })
      _this.data.rentalTypeArry.forEach(function(item, index) {
        if (item.value == _this.data.rentalType) {
          _this.setData({
            rentalTypeIndex: index
          });
        }
      })
    });
  },

  /**
   * 提交备案
   */
  submit: function() {
    var _this = this,
      data = {
        houseId: _this.data.houseId,
        houseType: _this.data.houseTypeArry[_this.data.houseTypeIndex].value,
        rentalType: _this.data.rentalTypeArry[_this.data.rentalTypeIndex].value,
        address: _this.data.address,
        street: _this.data.street,
        latitude: _this.data.latitude,
        longitude: _this.data.longitude,
        houseNo: _this.data.houseNo,
        space: _this.data.space,
        room: _this.data.room,
        images: _this.data.imageList,
        isCheck: _this.data.isCheck,
        isIllegal: _this.data.isIllegal,
        isRecord: _this.data.isRecord
      };
    if (!data.address || !data.latitude || !data.longitude) {
      App.showFail("请选择房屋坐落地址");
      return false;
    }
    if (!data.street) {
      App.showFail("请输入街道地址和门牌号");
      return false;
    }
    if (!data.space || data.space == 0) {
      App.showFail("出租面积必须大于0");
      return false;
    }
    if (!data.room || data.room == 0) {
      App.showFail("出租间数必须大于0");
      return false;
    }
    if (!data.images.length) {
      App.showFail("房屋照片至少上传一张");
      return false;
    }
    wx.showModal({
      title: '提示',
      content: '请核对信息无误，提交后需要重新审核，确定要提交？',
      success: function (o) {
        if (o.confirm) {
          _this.setData({
            disabled: true
          });
          var callback = function (data) {
            App.post("house/save", data, function () {
              App.showSuccess('提交成功', function () {
                wx.navigateBack();
              })
            }, false, function () {
              _this.setData({
                disabled: false
              });
            });
          };
          _this.uploadFile(data.images.length, data, callback);
        }
      }
    })
    
  },

  /**
   * 文件上传
   */
  uploadFile: function(imageNum, data, callback) {
    var uploadNum = 0;
    wx.showLoading({
      title: '图片上传中',
      mask: true
    })
    data.images.forEach(function(image, index) {
      wx.uploadFile({
        url: App.apiRoot + "upload/image",
        filePath: image,
        name: "file",
        formData: {
          token: wx.getStorageSync("token")
        },
        success: function(res) {
          var result = typeof res.data === 'object' ? res.data : JSON.parse(res.data);
          if (result.code === 0) {
            data.images[index] = result.data.imageUrl
          }
        },
        complete: function() {
          if (imageNum === ++uploadNum) {
            wx.hideLoading();
            callback && callback(data);
          }
        }
      });
    });
  },

  /**
   * 获取当前位置
   */
  getLocation: function() {
    var _this = this,
      qqmapsdk = new QQMapWX({
        key: "FSFBZ-HZD6X-PT44K-7FLLA-KHEI6-ONFZU"
      });
    wx.getLocation({
      type: "wgs84",
      success: function(res) {
        qqmapsdk.reverseGeocoder({
          location: {
            latitude: res.latitude,
            longitude: res.longitude
          },
          success: function(e) {
            var address = e.result.formatted_addresses.recommend;
            _this.setData({
              address: address,
              latitude: res.latitude,
              longitude: res.longitude
            });
          },
          fail: function(e) {
            _this.setData({
              address: "无法获取的你地理位置，请手动选择"
            });
          }
        });
      },
      fail: function(e) {
        _this.setData({
          address: "无法获取的你地理位置，请重新定位"
        });
      }
    });
  },

  /**
   * 选择地址
   */
  getAddress: function() {
    var _this = this;
    wx.getSetting({
      success: function(res) {
        if (0 == res.authSetting["scope.userLocation"]) {
          wx.showModal({
            content: "检测到您没打开定位权限，是否去设置打开？",
            confirmText: "确认",
            cancelText: "取消",
            success: function(o) {
              o.confirm && wx.openSetting();
            }
          });
        } else {
          wx.chooseLocation({
            success: function(result) {
              if (result.name != "") {
                _this.setData({
                  address: result.name,
                  latitude: result.latitude,
                  longitude: result.longitude
                });
              }
            }
          });
        }
      }
    });
  },

  /**
   * 同意阅读
   */
  bindAgreeChange: function(e) {
    this.setData({
      isAgree: !!e.detail.value.length
    });
  },

  /**
   * 选择图片
   */
  chooseImage: function(e) {
    var _this = this,
      index = e.currentTarget.dataset.index;
    wx.chooseImage({
      count: 4,
      sizeType: ['original', "compressed"],
      sourceType: ["album", "camera"],
      success: function(res) {
        if (_this.data.imageList.length + res.tempFilePaths.length > 6) {
          App.showFail("最多上传4张图片");
        } else {
          _this.setData({
            imageList: _this.data.imageList.concat(res.tempFilePaths)
          });
        }
      }
    });
  },

  /**
   * 删除图片
   */
  deleteImage: function(e) {
    var _this = this,
      index = e.currentTarget.dataset.index,
      imageList = _this.data.imageList;
    imageList.splice(index, 1);
    _this.setData({
      imageList: imageList
    });
  },

  /**
   * 预览图片
   */
  previewImg: function(e) {
    var url = e.currentTarget.dataset.url,
      imageList = this.data.imageList;
    wx.previewImage({
      current: url,
      urls: imageList
    });
  }
});