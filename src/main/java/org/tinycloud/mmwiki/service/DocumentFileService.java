package org.tinycloud.mmwiki.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.config.MmwikiProperties;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.exception.SystemException;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class DocumentFileService {

    public static final int DOCUMENT_TYPE_PAGE = 1;
    public static final int DOCUMENT_TYPE_DIR = 2;
    public static final String DEFAULT_FILE_NAME = "README";
    public static final String PAGE_SUFFIX = ".md";

    private Path documentRootDir;
    private Path markdownRootDir;

    @Autowired
    private MmwikiProperties properties;

    /**
     * 初始化文档根目录和 Markdown 根目录，统一转换为规范化绝对路径。
     */
    @PostConstruct
    public void init() {
        this.documentRootDir = Path.of(properties.getDocumentRootDir()).toAbsolutePath().normalize();
        this.markdownRootDir = this.documentRootDir.resolve("markdowns");
    }

    /**
     * 根据父级路径、文档名称和文档类型生成页面文件相对路径。
     *
     * @param name       文档名称
     * @param docType    文档类型，页面或目录
     * @param parentPath 父级目录相对路径
     * @return 页面 Markdown 文件相对路径
     */
    public String getPageFileByParentPath(String name, int docType, String parentPath) {
        if (docType == DOCUMENT_TYPE_PAGE) {
            return parentPath + "/" + name + PAGE_SUFFIX;
        }
        return parentPath + "/" + name + "/" + DEFAULT_FILE_NAME + PAGE_SUFFIX;
    }

    /**
     * 根据空间名称生成空间默认 README 文件相对路径。
     *
     * @param name 空间名称
     * @return 空间默认 Markdown 文件相对路径
     */
    public String getDefaultPageFileBySpaceName(String name) {
        return name + "/" + DEFAULT_FILE_NAME + PAGE_SUFFIX;
    }

    /**
     * 根据文档和父级文档链解析 Markdown 文件相对路径。
     *
     * @param document        当前文档
     * @param parentDocuments 从根到父级的文档链
     * @return 当前文档对应的 Markdown 文件相对路径
     */
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

    /**
     * 读取指定 Markdown 文件内容，文件不存在时返回空字符串。
     *
     * @param pageFile Markdown 文件相对路径
     * @return Markdown 文本内容
     * @throws IOException 读取文件失败时抛出
     */
    public String readPage(String pageFile) throws IOException {
        Path file = resolvePagePath(pageFile);
        if (!Files.exists(file)) {
            return "";
        }
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    /**
     * 创建空 Markdown 页面文件，父目录不存在时会先创建父目录。
     *
     * @param pageFile Markdown 文件相对路径
     * @throws IOException 创建目录或文件失败时抛出
     */
    public void createEmptyPage(String pageFile) throws IOException {
        Path file = resolvePagePath(pageFile);
        Files.createDirectories(file.getParent());
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
    }

    /**
     * 写入 Markdown 页面内容，父目录不存在时会先创建父目录。
     *
     * @param pageFile Markdown 文件相对路径
     * @param content  页面内容，null 会按空字符串写入
     * @throws IOException 写入文件失败时抛出
     */
    public void writePage(String pageFile, String content) throws IOException {
        Path file = resolvePagePath(pageFile);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content == null ? "" : content, StandardCharsets.UTF_8);
    }

    /**
     * 根据文档类型重命名页面文件或目录，名称未变化时仅保证旧文件存在。
     *
     * @param oldPageFile 旧 Markdown 文件相对路径
     * @param newName     新文档名称
     * @param docType     文档类型，页面或目录
     * @param nameChanged 名称是否发生变化
     * @throws IOException 文件或目录操作失败时抛出
     */
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

    /**
     * 移动页面文件或目录到新的 Markdown 相对路径。
     *
     * @param oldPageFile 原 Markdown 文件相对路径
     * @param newPageFile 目标 Markdown 文件相对路径
     * @param docType     文档类型，页面或目录
     * @throws IOException 文件或目录移动失败时抛出
     */
    public void movePageOrDirectory(String oldPageFile, String newPageFile, int docType) throws IOException {
        Path oldFile = resolvePagePath(oldPageFile);
        Path newFile = resolvePagePath(newPageFile);
        Files.createDirectories(newFile.getParent());
        if (docType == DOCUMENT_TYPE_PAGE) {
            Files.move(oldFile, newFile);
            return;
        }
        Files.createDirectories(newFile.getParent());
        Files.move(oldFile.getParent(), newFile.getParent());
    }

    /**
     * 删除页面文件或目录文档对应的整个目录。
     *
     * @param pageFile Markdown 文件相对路径
     * @param docType  文档类型，页面或目录
     * @throws IOException 删除文件或目录失败时抛出
     */
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

    /**
     * 将 Markdown 相对路径解析为 Markdown 根目录下的实际文件路径。
     *
     * @param pageFile Markdown 文件相对路径
     * @return 实际文件路径
     */
    public Path resolvePagePath(String pageFile) {
        return markdownRootDir.resolve(pageFile);
    }

    /**
     * 解析附件相对路径，并确保最终路径仍位于文档根目录内。
     */
    public Path resolveAttachmentPath(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            throw new SystemException("附件路径不能为空。");
        }
        Path path = documentRootDir.resolve(relativePath).normalize();
        if (!path.startsWith(documentRootDir)) {
            throw new SystemException("附件路径不合法。");
        }
        return path;
    }

    /**
     * 创建并返回附件存储目录，目录层级由调用方传入。
     *
     * @param first  第一级目录，通常为 attachment 或 images
     * @param second 第二级目录，通常为空间ID
     * @param third  第三级目录，通常为文档ID
     * @return 已创建或已存在的附件目录路径
     * @throws IOException 创建目录失败时抛出
     */
    public Path ensureAttachmentDirectory(String first, String second, String third) throws IOException {
        Path path = documentRootDir.resolve(first).resolve(second).resolve(third);
        Files.createDirectories(path);
        return path;
    }

    /**
     * 递归删除指定路径及其子文件，删除顺序从子节点到父节点。
     *
     * @param path 待删除的文件或目录路径
     * @throws IOException 删除失败时抛出
     */
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

    /**
     * 判断文档根目录配置是否存在。
     *
     * @return true 表示已配置文档根目录
     */
    public boolean isConfigured() {
        return StringUtils.hasText(documentRootDir.toString());
    }
}
