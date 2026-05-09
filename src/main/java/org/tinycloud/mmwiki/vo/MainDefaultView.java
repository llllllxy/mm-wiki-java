package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.Contact;
import org.tinycloud.mmwiki.domain.Link;
import org.tinycloud.mmwiki.domain.LogDocumentView;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * MainDefaultView view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class MainDefaultView {

    /**
     * panelTitle.
     */
    private String panelTitle;

    /**
     * panelDescription.
     */
    private String panelDescription;

    /**
     * logDocuments.
     */
    private List<LogDocumentView> logDocuments;

    /**
     * links.
     */
    private List<Link> links;

    /**
     * contacts.
     */
    private List<Contact> contacts;

    /**
     * paginator.
     */
    private Paginator paginator;

    public MainDefaultView() {
    }

    public MainDefaultView(
            String panelTitle,
            String panelDescription,
            List<LogDocumentView> logDocuments,
            List<Link> links,
            List<Contact> contacts,
            Paginator paginator
    ) {
        this.panelTitle = panelTitle;
        this.panelDescription = panelDescription;
        this.logDocuments = logDocuments;
        this.links = links;
        this.contacts = contacts;
        this.paginator = paginator;
    }

    public String getPanelTitle() {
        return panelTitle;
    }

    public void setPanelTitle(String panelTitle) {
        this.panelTitle = panelTitle;
    }

    public String getPanelDescription() {
        return panelDescription;
    }

    public void setPanelDescription(String panelDescription) {
        this.panelDescription = panelDescription;
    }

    public List<LogDocumentView> getLogDocuments() {
        return logDocuments;
    }

    public void setLogDocuments(List<LogDocumentView> logDocuments) {
        this.logDocuments = logDocuments;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }

}
