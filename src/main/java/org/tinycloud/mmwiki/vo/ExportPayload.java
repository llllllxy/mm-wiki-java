package org.tinycloud.mmwiki.vo;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;

/**
 * ExportPayload view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class ExportPayload {

    /**
     * fileName.
     */
    private String fileName;

    /**
     * resource.
     */
    private ByteArrayResource resource;

    /**
     * contentType.
     */
    private MediaType contentType;

    public ExportPayload() {
    }

    public ExportPayload(
            String fileName,
            ByteArrayResource resource
    ) {
        this(fileName, resource, MediaType.APPLICATION_OCTET_STREAM);
    }

    public ExportPayload(
            String fileName,
            ByteArrayResource resource,
            MediaType contentType
    ) {
        this.fileName = fileName;
        this.resource = resource;
        this.contentType = contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ByteArrayResource getResource() {
        return resource;
    }

    public void setResource(ByteArrayResource resource) {
        this.resource = resource;
    }

    public MediaType getContentType() {
        return contentType;
    }

    public void setContentType(MediaType contentType) {
        this.contentType = contentType;
    }

}
