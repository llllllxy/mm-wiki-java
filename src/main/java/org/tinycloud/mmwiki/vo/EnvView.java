package org.tinycloud.mmwiki.vo;

import java.util.List;
import java.util.Map;

/**
 * EnvView view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class EnvView {

    /**
     * server.
     */
    private Map<String, String> server;

    /**
     * envData.
     */
    private List<Map<String, Object>> envData;

    /**
     * dirData.
     */
    private List<Map<String, Object>> dirData;

    public EnvView() {
    }

    public EnvView(
            Map<String, String> server,
            List<Map<String, Object>> envData,
            List<Map<String, Object>> dirData
    ) {
        this.server = server;
        this.envData = envData;
        this.dirData = dirData;
    }

    public Map<String, String> getServer() {
        return server;
    }

    public void setServer(Map<String, String> server) {
        this.server = server;
    }

    public List<Map<String, Object>> getEnvData() {
        return envData;
    }

    public void setEnvData(List<Map<String, Object>> envData) {
        this.envData = envData;
    }

    public List<Map<String, Object>> getDirData() {
        return dirData;
    }

    public void setDirData(List<Map<String, Object>> dirData) {
        this.dirData = dirData;
    }

}
