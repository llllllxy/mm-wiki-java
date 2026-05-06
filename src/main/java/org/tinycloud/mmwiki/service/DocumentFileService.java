package org.tinycloud.mmwiki.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.config.MmwikiProperties;
import org.tinycloud.mmwiki.domain.Document;

@Service
public class DocumentFileService {

    public static final int DOCUMENT_TYPE_PAGE = 1;
    public static final int DOCUMENT_TYPE_DIR = 2;
    public static final String DEFAULT_FILE_NAME = "README";
    public static final String PAGE_SUFFIX = ".md";

    private final Path documentRootDir;
    private final Path markdownRootDir;

    public DocumentFileService(MmwikiProperties properties) {
        this.documentRootDir = Path.of(properties.getDocumentRootDir());
        this.markdownRootDir = this.documentRootDir.resolve("markdowns");
    }

    public String getPageFileByParentPath(String name, int docType, String parentPath) {
        if (docType == DOCUMENT_TYPE_PAGE) {
            return parentPath + "/" + name + PAGE_SUFFIX;
        }
        return parentPath + "/" + name + "/" + DEFAULT_FILE_NAME + PAGE_SUFFIX;
    }

    public String getDefaultPageFileBySpaceName(String name) {
        return name + "/" + DEFAULT_FILE_NAME + PAGE_SUFFIX;
    }

    public String resolvePageFile(Document document, List<Document> parentDocuments) {
        if ("0".equals(document.getParentId())) {
            return getDefaultPageFileBySpaceName(document.getName());
        }
        StringBuilder parentPath = new StringBuilder();
        for (Document parentDocument : parentDocuments) {
            if (parentPath.length() > 0) {
                parentPath.append('/');
            }
            parentPath.append(parentDocument.getName());
        }
        return getPageFileByParentPath(document.getName(), document.getType(), parentPath.toString());
    }

    public String readPage(String pageFile) throws IOException {
        Path file = resolvePagePath(pageFile);
        if (!Files.exists(file)) {
            return "";
        }
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    public void createEmptyPage(String pageFile) throws IOException {
        Path file = resolvePagePath(pageFile);
        Files.createDirectories(file.getParent());
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
    }

    public void writePage(String pageFile, String content) throws IOException {
        Path file = resolvePagePath(pageFile);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content == null ? "" : content, StandardCharsets.UTF_8);
    }

    public void renamePageOrDirectory(String oldPageFile, String newName, int docType, boolean nameChanged) throws IOException {
        Path oldFile = resolvePagePath(oldPageFile);
        if (!Files.exists(oldFile)) {
            Files.createDirectories(oldFile.getParent());
            if (docType == DOCUMENT_TYPE_PAGE) {
                Files.createFile(oldFile);
            } else {
                Files.createDirectories(oldFile.getParent());
                Files.createFile(oldFile);
            }
        }
        if (!nameChanged) {
            return;
        }
        if (docType == DOCUMENT_TYPE_PAGE) {
            Path target = oldFile.getParent().resolve(newName + PAGE_SUFFIX);
            Files.move(oldFile, target, StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        Path directory = oldFile.getParent();
        Path target = directory.getParent().resolve(newName);
        Files.move(directory, target, StandardCopyOption.REPLACE_EXISTING);
    }

    public void movePageOrDirectory(String oldPageFile, String newPageFile, int docType) throws IOException {
        Path oldFile = resolvePagePath(oldPageFile);
        Path newFile = resolvePagePath(newPageFile);
        Files.createDirectories(newFile.getParent());
        if (docType == DOCUMENT_TYPE_PAGE) {
            Files.move(oldFile, newFile, StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        Files.createDirectories(newFile.getParent());
        Files.move(oldFile.getParent(), newFile.getParent(), StandardCopyOption.REPLACE_EXISTING);
    }

    public void deletePageOrDirectory(String pageFile, int docType) throws IOException {
        Path file = resolvePagePath(pageFile);
        if (!Files.exists(file) && (docType == DOCUMENT_TYPE_PAGE || !Files.exists(file.getParent()))) {
            return;
        }
        if (docType == DOCUMENT_TYPE_PAGE) {
            Files.deleteIfExists(file);
            return;
        }
        deleteRecursively(file.getParent());
    }

    public Path resolvePagePath(String pageFile) {
        return markdownRootDir.resolve(pageFile);
    }

    public Path resolveAttachmentPath(String relativePath) {
        return documentRootDir.resolve(relativePath);
    }

    public Path ensureAttachmentDirectory(String first, String second, String third) throws IOException {
        Path path = documentRootDir.resolve(first).resolve(second).resolve(third);
        Files.createDirectories(path);
        return path;
    }

    private void deleteRecursively(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try (var stream = Files.walk(path)) {
            stream.sorted((left, right) -> right.getNameCount() - left.getNameCount())
                .forEach(item -> {
                    try {
                        Files.deleteIfExists(item);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw ex;
        }
    }

    public boolean isConfigured() {
        return StringUtils.hasText(documentRootDir.toString());
    }
}
