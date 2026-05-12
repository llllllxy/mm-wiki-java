package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tinycloud.mmwiki.domain.Attachment;
import org.tinycloud.mmwiki.mapper.AttachmentMapper;
import org.tinycloud.mmwiki.util.TimeUtils;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class AttachmentService {

    public static final int SOURCE_ATTACHMENT = 0;
    public static final int SOURCE_IMAGE = 1;

    @Autowired
    private AttachmentMapper attachmentMapper;
    @Autowired
    private DocumentFileService documentFileService;

    public List<Attachment> findByDocumentId(String documentId) {
        return attachmentMapper.findByDocumentId(documentId);
    }

    public List<Attachment> findBySpaceId(Integer spaceId) {
        return attachmentMapper.findBySpaceId(spaceId);
    }

    public List<Attachment> findByDocumentIdAndSource(String documentId, int source) {
        return attachmentMapper.findByDocumentIdAndSource(documentId, source);
    }

    public Attachment findById(Integer attachmentId) {
        return attachmentMapper.findById(attachmentId);
    }

    public Attachment save(Integer userId, String documentId, String name, String path, int source) {
        LocalDateTime now = LocalDateTime.now();
        Attachment attachment = new Attachment();
        attachment.setUserId(userId);
        attachment.setDocumentId(documentId);
        attachment.setName(name);
        attachment.setPath(path);
        attachment.setSource(source);
        attachment.setCreateTime(now);
        attachment.setUpdateTime(now);
        attachmentMapper.insert(attachment);
        return attachment;
    }

    @Transactional
    public void deleteById(Integer attachmentId) throws IOException {
        Attachment attachment = attachmentMapper.findById(attachmentId);
        if (attachment == null) {
            return;
        }
        Path file = documentFileService.resolveAttachmentPath(attachment.getPath());
        Files.deleteIfExists(file);
        attachmentMapper.deleteById(attachmentId);
    }

    @Transactional
    public void deleteByDocumentId(String documentId) throws IOException {
        List<Attachment> attachments = attachmentMapper.findByDocumentId(documentId);
        for (Attachment attachment : attachments) {
            Path file = documentFileService.resolveAttachmentPath(attachment.getPath());
            Files.deleteIfExists(file);
        }
        attachmentMapper.deleteByDocumentId(documentId);
    }
}

