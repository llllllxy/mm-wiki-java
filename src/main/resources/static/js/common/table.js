/**
 * bootstrap-table 公共封装，减少页面里重复编写的表格初始化配置。
 *
 * @param options 表格配置项
 */
function initTable(options) {
    // 根据 domId 获取表格对象，例如 domId = "tableId" 时，对应页面里的 <table id="tableId"></table>。
    var $table = $("#" + options.domId);
    // 初始化前先销毁旧实例，避免重复初始化导致表头、分页条、事件重复渲染。
    $table.bootstrapTable("destroy");
    // 初始化表格，并统一配置分页、排序、请求、响应解析等公共行为。
    $table.bootstrapTable({
        // 表格样式，默认使用 bootstrap 的边框、悬停、斑马纹样式。
        classes: getOption(options, "classes", "table table-bordered table-hover table-striped"),
        // 请求方式，默认 POST；如需 GET，可在页面 options.method 中覆盖。
        method: getOption(options, "method", "POST"),
        // 请求内容类型，默认使用普通表单提交格式，方便后端用参数对象或 request 接收。
        contentType: getOption(options, "contentType", "application/x-www-form-urlencoded;charset=UTF-8"),
        // jQuery ajax 的附加配置。
        ajaxOptions: {
            // 是否异步加载数据，默认 true。
            async: getOption(options, "async", true),
            // 请求超时时间，单位毫秒，默认 5 秒。
            timeout: getOption(options, "timeout", 5000),
            // 是否启用 ajax 缓存，默认 false，避免查询条件变化后还拿到旧数据。
            cache: getOption(options, "cache", false)
        },
        // 获取表格数据的后端接口地址。
        url: options.url,
        // 每一行数据的唯一标识字段，常用于刷新、选中、更新指定行。
        uniqueId: getOption(options, "uniqueId", "id"),
        // 工具栏容器选择器；传 toolbar = "toolbar" 时，会自动转成 "#toolbar"。
        toolbar: options.toolbar ? "#" + options.toolbar : undefined,
        // 是否显示斑马纹，默认 true。
        striped: getOption(options, "striped", true),
        // 期望后端返回的数据类型，默认 json。
        dataType: getOption(options, "dataType", "json"),
        // bootstrap-table 读取行数据的字段名；默认 rows，对应 responseHandler 返回的 rows。
        dataField: getOption(options, "dataField", "rows"),
        // bootstrap-table 读取总记录数的字段名；默认 total，对应 responseHandler 返回的 total。
        totalField: getOption(options, "totalField", "total"),
        // 表格语言环境，默认中文。
        locale: getOption(options, "locale", "zh-CN"),
        // 表格右上角按钮样式，default 对应 bootstrap 的 btn-default。
        buttonsClass: getOption(options, "buttonsClass", "default"),
        // 图标前缀，当前项目使用 bootstrap glyphicons。
        iconsPrefix: getOption(options, "iconsPrefix", "glyphicon"),
        // 是否显示刷新按钮，默认 false；页面需要时可显式传 true。
        showRefresh: getOption(options, "showRefresh", false),
        // 刷新表格时是否静默刷新，默认 true，减少额外 loading 干扰。
        silent: getOption(options, "silent", true),
        // 是否显示列筛选按钮，默认 false；页面需要自定义列显示时可开启。
        showColumns: getOption(options, "showColumns", false),
        // 是否启用表格数据缓存，默认 false。
        cache: getOption(options, "cache", false),
        // 是否点击行时自动选中 checkbox/radio，默认 false。
        clickToSelect: getOption(options, "clickToSelect", false),
        // 是否显示导出按钮，默认 false；依赖导出扩展插件。
        showExport: getOption(options, "showExport", false),
        // 表格按钮图标配置，可按页面需要覆盖 refresh、columns、export 等图标。
        icons: getOption(options, "icons", {
            // 刷新按钮图标。
            refresh: "glyphicon-refresh",
            // 列筛选按钮图标。
            columns: "glyphicon-th-list",
            // 导出按钮图标。
            export: "glyphicon-export"
        }),
        // 导出数据范围，basic 表示导出当前页基础数据。
        exportDataType: getOption(options, "exportDataType", "basic"),
        // 支持的导出文件类型，默认 excel。
        exportTypes: getOption(options, "exportTypes", ["excel"]),
        // 导出插件配置。
        exportOptions: {
            // 导出时忽略的列索引数组，默认不忽略任何列。
            ignoreColumn: getOption(options, "ignoreColumn", []),
            // 导出文件名，默认 myexcel。
            fileName: getOption(options, "fileName", "myexcel"),
            // 导出工作表名称，默认 myexcel。
            tableName: getOption(options, "tableName", "myexcel")
        },
        // 是否允许排序，默认 true。
        sortable: getOption(options, "sortable", true),
        // 默认排序方向，asc 升序，desc 降序。
        sortOrder: getOption(options, "sortOrder", "asc"),
        // 默认排序字段，需要和后端可识别的字段名保持一致。
        sortName: getOption(options, "sortName", "id"),
        // 是否显示 bootstrap-table 自带搜索框，默认 false；项目里通常使用自定义查询表单。
        search: getOption(options, "search", false),
        // 是否开启分页，默认 true。
        pagination: getOption(options, "pagination", true),
        // 分页是否循环跳转，默认 false，第一页不再跳上一页，最后一页不再跳下一页。
        paginationLoop: getOption(options, "paginationLoop", false),
        // 每页显示条数，默认 10。
        pageSize: getOption(options, "pageSize", 10),
        // 当前页码，默认第 1 页。
        pageNumber: getOption(options, "pageNumber", 1),
        // 每页条数下拉选项。
        pageList: getOption(options, "pageList", [10, 25, 50, 100]),
        // 是否显示跳转到指定页的控件，依赖 page-jump 扩展。
        paginationShowPageGo: getOption(options, "paginationShowPageGo", true),
        // 上一页按钮文案。
        paginationPreText: getOption(options, "paginationPreText", "上一页"),
        // 下一页按钮文案。
        paginationNextText: getOption(options, "paginationNextText", "下一页"),
        // 是否开启详情视图，默认 false；开启后可在行下面展开自定义内容。
        detailView: getOption(options, "detailView", false),
        // 详情视图渲染方法，只有 detailView 为 true 时才会用到。
        detailFormatter: options.detailFormatter,
        // 分页方式，server 表示服务端分页，client 表示前端一次性加载后分页。
        sidePagination: getOption(options, "sidePagination", "server"),
        // 请求参数格式；空字符串表示传 pageNumber/pageSize/sortName/sortOrder，limit 表示传 limit/offset。
        queryParamsType: getOption(options, "queryParamsType", ""),
        // 请求参数组装方法：先构造项目默认分页参数，再合并页面传入的自定义查询条件。
        queryParams: function (params) {
            // 构造默认分页、排序、搜索参数，统一后端接口入参名称。
            var query = buildDefaultQueryParams(params);
            // 如果页面提供了 queryParams 方法，就把页面查询条件合并进默认参数。
            if (typeof options.queryParams === "function") {
                return $.extend(query, options.queryParams(params) || {});
            }
            // 页面没有额外查询条件时，只提交默认分页、排序参数。
            return query;
        },
        // 后端响应解析方法；页面不传时使用项目默认 JsonResponse + PageModel 解析逻辑。
        responseHandler: options.responseHandler || defaultTableResponseHandler,
        // 数据加载失败后的回调；网络错误、超时等统一由 ajax.js 处理，这里只透传页面自定义回调。
        onLoadError: function (status, jqXHR) {
            if (typeof options.onLoadError === "function") {
                options.onLoadError(status, jqXHR);
            }
        },
        // 数据加载成功后的回调。
        onLoadSuccess: function (data) {
            // 表格刷新后重新激活 tooltip，否则新渲染出来的按钮提示不会生效。
            $("[data-toggle='tooltip']").tooltip();
            // 如果页面传入自定义成功回调，则继续执行页面自己的逻辑。
            if (typeof options.onLoadSuccess === "function") {
                options.onLoadSuccess(data);
            }
        },
        // 表格列配置，由具体页面定义字段、标题、格式化方法和操作按钮。
        columns: options.columns
    });
}

/**
 * 读取配置项。
 *
 * 这里不能写成 options[name] || defaultValue，因为显式传 false、0、空字符串时也应该生效。
 *
 * @param options 页面传入的配置对象
 * @param name 配置项名称
 * @param defaultValue 默认值
 * @returns {*} 页面传入值或默认值
 */
function getOption(options, name, defaultValue) {
    return options[name] !== undefined ? options[name] : defaultValue;
}

/**
 * 构造项目统一的默认分页查询参数。
 *
 * @param params bootstrap-table 传入的分页、排序参数
 * @returns {{number: *, searchText: *, page: *, sortOrder: *, sortName: *}} 后端分页接口参数
 */
function buildDefaultQueryParams(params) {
    return {
        // 当前页码，对应后端 pageNum。
        pageNum: params.pageNumber,
        // 每页条数，对应后端 pageSize。
        pageSize: params.pageSize,
        // bootstrap-table 自带搜索文本。
        searchText: params.searchText,
        // 排序字段。
        sortName: params.sortName,
        // 排序方向。
        sortOrder: params.sortOrder
    };
}

/**
 * 默认响应解析器。
 *
 * 后端约定返回 JsonResponse<PageModel<T>>，这里转换成 bootstrap-table 需要的 rows/total。
 *
 * @param res 后端 JsonResponse 响应
 * @returns {{total: number, rows: *[]}} bootstrap-table 数据结构
 */
function defaultTableResponseHandler(res) {
    // 空响应直接返回空表格，避免页面脚本报错。
    if (!res) {
        return {rows: [], total: 0};
    }
    // 非成功响应或缺少 data 时，只处理普通业务错误；会话失效等全局错误由 ajax.js 拦截。
    if (res.code !== 1 || !res.data) {
        if (typeof Layers !== "undefined") {
            Layers.failedMsg(res.message || "数据加载失败");
        } else {
            alert(res.message || "数据加载失败");
        }
        return {rows: [], total: 0};
    }

    // 兼容 PageModel(records/totalCount) 和 bootstrap-table 原生结构(rows/total)。
    return {
        rows: res.data.records || res.data.rows || [],
        total: res.data.totalCount || res.data.total || 0
    };
}

/**
 * HTML 转义，防止后端文本直接拼接进 formatter 时破坏页面结构。
 *
 * @param value 原始值
 * @returns {string} 转义后的安全文本
 */
function tableHtml(value) {
    return $("<div/>").text(value == null ? "" : value).html();
}

/**
 * 构造一个可点击链接。
 *
 * @param href 链接地址
 * @param text 链接文本
 * @param target 打开目标，可为空
 * @returns {string} 链接 HTML
 */
function tableLink(href, text, target) {
    var targetAttr = target ? ' target="' + tableHtml(target) + '"' : "";
    return '<a href="' + tableHtml(href) + '"' + targetAttr + '>' + tableHtml(text) + '</a>';
}

/**
 * 构造标签。
 *
 * @param text 标签文本
 * @param cssClass 标签样式
 * @returns {string} 标签 HTML
 */
function tableLabel(text, cssClass) {
    return '<span class="label ' + tableHtml(cssClass || "label-default") + '">' + tableHtml(text) + '</span>';
}

/**
 * 构造文字状态。
 *
 * @param text 文字
 * @param cssClass 样式
 * @returns {string} 文本 HTML
 */
function tableText(text, cssClass) {
    return '<span class="' + tableHtml(cssClass || "") + '">' + tableHtml(text) + '</span>';
}

/**
 * 渲染文档操作类型。
 *
 * @param action 操作类型：1 创建，2 修改，3 删除
 * @returns {string} 操作标签 HTML
 */
function tableDocumentAction(action) {
    if (action === 1) {
        return tableText("创建", "text text-info");
    }
    if (action === 2) {
        return tableText("修改", "text text-primary");
    }
    if (action === 3) {
        return tableText("删除", "text text-danger");
    }
    return tableHtml(action);
}

/**
 * 渲染空间权限。
 *
 * @param privilege 权限值：0 浏览者，1 编辑者，2 管理员
 * @returns {string} 权限标签 HTML
 */
function tableSpacePrivilege(privilege) {
    if (privilege === 1) {
        return tableLabel("编辑者", "label-info");
    }
    if (privilege === 2) {
        return tableLabel("管理员", "label-success");
    }
    return tableLabel("浏览者", "label-default");
}

/**
 * 渲染逗号分隔的标签列表。
 *
 * @param value 原始标签字符串
 * @returns {string} 标签列表 HTML
 */
function tableTags(value) {
    if (!value) {
        return "";
    }
    var html = "";
    var tags = String(value).split(",");
    for (var i = 0; i < tags.length; i++) {
        var tag = tags[i].trim();
        if (tag) {
            html += tableLabel(tag, "label-default") + " ";
        }
    }
    return html;
}

/**
 * 刷新表格并回到第一页，通常用于查询按钮。
 *
 * @param tableId 表格 ID
 */
function reloadTableFirstPage(tableId) {
    $("#" + tableId).bootstrapTable("refreshOptions", {pageNumber: 1});
}

/**
 * 重置查询表单并刷新表格。
 *
 * @param formId 表单 ID
 * @param tableId 表格 ID
 */
function resetTableSearch(formId, tableId) {
    $("#" + formId)[0].reset();
    reloadTableFirstPage(tableId);
}
