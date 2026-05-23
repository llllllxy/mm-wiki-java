package org.tinycloud.mmwiki.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tinycloud.mmwiki.TestFileUtils;
import org.tinycloud.mmwiki.domain.Attachment;
import org.tinycloud.mmwiki.mapper.AttachmentMapper;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    private Path tempDir;

    @Mock
    private AttachmentMapper attachmentMapper;
    @Mock
    private DocumentFileService documentFileService;

    @InjectMocks
    private AttachmentService attachmentService;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = TestFileUtils.createTempDirectory("attachment-");
    }

    @AfterEach
    void tearDown() throws Exception {
        TestFileUtils.deleteRecursively(tempDir);
    }

    @Test
    void savePersistsAttachmentMetadataWithoutChangingOriginalName() {
        Attachment attachment = attachmentService.save(3, "doc-1", "image (1).png",
                "images/5/doc-1/uuid.png", AttachmentService.SOURCE_IMAGE);

        ArgumentCaptor<Attachment> captor = ArgumentCaptor.forClass(Attachment.class);
        verify(attachmentMapper).insert(captor.capture());
        Attachment inserted = captor.getValue();
        assertThat(inserted).isSameAs(attachment);
        assertThat(inserted.getUserId()).isEqualTo(3);
        assertThat(inserted.getDocumentId()).isEqualTo("doc-1");
        assertThat(inserted.getName()).isEqualTo("image (1).png");
        assertThat(inserted.getPath()).isEqualTo("images/5/doc-1/uuid.png");
        assertThat(inserted.getSource()).isEqualTo(AttachmentService.SOURCE_IMAGE);
        assertThat(inserted.getCreateTime()).isNotNull();
        assertThat(inserted.getUpdateTime()).isNotNull();
    }

    @Test
    void deleteByIdDeletesPhysicalFileBeforeDatabaseRecord() throws Exception {
        Path file = tempDir.resolve("file.txt");
        Files.writeString(file, "content");
        Attachment attachment = new Attachment();
        attachment.setAttachmentId(9);
        attachment.setPath("attachments/file.txt");
        when(attachmentMapper.findById(9)).thenReturn(attachment);
        when(documentFileService.resolveAttachmentPath("attachments/file.txt")).thenReturn(file);

        attachmentService.deleteById(9);

        assertThat(Files.exists(file)).isFalse();
        verify(attachmentMapper).deleteById(9);
    }

    @Test
    void deleteByIdIgnoresMissingAttachment() throws Exception {
        when(attachmentMapper.findById(9)).thenReturn(null);

        attachmentService.deleteById(9);

        verify(documentFileService, never()).resolveAttachmentPath(org.mockito.ArgumentMatchers.anyString());
        verify(attachmentMapper, never()).deleteById(9);
    }
}
