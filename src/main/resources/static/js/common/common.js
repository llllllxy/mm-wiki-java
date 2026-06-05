$(function () {
    $('[data-toggle="tooltip"]').tooltip();
    // $("[data-toggle='web-popover']").webuiPopover({animation: 'pop', autoHide: 3000});
});

var Common = {

    /**
     * ajax submit
     * @param url
     * @param data 可以是字符串 (如 "a=1&b=2")、数组 (serializeArray() 输出) 或普通对象
     */
    ajaxSubmit: function (url, data) {
        var jsonData = {};
        // 1. 处理 undefined 或空字符串
        if (data === undefined || data === "") {
            jsonData = {};
        }
        // 2. 如果已经是数组 (serializeArray 的结果)
        else if (Array.isArray(data) && data.length && data[0].hasOwnProperty('name') && data[0].hasOwnProperty('value')) {
            for (var i = 0; i < data.length; i++) {
                jsonData[data[i].name] = data[i].value;
            }
        }
        // 3. 当作字符串处理 (serialize 的结果)
        else if (typeof data === 'string') {
            var values = data.split("&");
            for (var i = 0; i < values.length; i++) {
                var pair = values[i].split("=");/**/
                jsonData[pair[0]] = pair.length > 1 ? decodeURIComponent(pair[1].replace(/\+/g, '%20')) : "";
            }
        } else {
            jsonData = data;
        }

        $.ajax({
            type: 'post',
            url: url,
            data: jsonData,
            dataType: "json",
            success: function (response) {
                if (response.code !== 1) {
                    Layers.failedMsg(response.message, function () {
                    });
                } else {
                    var redirect = response.redirect || {};
                    var sleep = redirect.sleep !== undefined ? redirect.sleep : 2000;
                    Layers.successMsg(response.message, sleep, function () {
                        Common.redirect(redirect);
                    });
                }
            }
        });
    },

    /**
     * 弹出确认框，确认后执行通用 ajax 提交。
     * @param title 确认提示文案
     * @param url 提交地址
     * @param data 提交参数
     */
    ajaxSubmitConfirm: function (title, url, data) {
        Layers.confirmCallback(title, function () {
            Common.ajaxSubmit(url, data);
        }, function () {
        });
    },

    /**
     * ajax submit callback
     * @param url
     * @param data
     * @param success
     * @param err
     */
    ajaxSubmitCallback: function (url, data, success, err) {
        var jsonData = {};
        // 1. 处理 undefined 或空字符串
        if (data === undefined || data === "") {
            jsonData = {};
        }
        // 2. 如果已经是数组 (serializeArray 的结果)
        else if (Array.isArray(data) && data.length && data[0].hasOwnProperty('name') && data[0].hasOwnProperty('value')) {
            for (var i = 0; i < data.length; i++) {
                jsonData[data[i].name] = data[i].value;
            }
        }
        // 3. 当作字符串处理 (serialize 的结果)
        else if (typeof data === 'string') {
            var values = data.split("&");
            for (var i = 0; i < values.length; i++) {
                var pair = values[i].split("=");/**/
                jsonData[pair[0]] = pair.length > 1 ? decodeURIComponent(pair[1].replace(/\+/g, '%20')) : "";
            }
        } else {
            jsonData = data;
        }
        $.ajax({
            type: 'post',
            url: url,
            data: jsonData,
            dataType: "json",
            success: function (response) {
                success(response);
            },
            error: function (response) {
                if (typeof err === "function") {
                    err(response);
                }
            }
        });
    },

    /**
     * redirect
     * @param redirect
     */
    redirect: function (redirect) {
        redirect = redirect || {};
        if (redirect.url) {
            location.href = redirect.url;
        } else {
            location.reload();
        }
    },

    /**
     * 时间格式化
     * @param s
     * @returns {{s: (string|*), d: (string|*), h: (string|*), m: (string|*)}}
     */
    secondsFormat: function (s) {
        var day = Math.floor(s / (24 * 3600));
        var hour = Math.floor((s - day * 24 * 3600) / 3600);
        var minute = Math.floor((s - day * 24 * 3600 - hour * 3600) / 60);
        var second = s - day * 24 * 3600 - hour * 3600 - minute * 60;

        function formatStr(t) {
            if (parseInt(t) <= 0) {
                return "00";
            }
            if (0 < parseInt(t) && parseInt(t) < 10) {
                return "0" + t.toString();
            }
            return t;
        }

        return {
            d: formatStr(day),
            h: formatStr(hour),
            m: formatStr(minute),
            s: formatStr(second)
        };
    },

    /**
     * 判断是否是移动端
     * @returns {boolean}
     * @constructor
     */
    isMobile: function () {
        var sUserAgent = navigator.userAgent.toLowerCase();
        var bIsIpad = sUserAgent.match(/ipad/i) == "ipad";
        var bIsIphoneOs = sUserAgent.match(/iphone os/i) == "iphone os";
        var bIsMidp = sUserAgent.match(/midp/i) == "midp";
        var bIsUc7 = sUserAgent.match(/rv:1.2.3.4/i) == "rv:1.2.3.4";
        var bIsUc = sUserAgent.match(/ucweb/i) == "ucweb";
        var bIsAndroid = sUserAgent.match(/android/i) == "android";
        var bIsCE = sUserAgent.match(/windows ce/i) == "windows ce";
        var bIsWM = sUserAgent.match(/windows mobile/i) == "windows mobile";
        return !!(bIsIpad || bIsIphoneOs || bIsMidp || bIsUc7 || bIsUc || bIsAndroid || bIsCE || bIsWM);
    }
};
