package org.tinycloud.mmwiki.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.tinycloud.mmwiki.config.MmwikiProperties;
import org.tinycloud.mmwiki.util.RSAUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PasswordCryptoServiceTest {

    private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnrG3PIUGVEYpN+XbTosGWenS1F1fR0aK8/ti2R8qXDdiHkIGDf1d+5yuK6Awv6Ml6QKCzT3/LhScCyrVS+CjRZxGSspesk3piwirnw43xo7NZU5sBAPHhFas+MLPn9jQNKLc2BZ2ws8uf9E4LTRInFcS30T88ar1A/YlVSih+nT+wSS2PCU5N8LXW+vzb9oy1rSMSK4lO+OUC3aHPV5Q6HnpZyPeXtJrpgR3n5MGXeRyc5UTNCVRioyUxpBHShCSx5NbgegLWS2ihHte6vTm3sZ9dudJaO/kgjG0q5X/RLGhUxUfMWy15zoDxaP8/tEWi1S/+2YVqh10j4+rOcfIJQIDAQAB";
    private static final String PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCesbc8hQZURik35dtOiwZZ6dLUXV9HRorz+2LZHypcN2IeQgYN/V37nK4roDC/oyXpAoLNPf8uFJwLKtVL4KNFnEZKyl6yTemLCKufDjfGjs1lTmwEA8eEVqz4ws+f2NA0otzYFnbCzy5/0TgtNEicVxLfRPzxqvUD9iVVKKH6dP7BJLY8JTk3wtdb6/Nv2jLWtIxIriU745QLdoc9XlDoeelnI95e0mumBHefkwZd5HJzlRM0JVGKjJTGkEdKEJLHk1uB6AtZLaKEe17q9Obexn1250lo7+SCMbSrlf9EsaFTFR8xbLXnOgPFo/z+0RaLVL/7ZhWqHXSPj6s5x8glAgMBAAECggEAHS7pV4PPCihs5A6tKeB/JoHNd6hEIUNjwkJ0hyEoFRVKQe46VtBFPEQv823eCR/jlNoVW9EO4FaB67vQxcdlvyNh+dcFWxzg2eSaSwG+WBFizEKDsqlPQ5L89DI/JFIm18crfcGDzYd0MaU0A3wd7kJFAjIZogeiTzww2VxDjV80YWhbQNUG5cizJwaMrsqmPYYRpVGWPaS/TvkECNvfisN0xmRk1hz/WZGjycRAFNFwNJQWD8JRPIMPLWZAI7itmjrU4yeanr96Nq1XLRzJvECk/s/uGeiy8ukbry4soJ3n+1rSrqPFnS/4/UpKGnw1wE2QCNSYYM9/n53AKuMK8QKBgQDPBSpyvjEQ2d3Jl9+Avn0xmnhn+n646o/Q13zagWhSrGecgmu7QJo3sCw8UcbVbUMvkGXeZZS+xKex/cHTUL5zg421700HBIC7gsGajNDOsp3A22ro74rMTZ+S6lak16dRFGDDVuWWARWAnOD3KxC3L+dudOLC+9mhHrJ0UGzUiwKBgQDEPYimtMAUbNtuzUYPgo/KxNox0+/LJ7c8ignBFgajRBRZbdQFqraD26eEjnSNh37p4tad071ULbcYpH76WM3jY3Mvg45nhSVczsGLY5RbnNt6F1hqzQauF5i0OonJaoXBToiO+Lkp542nKFowSxQO4l3DPjG7ErJFe6AhD9x8DwKBgEr0st7i0D8qsywXyjwCPLAmHux+/T0U6MeUDkfLTSuJ8uXvLvSYun3pUsrwTQ7fxdDF9qxkmUvNRm6i03ggpySKAFhcccZVsgqymEjOLZh5gbbfe0El4lqAzINUR9TdpF4uA68WmoAKmm973dSAikfZl3Ed7zDsIWs+Ax0sNYtrAoGAW5sh7DgaJi6+rFWUq+7VsuD7viDiImKyzcbeUM04YgSIv0penBqZwqnJ5D4fuGWU4Bds+hX6no4Ml6tKPxEvY0NsZLSzyC1GSLGXdbsRaUaqya6WLpzwzlnjhdaBtY1NTPSdGKiQvzgBTxOuLjcN8PjIrjidPGe44miYUna2BQ8CgYEAlmsQ92AgbUuMDWIZhiboROClhzYYQRp4Xmq9nt48opIcsUEIQ9swh05Eh/KrG6wGTDtJBYUOnTcdoLFAlAgd/iiwilV1eDsmG48lPv9F94q/FvdDPyM+XcfNEmBPG5VyAHV2gyuywgG2JXFT61vyE81q2X8+GXas5Cg3eQOHDSU=";

    /**
     * 验证 RSA 密文可以通过密码解密服务还原为明文。
     */
    @Test
    void decryptPasswordShouldReturnPlainText() throws Exception {
        MmwikiProperties properties = new MmwikiProperties();
        properties.getSecurity().getRsa().setPrivateKey(PRIVATE_KEY);
        PasswordCryptoService service = new PasswordCryptoService();
        ReflectionTestUtils.setField(service, "properties", properties);

        String encrypted = RSAUtils.encryptByPublicKey(PUBLIC_KEY, "abc123中文");

        assertEquals("abc123中文", service.decryptPassword(encrypted));
    }
}
