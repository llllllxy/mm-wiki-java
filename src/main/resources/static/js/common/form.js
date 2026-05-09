/**
 * Form.js 表单提交类
 * 依赖 jquery.form.js
 */

var Form = {

    /**
     * 提示 div
     */
    failedBox: '#failedBox',

    /**
     * 是否在弹框中
     */
    inPopup: false,

    /**
     * ajax submit
     * @param element
     * @param inPopup
     * @returns {boolean}
     */
    ajaxSubmit: function (element, inPopup) {
        if (inPopup) {
            Form.inPopup = true;
        }

        function successBox(message, data) {
            Common.successBox(Form.failedBox, message)
        }

        function failed(message, data) {
            Common.errorBox(Form.failedBox, message)
        }

        function response(result) {
            if (result.code == 0) {
                failed(result.message, result.data);
            }
            if (result.code == 1) {
                successBox(result.message, result.data);
            }
            $("body,html").animate({scrollTop: 0}, 300);
            if (result.redirect.url) {
                var sleepTime = result.redirect.sleep || 3000;
                setTimeout(function () {
                    if (Form.inPopup) {
                        parent.location.href = result.redirect.url;
                    } else {
                        location.href = result.redirect.url;
                    }
                }, sleepTime);
            }
        }

        function beforeSubmit(formData, jqForm, options) {
            // 只加密显式标记的密码字段，避免通用表单误伤邮箱、LDAP、安装配置等明文密钥。
            for (var i = 0; i < formData.length; i++) {
                var field = jqForm.find(":input").filter(function () {
                    return this.name == formData[i].name && $(this).attr("data-sha256") == "true";
                });
                if (field.length > 0 && formData[i].value) {
                    formData[i].value = hex_sha256(formData[i].value);
                }
            }
            return true;  // 返回true表示继续提交表单，返回false表示取消提交
        }

        var options = {
            dataType: 'json',
            beforeSubmit: beforeSubmit,
            success: response
        };

        $(element).ajaxSubmit(options);

        return false;
    }
};
