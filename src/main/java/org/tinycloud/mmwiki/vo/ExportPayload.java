package org.tinycloud.mmwiki.vo;

import org.springframework.core.io.ByteArrayResource;

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

    public ExportPayload() {
    }

    public ExportPayload(
            String fileName,
            ByteArrayResource resource
    ) {
        this.fileName = fileName;
        this.resource = resource;
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

}
