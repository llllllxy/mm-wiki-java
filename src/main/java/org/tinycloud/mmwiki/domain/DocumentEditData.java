package org.tinycloud.mmwiki.domain;

public record DocumentEditData(
    Document document,
    String pageContent,
    String sendEmail,
    String autoFollowDoc
) {
}
