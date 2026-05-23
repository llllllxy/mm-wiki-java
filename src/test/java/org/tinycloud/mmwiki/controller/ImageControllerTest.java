package org.tinycloud.mmwiki.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.tinycloud.mmwiki.TestFileUtils;
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
import org.tinycloud.mmwiki.web.EditorImageResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

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

    private ImageController imageController;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = TestFileUtils.createTempDirectory("image-upload-");
        imageController = new ImageController();
        ReflectionTestUtils.setField(imageController, "documentService", documentService);
        ReflectionTestUtils.setField(imageController, "spaceService", spaceService);
        ReflectionTestUtils.setField(imageController, "accessService", accessService);
        ReflectionTestUtils.setField(imageController, "attachmentService", attachmentService);
        ReflectionTestUtils.setField(imageController, "documentFileService", documentFileService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(AuthInterceptor.SESSION_AUTHOR, currentUser());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() throws Exception {
        RequestContextHolder.resetRequestAttributes();
        TestFileUtils.deleteRecursively(tempDir);
    }

    @Test
    void uploadStoresImageWithGeneratedSafeFileNameAndKeepsOriginalNameInAttachment() throws Exception {
        Document document = document();
        Space space = new Space();
        Path saveDir = tempDir.resolve("images/5/doc-1");
        Files.createDirectories(saveDir);
        MockMultipartFile file = new MockMultipartFile("editormd-image-file", "image (1).PNG", "image/png", new byte[]{1, 2, 3});
        when(documentService.findActiveById("doc-1")).thenReturn(document);
        when(spaceService.requireSpace(5)).thenReturn(space);
        when(accessService.access(org.mockito.ArgumentMatchers.any(CurrentUser.class), eq(space))).thenReturn(new Access(true, true, false));
        when(documentFileService.ensureAttachmentDirectory("images", "5", "doc-1")).thenReturn(saveDir);

        EditorImageResponse response = imageController.upload("doc-1", file);

        assertThat(response.getSuccess()).isEqualTo(1);
        assertThat(response.getUrl()).startsWith("/images/5/doc-1/");
        assertThat(response.getUrl()).endsWith(".png");
        assertThat(response.getUrl()).doesNotContain("image (1)");
        List<Path> files;
        try (Stream<Path> stream = Files.list(saveDir)) {
            files = stream.toList();
        }
        assertThat(files).hasSize(1);
        assertThat(files.get(0).getFileName().toString()).matches("[a-f0-9]{32}\\.png");
        verify(attachmentService).save(eq(9), eq("doc-1"), eq("image (1).PNG"),
                org.mockito.ArgumentMatchers.matches("images/5/doc-1/[a-f0-9]{32}\\.png"),
                eq(AttachmentService.SOURCE_IMAGE));
    }

    private static CurrentUser currentUser() {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setUserId(9);
        currentUser.setUsername("editor");
        currentUser.setRoleId(3);
        return currentUser;
    }

    private static Document document() {
        Document document = new Document();
        document.setDocumentId("doc-1");
        document.setSpaceId(5);
        document.setName("文档");
        return document;
    }
}
