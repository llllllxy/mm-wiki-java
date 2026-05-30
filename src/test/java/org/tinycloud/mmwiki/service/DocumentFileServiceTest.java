package org.tinycloud.mmwiki.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.tinycloud.mmwiki.TestFileUtils;
import org.tinycloud.mmwiki.config.MmwikiProperties;
import org.tinycloud.mmwiki.constant.DocumentTypeEnum;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.exception.SystemException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentFileServiceTest {

    private Path documentRoot;

    private DocumentFileService documentFileService;

    @BeforeEach
    void setUp() throws Exception {
        documentRoot = TestFileUtils.createTempDirectory("document-file-");
        MmwikiProperties properties = new MmwikiProperties();
        properties.setDocumentRootDir(documentRoot.toString());
        documentFileService = new DocumentFileService();
        ReflectionTestUtils.setField(documentFileService, "properties", properties);
        documentFileService.init();
    }

    @AfterEach
    void tearDown() throws Exception {
        TestFileUtils.deleteRecursively(documentRoot);
    }

    @Test
    void resolvesDefaultSpaceReadmeForRootDocument() {
        Document document = document("0", "产品空间", DocumentTypeEnum.DIRECTORY.getCode());

        String pageFile = documentFileService.resolvePageFile(document, List.of());

        assertThat(pageFile).isEqualTo("产品空间/README.md");
    }

    @Test
    void resolvesPageFileUnderParentPath() {
        Document parent = document("0", "指南", DocumentTypeEnum.DIRECTORY.getCode());
        Document child = document("parent", "安装", DocumentTypeEnum.PAGE.getCode());

        String pageFile = documentFileService.resolvePageFile(child, List.of(parent));

        assertThat(pageFile).isEqualTo("指南/安装.md");
    }

    @Test
    void writeAndReadPageUsesUtf8MarkdownRoot() throws Exception {
        documentFileService.writePage("指南/安装.md", "请用 100 字解释 Spring Boot。");

        String content = documentFileService.readPage("指南/安装.md");

        assertThat(content).isEqualTo("请用 100 字解释 Spring Boot。");
        assertThat(Files.exists(documentRoot.resolve("markdowns/指南/安装.md"))).isTrue();
    }

    @Test
    void ensureAttachmentDirectoryCreatesDirectoryUnderDocumentRoot() throws Exception {
        Path directory = documentFileService.ensureAttachmentDirectory("images", "5", "doc-1");

        assertThat(directory).isEqualTo(documentRoot.toAbsolutePath().normalize().resolve("images").resolve("5").resolve("doc-1"));
        assertThat(Files.isDirectory(directory)).isTrue();
    }

    /**
     * 验证合法附件相对路径会被解析到文档根目录内。
     */
    @Test
    void resolveAttachmentPathAllowsPathInsideDocumentRoot() {
        Path path = documentFileService.resolveAttachmentPath("attachment/5/doc-1/file.pdf");

        assertThat(path).isEqualTo(documentRoot.toAbsolutePath().normalize().resolve("attachment/5/doc-1/file.pdf"));
    }

    /**
     * 验证包含目录回退的附件路径不能越过文档根目录。
     */
    @Test
    void resolveAttachmentPathRejectsTraversalOutsideDocumentRoot() {
        assertThatThrownBy(() -> documentFileService.resolveAttachmentPath("../outside.txt"))
                .isInstanceOf(SystemException.class)
                .hasMessageContaining("附件路径不合法");
    }

    /**
     * 验证绝对路径不能绕过附件根目录限制。
     */
    @Test
    void resolveAttachmentPathRejectsAbsolutePathOutsideDocumentRoot() {
        Path outsidePath = documentRoot.toAbsolutePath().normalize().getParent().resolve("outside.txt");

        assertThatThrownBy(() -> documentFileService.resolveAttachmentPath(outsidePath.toString()))
                .isInstanceOf(SystemException.class)
                .hasMessageContaining("附件路径不合法");
    }

    @Test
    void deleteDirectoryDocumentRemovesWholeDirectory() throws Exception {
        documentFileService.writePage("指南/README.md", "index");
        documentFileService.writePage("指南/子页面.md", "child");

        documentFileService.deletePageOrDirectory("指南/README.md", DocumentTypeEnum.DIRECTORY.getCode());

        assertThat(Files.exists(documentRoot.resolve("markdowns/指南"))).isFalse();
    }

    private static Document document(String parentId, String name, int type) {
        Document document = new Document();
        document.setParentId(parentId);
        document.setName(name);
        document.setType(type);
        return document;
    }
}
