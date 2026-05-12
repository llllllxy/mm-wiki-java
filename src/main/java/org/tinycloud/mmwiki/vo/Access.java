package org.tinycloud.mmwiki.vo;

/**
 * Access view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class Access {

    /**
     * visit.
     */
    private boolean visit;

    /**
     * editor.
     */
    private boolean editor;

    /**
     * manager.
     */
    private boolean manager;

    public Access() {
    }

    public Access(boolean visit, boolean editor, boolean manager) {
        this.visit = visit;
        this.editor = editor;
        this.manager = manager;
    }

    public boolean isVisit() {
        return visit;
    }

    public void setVisit(boolean visit) {
        this.visit = visit;
    }

    public boolean isEditor() {
        return editor;
    }

    public void setEditor(boolean editor) {
        this.editor = editor;
    }

    public boolean isManager() {
        return manager;
    }

    public void setManager(boolean manager) {
        this.manager = manager;
    }

}
