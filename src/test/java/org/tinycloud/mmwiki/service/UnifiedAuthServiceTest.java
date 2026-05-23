package org.tinycloud.mmwiki.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnifiedAuthServiceTest {

    /**
     * 验证 LDAP filter 特殊字符会被正确转义。
     */
    @Test
    void escapeLdapFilterValueShouldEscapeSpecialChars() {
        assertEquals("admin\\2a\\28x\\29\\5c\\00", UnifiedAuthService.escapeLdapFilterValue("admin*(x)\\\u0000"));
    }
}
