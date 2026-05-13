/**
 * Ajax 请求封装。
 */
var AjaxUtil = {
    // 防止多个 Ajax 请求同时触发 HTTP 401 时重复弹出多个登录失效弹框。
    unauthorizedAlerting: false,

    get: function (options) {
        return AjaxUtil.request(options, "GET");
    },

    post: function (options) {
        return AjaxUtil.request(options, "POST");
    },

    upload: function (options) {
        if (!options || !options.url) {
            alert("请求错误，url不可为空!");
            return false;
        }
        options.type = "POST";
        options.timeout = options.timeout !== undefined ? options.timeout : 5000;
        options.async = options.async !== undefined ? options.async : true;
        options.cache = options.cache !== undefined ? options.cache : false;
        options.dataType = options.dataType || "json";
        options.contentType = options.contentType !== undefined ? options.contentType : false;
        options.processData = options.processData !== undefined ? options.processData : false;
        options.data = options.data || new FormData();

        return $.ajax(options.url, {
            type: options.type,
            timeout: options.timeout,
            async: options.async,
            cache: options.cache,
            dataType: options.dataType,
            processData: options.processData,
            data: options.data,
            contentType: options.contentType,
            success: function (data, textStatus, jqXHR) {
                if (typeof options.success === "function") {
                    options.success(data, textStatus, jqXHR);
                }
            },
            error: function (XMLHttpRequest) {
                AjaxUtil.handleError(options, XMLHttpRequest);
            }
        });
    },

    request: function (options, method) {
        if (!options || !options.url) {
            alert("请求错误，url不可为空!");
            return false;
        }
        options.type = method;
        options.timeout = options.timeout !== undefined ? options.timeout : 5000;
        options.async = options.async !== undefined ? options.async : true;
        options.cache = options.cache !== undefined ? options.cache : false;
        options.dataType = options.dataType || "json";
        options.contentType = options.contentType || "application/x-www-form-urlencoded;charset=UTF-8";
        options.data = options.data || "";

        return $.ajax(options.url, {
            type: options.type,
            timeout: options.timeout,
            async: options.async,
            cache: options.cache,
            dataType: options.dataType,
            data: options.data,
            contentType: options.contentType,
            success: function (data, textStatus, jqXHR) {
                if (typeof options.success === "function") {
                    options.success(data, textStatus, jqXHR);
                }
            },
            error: function (XMLHttpRequest) {
                AjaxUtil.handleError(options, XMLHttpRequest);
            }
        });
    },

    handleError: function (options, XMLHttpRequest) {
        if (AjaxUtil.handleUnauthorized(XMLHttpRequest)) {
            return;
        }

        var message = "错误提示： " + XMLHttpRequest.status + " " + XMLHttpRequest.statusText;
        if (typeof options.error === "function") {
            options.error(message);
            return;
        }
        if (typeof Layers !== "undefined") {
            Layers.failedMsg(message);
        } else {
            alert(message);
        }
    },

    /**
     * 处理普通 Ajax 请求时的 HTTP 401 未授权响应。
     *
     * @param XMLHttpRequest jQuery XHR 对象
     * @returns {boolean} true 表示已经识别并处理 HTTP 401
     */
    handleUnauthorized: function (XMLHttpRequest) {
        if (!XMLHttpRequest || Number(XMLHttpRequest.status) !== 401) {
            return false;
        }
        if (AjaxUtil.unauthorizedAlerting) {
            return true;
        }

        AjaxUtil.unauthorizedAlerting = true;
        var response = XMLHttpRequest.responseJSON || {};
        var redirectUrl = response.redirect && response.redirect.url ? response.redirect.url : "/author/index";

        if (typeof layer !== "undefined") {
            layer.confirm("未登录或登录已失效！<br/>是否跳转到登录页面？", {
                title: "登录状态失效",
                btn: ["跳转登录", "留在本页"],
                btnAlign: "c",
                closeBtn: 0
            }, function (index) {
                layer.close(index);
                window.location.href = redirectUrl;
            }, function () {
                AjaxUtil.unauthorizedAlerting = false;
            });
        } else if (confirm("未登录或登录已失效！\n是否跳转到登录页面？")) {
            window.location.href = redirectUrl;
        } else {
            AjaxUtil.unauthorizedAlerting = false;
        }
        return true;
    }
};
