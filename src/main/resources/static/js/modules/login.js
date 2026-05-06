var Login = {

    errorMessage: "#errorMessage",

    /**
     * 登录
     * @param element
     */
    ajaxSubmit: function (element) {
        var usernameEle = $(element).find("input[name='username']");
        var passwordEle = $(element).find("input[name='password']");
        var submitEle = $(element).find("input[name='submit']");
        var username = usernameEle.val();
        var password = passwordEle.val();
        if (!username) {
            layer.tips(usernameEle.attr("placeholder"), usernameEle);
            return false;
        }
        if (!password) {
            layer.tips(passwordEle.attr("placeholder"), passwordEle);
            return false;
        }

        function response(result) {
            if (result.code == 0) {
                passwordEle.val(password);
                layer.tips(result.message, submitEle);
            }
            if (result.code == 1) {
                var content = '<i class="fa fa-smile-o"></i> ' + result.message;
                layer.msg(content);
                if (result.redirect.url) {
                    var sleepTime = result.redirect.sleep || 3000;
                    setTimeout(function () {
                        location.href = result.redirect.url;
                    }, sleepTime);
                }
            }
        }

        function beforeSubmit(formData, jqForm, options) {
            // 对要提交的数据进行修改
            formData[0].value = username;
            formData[1].value = hex_sha256(password);
            return true;  // 返回true表示继续提交表单，返回false表示取消提交
        }

        var options = {
            dataType: 'json',
            beforeSubmit: beforeSubmit,
            success: response
        };

        $(element).ajaxSubmit(options);
    }
};