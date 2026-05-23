var RSA2048_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnrG3PIUGVEYpN+XbTosGWenS1F1fR0aK8/ti2R8qXDdiHkIGDf1d+5yuK6Awv6Ml6QKCzT3/LhScCyrVS+CjRZxGSspesk3piwirnw43xo7NZU5sBAPHhFas+MLPn9jQNKLc2BZ2ws8uf9E4LTRInFcS30T88ar1A/YlVSih+nT+wSS2PCU5N8LXW+vzb9oy1rSMSK4lO+OUC3aHPV5Q6HnpZyPeXtJrpgR3n5MGXeRyc5UTNCVRioyUxpBHShCSx5NbgegLWS2ihHte6vTm3sZ9dudJaO/kgjG0q5X/RLGhUxUfMWy15zoDxaP8/tEWi1S/+2YVqh10j4+rOcfIJQIDAQAB";

/**
 * 将纯 Base64 公钥包装为浏览器 RSA 工具需要的 PEM 格式。
 */
function rsa2048PublicKeyPem() {
    return "-----BEGIN PUBLIC KEY-----\n" + RSA2048_PUBLIC_KEY + "\n-----END PUBLIC KEY-----";
}

/**
 * 使用 RSA2048 公钥加密前端密码明文。
 */
function rsa2048Encrypt(value) {
    if (!value) {
        return value;
    }
    if (typeof JSEncrypt === "undefined") {
        return "";
    }
    var encryptor = new JSEncrypt();
    encryptor.setPublicKey(rsa2048PublicKeyPem());
    return encryptor.encrypt(value) || "";
}
