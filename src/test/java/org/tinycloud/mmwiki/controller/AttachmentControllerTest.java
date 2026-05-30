package org.tinycloud.mmwiki.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.core.io.PathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.tinycloud.mmwiki.TestFileUtils;
import org.tinycloud.mmwiki.constant.AttachmentSourceEnum;
import org.tinycloud.mmwiki.constant.GlobalConstant;
import org.tinycloud.mmwiki.domain.Attachment;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.domain.Space;
import org.tinycloud.mmwiki.service.AccessService;
import org.tinycloud.mmwiki.service.AttachmentService;
import org.tinycloud.mmwiki.service.DocumentFileService;
import org.tinycloud.mmwiki.service.DocumentService;
import org.tinycloud.mmwiki.service.SpaceService;
import org.tinycloud.mmwiki.vo.Access;
import org.tinycloud.mmwiki.web.AuthInterceptor;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.JsonResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentControllerTest {

    private Path tempDir;

    @Mock
    private DocumentService documentService;
    @Mock
    private SpaceService spaceService;
    @Mock
    private AccessService accessService;
    @Mock
    private AttachmentService attachmentService;
    @Mock
    private DocumentFileService documentFileService;

    private AttachmentController attachmentController;

    /**
     * 初始化附件上传控制器测试依赖和当前登录用户会话。
     */
    @BeforeEach
    void setUp() throws Exception {
        tempDir = TestFileUtils.createTempDirectory("attachment-upload-");
        attachmentController = new AttachmentController();
        ReflectionTestUtils.setField(attachmentController, "documentService", documentService);
        ReflectionTestUtils.setField(attachmentController, "spaceService", spaceService);
        ReflectionTestUtils.setField(attachmentController, "accessService", accessService);
        ReflectionTestUtils.setField(attachmentController, "attachmentService", attachmentService);
        ReflectionTestUtils.setField(attachmentController, "documentFileService", documentFileService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(GlobalConstant.SESSION_AUTHOR, currentUser());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    /**
     * 清理附件上传测试产生的临时文件和请求上下文。
     */
    @AfterEach
    void tearDown() throws Exception {
        RequestContextHolder.resetRequestAttributes();
        TestFileUtils.deleteRecursively(tempDir);
    }

    /**
     * 验证普通附件上传使用 UUID 安全文件名保存，并保留原始文件名作为附件名称。
     */
    @Test
    void uploadStoresAttachmentWithGeneratedSafeFileNameAndKeepsOriginalNameInAttachment() throws Exception {
        Document document = document();
        Space space = new Space();
        Path saveDir = tempDir.resolve("attachment/5/doc-1");
        Files.createDirectories(saveDir);
        MockMultipartFile file = new MockMultipartFile("attachment", "report (1).PDF", "application/pdf", new byte[]{1, 2, 3});
        when(documentService.findActiveById("doc-1")).thenReturn(document);
        when(spaceService.requireSpace(5)).thenReturn(space);
        when(accessService.access(org.mockito.ArgumentMatchers.any(CurrentUser.class), eq(space))).thenReturn(new Access(true, true, false));
        when(documentFileService.ensureAttachmentDirectory("attachment", "5", "doc-1")).thenReturn(saveDir);

        JsonResponse<Void> response = attachmentController.upload("doc-1", file);

        assertThat(response.getCode()).isEqualTo(1);
        List<Path> files;
        try (Stream<Path> stream = Files.list(saveDir)) {
            files = stream.toList();
        }
        assertThat(files).hasSize(1);
        assertThat(files.get(0).getFileName().toString()).matches("[a-f0-9]{32}\\.pdf");
        verify(attachmentService).save(eq(9), eq("doc-1"), eq("report (1).PDF"),
                org.mockito.ArgumentMatchers.matches("attachment/5/doc-1/[a-f0-9]{32}\\.pdf"),
                eq(AttachmentSourceEnum.ATTACHMENT));
    }

    /**
     * 验证附件下载响应头使用 RFC5987 编码文件名，避免直接拼接原始文件名。
     */
    @Test
    void downloadUsesEncodedContentDispositionForOriginalFileName() throws Exception {
        Document document = document();
        Space space = new Space();
        Path file = tempDir.resolve("attachment/5/doc-1/file.pdf");
        Files.createDirectories(file.getParent());
        Files.write(file, new byte[]{1, 2, 3});
        Attachment attachment = new Attachment();
        attachment.setAttachmentId(7);
        attachment.setDocumentId("doc-1");
        attachment.setName("中文报告.pdf");
        attachment.setPath("attachment/5/doc-1/file.pdf");
        when(attachmentService.findById(7)).thenReturn(attachment);
        when(documentService.findActiveById("doc-1")).thenReturn(document);
        when(spaceService.requireSpace(5)).thenReturn(space);
        when(accessService.access(org.mockito.ArgumentMatchers.any(CurrentUser.class), eq(space))).thenReturn(new Access(true, false, false));
        when(documentFileService.resolveAttachmentPath("attachment/5/doc-1/file.pdf")).thenReturn(file);

        ResponseEntity<PathResource> response = attachmentController.download(7);

        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertThat(contentDisposition).startsWith("attachment;");
        assertThat(contentDisposition).contains("filename*=UTF-8''");
        assertThat(contentDisposition).contains("%E4%B8%AD%E6%96%87%E6%8A%A5%E5%91%8A.pdf");
        assertThat(contentDisposition).doesNotContain("中文报告.pdf");
    }

    /**
     * 构造测试用当前登录用户。
     */
    private static CurrentUser currentUser() {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setUserId(9);
        currentUser.setUsername("editor");
        currentUser.setRoleId(3);
        return currentUser;
    }

    /**
     * 构造测试用文档。
     */
    private static Document document() {
        Document document = new Document();
        document.setDocumentId("doc-1");
        document.setSpaceId(5);
        document.setName("文档");
        return document;
    }
}
