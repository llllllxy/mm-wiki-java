$(function () {
    $('[data-toggle="tooltip"]').tooltip();
    // $("[data-toggle='web-popover']").webuiPopover({animation: 'pop', autoHide: 3000});
});

var Common = {

    /**
     * ajax submit
     * @param url
     * @param data
     */
    ajaxSubmit: function (url, data) {
        var jsonData = {};
        if (data !== "" && data !== undefined) {
            var values = data.split("&");
            for (var i = 0; i < values.length; i++) {
                jsonData[values[i].split("=")[0]] = unescape(values[i].split("=")[1]);
            }
        }

        $.ajax({
            type: 'post',
            url: url,
            data: jsonData,
            dataType: "json",
            success: function (response) {
                if (response.code == 0) {
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
     * ajax submit callback
     * @param url
     * @param data
     * @param success
     * @param err
     */
    ajaxSubmitCallback: function (url, data, success, err) {
        var jsonData = {};
        if (data !== "" && data !== undefined) {
            var values = data.split("&");
            for (var i = 0; i < values.length; i++) {
                jsonData[values[i].split("=")[0]] = unescape(values[i].split("=")[1]);
            }
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
     * 成功提示
     * @param element
     * @param message
     */
    successBox: function (element, message) {
        $(element).html('');
        $(element).removeClass();
        $(element).addClass('alert alert-success');
        $(element).append('<a class="close" href="#" onclick="$(this).parent().hide();">&times;</a>');
        $(element).append('<strong><i class="glyphicon glyphicon-ok-circle"></i> 操作成功：</strong>');
        $(element).append(message);
        $(element).show();
    },

    /**
     * 错误提示
     * @param element
     * @param message
     */
    errorBox: function (element, message) {
        $(element).html('');
        $(element).removeClass('hide');
        $(element).addClass('alert alert-danger');
        $(element).append('<a class="close" href="#" onclick="$(this).parent().hide();">&times;</a>');
        $(element).append('<strong><i class="glyphicon glyphicon-remove-circle"></i> 操作失败：</strong>');
        $(element).append(message);
        $(element).show();
    },

    /**
     * 警告提示
     * @param element
     * @param message
     */
    warningBox: function (element, message) {
        $(element).html('');
        $(element).removeClass();
        $(element).addClass('alert alert-warning');
        $(element).append('<a class="close" href="#" onclick="$(this).parent().hide();">&times;</a>');
        $(element).append('<strong><i class="glyphicon glyphicon-volume-up"></i> 警告：</strong>');
        $(element).append(message);
        $(element).show();
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
