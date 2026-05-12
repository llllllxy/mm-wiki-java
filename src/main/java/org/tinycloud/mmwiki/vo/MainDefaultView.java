package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.Contact;
import org.tinycloud.mmwiki.domain.Link;

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
     * links.
     */
    private List<Link> links;

    /**
     * contacts.
     */
    private List<Contact> contacts;

    public MainDefaultView() {
    }

    public MainDefaultView(
            String panelTitle,
            String panelDescription,
            List<Link> links,
            List<Contact> contacts
    ) {
        this.panelTitle = panelTitle;
        this.panelDescription = panelDescription;
        this.links = links;
        this.contacts = contacts;
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

}
