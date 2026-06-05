/**
 * Form.js 表单提交类
 */

var Form = {

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
        function response(result) {
            if (result.code !== 1) {
                Layers.failedMsg(result.message)
            } else {
                Layers.successMsg(result.message)
            }
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

        function beforeSubmit(formData, jqForm) {
            // 只加密显式标记的密码字段，避免通用表单误伤邮箱、LDAP、安装配置等明文密钥。
            for (var i = 0; i < formData.length; i++) {
                var field = jqForm.find(":input").filter(function () {
                    return this.name == formData[i].name && $(this).attr("data-rsa2048") == "true";
                });
                if (field.length > 0 && formData[i].value) {
                    var encrypted = rsa2048Encrypt(formData[i].value);
                    if (!encrypted) {
                        Layers.failedMsg("密码加密失败！")
                        return false;
                    }
                    formData[i].value = encrypted;
                }
            }
            return true;
        }

        var $form = $(element);
        var formData = $form.serializeArray();
        if (!beforeSubmit(formData, $form)) {
            return false;
        }

        $.ajax({
            type: $form.attr("method") || "post",
            url: $form.attr("action"),
            data: formData,
            dataType: 'json',
            success: response
        });

        return false;
    }
};
