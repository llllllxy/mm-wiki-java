package org.tinycloud.mmwiki.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.tinycloud.mmwiki.config.MmwikiProperties;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.domain.EmailServer;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.mapper.EmailMapper;
import org.tinycloud.mmwiki.mapper.FollowMapper;
import org.tinycloud.mmwiki.mapper.UserMapper;
import org.tinycloud.mmwiki.web.JsonResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private EmailMapper emailMapper;
    @Mock
    private FollowMapper followMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ConfigService configService;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private MmwikiProperties properties;

    @InjectMocks
    private EmailService emailService;

    @Test
    void saveNormalizesFieldsAndStartsDisabled() {
        EmailServer emailServer = validEmail();
        emailServer.setName("  office smtp  ");
        emailServer.setHost(" smtp.example.com ");
        emailServer.setSenderAddress(" wiki@example.com ");
        emailServer.setSenderName("");
        emailServer.setSenderTitlePrefix("");
        emailServer.setUsername(" wiki-user ");
        emailServer.setPassword(" secret ");
        emailServer.setIsSsl(2);
        when(emailMapper.countByName("office smtp")).thenReturn(0L);

        JsonResponse<Void> response = emailService.save(emailServer);

        assertThat(response.getCode()).isEqualTo(ErrorCodeEnum.SUCCESS.getCode());
        ArgumentCaptor<EmailServer> captor = ArgumentCaptor.forClass(EmailServer.class);
        verify(emailMapper).insert(captor.capture());
        EmailServer inserted = captor.getValue();
        assertThat(inserted.getName()).isEqualTo("office smtp");
        assertThat(inserted.getHost()).isEqualTo("smtp.example.com");
        assertThat(inserted.getSenderAddress()).isEqualTo("wiki@example.com");
        assertThat(inserted.getSenderName()).isEqualTo("MM-Wiki");
        assertThat(inserted.getSenderTitlePrefix()).isEqualTo("[MM-Wiki]");
        assertThat(inserted.getUsername()).isEqualTo("wiki-user");
        assertThat(inserted.getPassword()).isEqualTo("secret");
        assertThat(inserted.getIsSsl()).isZero();
        assertThat(inserted.getIsUsed()).isZero();
        assertThat(inserted.getCreateTime()).isNotNull();
        assertThat(inserted.getUpdateTime()).isNotNull();
    }

    @Test
    void saveRejectsDuplicateName() {
        EmailServer emailServer = validEmail();
        when(emailMapper.countByName(emailServer.getName())).thenReturn(1L);

        assertThatThrownBy(() -> emailService.save(emailServer))
                .isInstanceOf(SystemException.class)
                .hasMessage("邮件服务器名称已经存在。");
    }

    @Test
    void markUsedClearsPreviousServerBeforeEnablingTarget() {
        EmailServer emailServer = validEmail();
        emailServer.setEmailId(7);
        when(emailMapper.findById(7)).thenReturn(emailServer);

        JsonResponse<Void> response = emailService.markUsed(7);

        assertThat(response.getCode()).isEqualTo(ErrorCodeEnum.SUCCESS.getCode());
        InOrder inOrder = inOrder(emailMapper);
        inOrder.verify(emailMapper).findById(7);
        inOrder.verify(emailMapper).clearUsed();
        inOrder.verify(emailMapper).markUsed(7);
    }

    @Test
    void markUsedReportsNotFoundWhenServerDoesNotExist() {
        when(emailMapper.findById(404)).thenReturn(null);

        assertThatThrownBy(() -> emailService.markUsed(404))
                .isInstanceOfSatisfying(SystemException.class, ex -> {
                    SystemException systemException = (SystemException) ex;
                    assertThat(systemException.getErrorCode()).isEqualTo(ErrorCodeEnum.NOT_FOUND);
                    assertThat(systemException.getMessage()).isEqualTo("邮件服务器不存在。");
                });
    }

    private static EmailServer validEmail() {
        EmailServer emailServer = new EmailServer();
        emailServer.setName("office");
        emailServer.setHost("smtp.example.com");
        emailServer.setPort(465);
        emailServer.setSenderAddress("wiki@example.com");
        emailServer.setSenderName("MM-Wiki");
        emailServer.setSenderTitlePrefix("[MM-Wiki]");
        emailServer.setUsername("wiki-user");
        emailServer.setPassword("secret");
        emailServer.setIsSsl(1);
        return emailServer;
    }
}
