package org.tinycloud.mmwiki.web;

import org.junit.jupiter.api.Test;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonResponseTest {

    @Test
    void successUsesUnifiedSuccessCodeAndRedirectPayload() {
        JsonResponse<Void> response = JsonResponse.success("保存成功", "/system/user/list", 1500);

        assertThat(response.getCode()).isEqualTo(ErrorCodeEnum.SUCCESS.getCode());
        assertThat(response.getMessage()).isEqualTo("保存成功");
        assertThat(response.getRedirect()).containsEntry("url", "/system/user/list");
        assertThat(response.getRedirect()).containsEntry("sleep", 1500);
    }

    @Test
    void errorCanCarrySpecificBusinessCode() {
        JsonResponse<Void> response = JsonResponse.error(ErrorCodeEnum.FORBIDDEN, "没有权限", "/error/403");

        assertThat(response.getCode()).isEqualTo(ErrorCodeEnum.FORBIDDEN.getCode());
        assertThat(response.getMessage()).isEqualTo("没有权限");
        assertThat(response.getRedirect()).containsEntry("url", "/error/403");
    }

    @Test
    void pageModelCalculatesTotalPageWhenBuiltManually() {
        PageModel<String> pageModel = PageModel.build(1L, 10L, List.of("a", "b"), 21L);

        assertThat(pageModel.getPageNo()).isEqualTo(1L);
        assertThat(pageModel.getPageSize()).isEqualTo(10L);
        assertThat(pageModel.getTotalCount()).isEqualTo(21L);
        assertThat(pageModel.getTotalPage()).isEqualTo(3L);
        assertThat(pageModel.getRecords()).containsExactly("a", "b");
    }
}
