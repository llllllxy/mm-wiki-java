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
            if (result.code !== 1) {
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

        var $form = $(element);
        var formData = $form.serializeArray();
        for (var i = 0; i < formData.length; i++) {
            if (formData[i].name == "username") {
                formData[i].value = username;
            }
            if (formData[i].name == "password") {
                formData[i].value = hex_sha256(password);
            }
        }

        $.ajax({
            type: $form.attr("method") || "post",
            url: $form.attr("action"),
            data: formData,
            dataType: 'json',
            success: response
        });
    }
};
