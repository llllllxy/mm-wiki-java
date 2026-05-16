/**
 * 封装AJAX，设置AJAX的全局默认选项，
 * 当AJAX请求会话过期时，跳转到登录页面
 */
var _ajax = $.ajax;

var ajaxDefaults = {
    timeout: 20000,
    async: true,
    cache: false,
    dataType: "json"
};

var AJAX_CODE_UNAUTHORIZED = 401;

$.ajax = function (opt) {
    opt = $.extend({}, ajaxDefaults, opt || {});

    var fn = {
        error: function (XMLHttpRequest, textStatus, errorThrown) {
        },
        success: function (data, textStatus) {
        },
        beforeSend: function (XHR) {
        },
        complete: function (XHR, TS) {
        }
    };
    if (opt.error) {
        fn.error = opt.error;
    }
    if (opt.success) {
        fn.success = opt.success;
    }
    if (opt.beforeSend) {
        fn.beforeSend = opt.beforeSend;
    }
    if (opt.complete) {
        fn.complete = opt.complete;
    }
    //扩展增强处理
    var _opt = $.extend({}, opt, {
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            handleErrorMsg(XMLHttpRequest);
            // 错误方法增强处理
            fn.error(XMLHttpRequest, textStatus, errorThrown);
        },
        // 只有 HTTP 状态码为 200（包括 200-299 范围内）的 Ajax 请求才会触发 success 回调函数，其他状态码将触发 error 回调函数
        success: function (res, textStatus) {
            if (Number(res && res.code) === AJAX_CODE_UNAUTHORIZED) {
                handleUnauthorized(res);
            } else {
                // 成功回调方法增强处理
                fn.success(res, textStatus);
            }
        },
        beforeSend: function (XHR) {
            XHR.setRequestHeader("Powered-By", 'MM-WIKI');
            // 提交前回调方法
            fn.beforeSend(XHR);
        },
        complete: function (XHR, TS) {
            // 完成后回调方法
            fn.complete(XHR, TS);
        }
    });
    return _ajax(_opt);
};

/**
 * 处理普通 Ajax 请求未登录或登录失效响应。
 *
 * @param response 后端返回的 JSON 对象
 */
function handleUnauthorized(response) {
    var redirectUrl = response.redirect && response.redirect.url ? response.redirect.url : "/author/index";

    if (typeof layer !== "undefined") {
        layer.alert('会话已过期，请重新登录', function (index) {
            layer.close(index);
            redirectTopWindow(redirectUrl);
        });
    } else if (confirm("未登录或登录已失效！\n是否跳转到登录页面？")) {
        redirectTopWindow(redirectUrl);
    } else {
        console.log("未登录或登录已失效！");
    }
}

/**
 * 跳转到登录页。
 *
 * iframe 内页面会优先让顶层窗口跳转，避免只在 iframe 内展示登录页。
 *
 * @param url 登录页地址
 */
function redirectTopWindow(url) {
    if (window.top && window.top !== window) {
        window.top.location.href = url;
    } else {
        window.location.href = url;
    }
}


function handleErrorMsg(XMLHttpRequest) {
    if (XMLHttpRequest && Number(XMLHttpRequest.status) === 401) {
        handleUnauthorized(XMLHttpRequest.responseJSON || {});
        return;
    }
    var message = "错误提示： " + XMLHttpRequest.status + " " + XMLHttpRequest.statusText;
    if (typeof layer !== "undefined") {
        var content = '<i class="fa fa-frown-o"></i> ';
        layer.msg(content + message);
    } else {
        alert(message);
    }
}
