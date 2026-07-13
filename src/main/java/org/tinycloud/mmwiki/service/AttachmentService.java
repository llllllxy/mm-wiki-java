package org.tinycloud.mmwiki.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.tinycloud.paginate.Page;
import org.tinycloud.paginate.request.PaginateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tinycloud.mmwiki.constant.AttachmentSourceEnum;
import org.tinycloud.mmwiki.domain.Attachment;
import org.tinycloud.mmwiki.mapper.AttachmentMapper;
import org.tinycloud.mmwiki.web.PageModel;

/**
 * 附件业务服务，负责附件元数据查询、保存和删除时的磁盘文件清理。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class AttachmentService {

    @Autowired
    private AttachmentMapper attachmentMapper;
    @Autowired
    private DocumentFileService documentFileService;

    /**
     * 查询指定文档下的全部附件记录。
     *
     * @param documentId 文档ID
     * @return 文档关联的附件列表
     */
    public List<Attachment> findByDocumentId(String documentId) {
        return this.attachmentMapper.findByDocumentId(documentId);
    }

    /**
     * 查询指定空间下未删除文档关联的全部附件记录。
     *
     * @param spaceId 空间ID
     * @return 空间内文档关联的附件列表
     */
    public List<Attachment> findBySpaceId(Integer spaceId) {
        return this.attachmentMapper.findBySpaceId(spaceId);
    }

    /**
     * 按文档ID和附件来源查询附件，用于区分普通附件和编辑器图片。
     *
     * @param documentId 文档ID
     * @param source     附件来源类型
     * @return 匹配来源的附件列表
     */
    public List<Attachment> findByDocumentIdAndSource(String documentId, AttachmentSourceEnum source) {
        return this.attachmentMapper.findByDocumentIdAndSource(documentId, source.getCode());
    }

    /**
     * 分页查询指定文档下的附件列表，供 bootstrap-table 异步加载普通附件和图片附件。
     *
     * @param documentId 文档ID
     * @param source     附件来源类型
     * @param pageNum    当前页码
     * @param pageSize   每页数量
     * @return 附件分页数据
     */
    public PageModel<Attachment> pageByDocumentIdAndSource(String documentId, AttachmentSourceEnum source, int pageNum, int pageSize) {
        Page<Attachment> pageInfo = PaginateRequest.of(pageNum, pageSize)
                .request(() -> this.attachmentMapper.findByDocumentIdAndSource(documentId, source.getCode()));
        return PageModel.from(pageInfo);
    }

    /**
     * 根据附件ID查询单条附件记录。
     *
     * @param attachmentId 附件ID
     * @return 附件记录，不存在时返回 null
     */
    public Attachment findById(Integer attachmentId) {
        return this.attachmentMapper.findById(attachmentId);
    }

    /**
     * 根据文档ID、存储路径和来源查找附件，主要用于图片直链访问前的归属校验。
     *
     * @param documentId 文档ID
     * @param path       附件存储相对路径
     * @param source     附件来源类型
     * @return 匹配的附件记录，不存在时返回 null
     */
    public Attachment findByDocumentIdPathAndSource(String documentId, String path, AttachmentSourceEnum source) {
        return this.attachmentMapper.findByDocumentIdPathAndSource(documentId, path, source.getCode());
    }

    /**
     * 保存附件元数据，文件本身由调用方写入磁盘。
     *
     * @param userId     上传用户ID
     * @param documentId 关联文档ID
     * @param name       原始附件名称
     * @param path       附件存储相对路径
     * @param source     附件来源类型
     * @return 新增后的附件实体
     */
    public Attachment save(Integer userId, String documentId, String name, String path, AttachmentSourceEnum source) {
        LocalDateTime now = LocalDateTime.now();
        Attachment attachment = new Attachment();
        attachment.setUserId(userId);
        attachment.setDocumentId(documentId);
        attachment.setName(name);
        attachment.setPath(path);
        attachment.setSource(source.getCode());
        attachment.setCreateTime(now);
        attachment.setUpdateTime(now);
        this.attachmentMapper.insert(attachment);
        return attachment;
    }

    /**
     * 删除单个附件，先根据附件记录解析并删除磁盘文件，再删除数据库记录。
     *
     * @param attachmentId 附件ID
     * @throws IOException 删除磁盘文件失败时抛出
     */
    @Transactional
    public void deleteById(Integer attachmentId) throws IOException {
        Attachment attachment = this.attachmentMapper.findById(attachmentId);
        if (attachment == null) {
            return;
        }
        Path file = this.documentFileService.resolveAttachmentPath(attachment.getPath());
        Files.deleteIfExists(file);
        this.attachmentMapper.deleteById(attachmentId);
    }

    /**
     * 删除指定文档下的全部附件，逐个清理磁盘文件后删除数据库记录。
     *
     * @param documentId 文档ID
     * @throws IOException 删除磁盘文件失败时抛出
     */
    @Transactional
    public void deleteByDocumentId(String documentId) throws IOException {
        List<Attachment> attachments = this.attachmentMapper.findByDocumentId(documentId);
        for (Attachment attachment : attachments) {
            Path file = this.documentFileService.resolveAttachmentPath(attachment.getPath());
            Files.deleteIfExists(file);
        }
        this.attachmentMapper.deleteByDocumentId(documentId);
    }
}
