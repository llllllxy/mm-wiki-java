package org.tinycloud.mmwiki.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.tinycloud.mmwiki.TestFileUtils;
import org.tinycloud.mmwiki.domain.Document;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfExportServiceTest {

    private Path tempDir;

    @Mock
    private DocumentFileService documentFileService;

    private PdfExportService pdfExportService;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = TestFileUtils.createTempDirectory("pdf-export-");
        pdfExportService = new PdfExportService();
        ReflectionTestUtils.setField(pdfExportService, "documentFileService", documentFileService);
    }

    @AfterEach
    void tearDown() throws Exception {
        TestFileUtils.deleteRecursively(tempDir);
    }

    @Test
    void renderMarkdownKeepsChineseInsideCodeBlock() {
        String html = ReflectionTestUtils.invokeMethod(pdfExportService, "renderMarkdown",
                "```text\n请用 100 字解释 Spring Boot。\n```");

        assertThat(html).contains("<pre>");
        assertThat(html).contains("请用 100 字解释 Spring Boot。");
    }

    @Test
    void buildHtmlUsesCjkFontForCodeBlocksAndEscapesTitle() {
        Document document = new Document();
        document.setName("<测试>");

        String html = ReflectionTestUtils.invokeMethod(pdfExportService, "buildHtml", document, "<p>正文</p>");

        assertThat(html).contains("&lt;测试&gt;");
        assertThat(html).contains("font-family: 'MMWikiCJK', 'Courier New', monospace;");
        assertThat(html).contains("<article><p>正文</p></article>");
    }

    @Test
    void inlineLocalImagesEmbedsUploadedImagesAsDataUri() throws Exception {
        Path image = tempDir.resolve("image.png");
        Files.write(image, minimalPngBytes());
        when(documentFileService.resolveAttachmentPath("images/5/doc-1/image (1).png")).thenReturn(image);
        String html = "<p><img src=\"/images/5/doc-1/image%20(1).png\" alt=\"image\" /></p>";

        String inlined = ReflectionTestUtils.invokeMethod(pdfExportService, "inlineLocalImages", html);

        assertThat(inlined).contains("src=\"data:image/");
        assertThat(inlined).contains(";base64,");
        assertThat(inlined).doesNotContain("/images/5/doc-1/image%20(1).png");
    }

    @Test
    void inlineLocalImagesLeavesExternalImagesUntouched() {
        String html = "<p><img src=\"https://example.com/a.png\" /></p>";

        String inlined = ReflectionTestUtils.invokeMethod(pdfExportService, "inlineLocalImages", html);

        assertThat(inlined).isEqualTo(html);
    }

    private static byte[] minimalPngBytes() {
        return new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, 0x77, 0x53,
                (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41,
                0x54, 0x08, (byte) 0xD7, 0x63, (byte) 0xF8, (byte) 0xCF,
                (byte) 0xC0, 0x00, 0x00, 0x03, 0x01, 0x01, 0x00,
                0x18, (byte) 0xDD, (byte) 0x8D, (byte) 0xB0, 0x00, 0x00,
                0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42,
                0x60, (byte) 0x82
        };
    }
}
