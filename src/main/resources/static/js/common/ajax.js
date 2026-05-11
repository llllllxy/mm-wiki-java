/**
 * Ajax 请求封装。
 */
var AjaxUtil = {
    ctx: "",

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
    }
};
