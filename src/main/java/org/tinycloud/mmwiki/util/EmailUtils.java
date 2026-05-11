package org.tinycloud.mmwiki.util;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 邮件工具类，只支持 smtp 协议。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public final class EmailUtils {

    private static final Logger log = LoggerFactory.getLogger(EmailUtils.class);

    /**
     * 发送邮件。
     *
     * @param request 邮件请求参数
     * @return true 成功，false 失败
     */
    public static boolean sendMsg(EmailRequest request) {
        try {
            validateRequest(request);

            Properties props = new Properties();
            props.setProperty("mail.transport.protocol", "smtp");
            props.setProperty("mail.smtp.host", request.getHost());
            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.smtp.port", request.getPort());
            props.setProperty("mail.smtp.connectiontimeout", "15000");
            props.setProperty("mail.smtp.timeout", "15000");
            props.setProperty("mail.smtp.writetimeout", "15000");
            if (request.isSsl()) {
                props.setProperty("mail.smtp.ssl.enable", "true");
            }

            Session session = Session.getInstance(props);
            session.setDebug(request.isDebug());

            MimeMessage message = createMimeMessage(session, request);
            Transport transport = session.getTransport();
            transport.connect(request.getAccount(), request.getPassword());
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            return true;
        } catch (Exception e) {
            log.error("sendMsg is error: ", e);
            return false;
        }
    }

    private static MimeMessage createMimeMessage(Session session, EmailRequest request) throws Exception {
        MimeMessage message = new MimeMessage(session);

        if (request.getSendName() == null || request.getSendName().isEmpty()) {
            message.setFrom(new InternetAddress(request.getAccount(), null, "UTF-8"));
        } else {
            message.setFrom(new InternetAddress(request.getAccount(), request.getSendName(), "UTF-8"));
        }

        setRecipients(message, MimeMessage.RecipientType.TO, request.getToMails());
        setRecipients(message, MimeMessage.RecipientType.CC, request.getCcMails());
        setRecipients(message, MimeMessage.RecipientType.BCC, request.getBccMails());
        message.setSubject(request.getTitle() == null ? "" : request.getTitle(), "UTF-8");

        MimeMultipart msgMultipart = new MimeMultipart("mixed");
        message.setContent(msgMultipart);

        MimeBodyPart contentPart = new MimeBodyPart();
        msgMultipart.addBodyPart(contentPart);
        MimeMultipart bodyMultipart = new MimeMultipart("related");
        contentPart.setContent(bodyMultipart);
        MimeBodyPart htmlPart = new MimeBodyPart();
        bodyMultipart.addBodyPart(htmlPart);
        htmlPart.setContent(request.getContent() == null ? "" : request.getContent(), "text/html;charset=UTF-8");

        if (request.getFileList() != null && !request.getFileList().isEmpty()) {
            for (File file : request.getFileList()) {
                if (file == null || !file.exists()) {
                    continue;
                }
                try {
                    MimeBodyPart filePart = new MimeBodyPart();
                    FileDataSource dataSource = new FileDataSource(file);
                    DataHandler dataHandler = new DataHandler(dataSource);
                    filePart.setDataHandler(dataHandler);
                    filePart.setFileName(MimeUtility.encodeWord(file.getName()));
                    msgMultipart.addBodyPart(filePart);
                } catch (Exception e) {
                    log.error("send mail error fileName={}", file.getName(), e);
                }
            }
        }

        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private static void setRecipients(MimeMessage message, Message.RecipientType recipientType, List<String> mailList) throws Exception {
        if (mailList == null || mailList.isEmpty()) {
            return;
        }

        List<InternetAddress> addressList = new ArrayList<>();
        for (String mail : mailList) {
            if (mail == null || mail.trim().isEmpty()) {
                continue;
            }
            addressList.add(new InternetAddress(mail.trim()));
        }

        if (!addressList.isEmpty()) {
            message.setRecipients(recipientType, addressList.toArray(new InternetAddress[0]));
        }
    }

    private static void validateRequest(EmailRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("EmailRequest不能为空");
        }
        if (request.getAccount() == null || request.getAccount().trim().isEmpty()) {
            throw new IllegalArgumentException("发件邮箱账号不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("发件邮箱密码不能为空");
        }
        if (request.getHost() == null || request.getHost().trim().isEmpty()) {
            throw new IllegalArgumentException("SMTP服务器地址不能为空");
        }
        if (request.getPort() == null || request.getPort().trim().isEmpty()) {
            throw new IllegalArgumentException("SMTP服务器端口不能为空");
        }
        if (request.getToMails() == null || request.getToMails().isEmpty()) {
            throw new IllegalArgumentException("收件人不能为空");
        }
    }

    /**
     * 邮件请求参数。
     *
     * @author liuxingyu01
     * @since 2026-05-06
     */
    public static class EmailRequest {
        private String account;
        private String password;
        private String host;
        private String port;
        private boolean ssl;
        private boolean debug;
        private String sendName;
        private List<String> toMails;
        private List<String> ccMails;
        private List<String> bccMails;
        private String title;
        private String content;
        private List<File> fileList;

        private EmailRequest() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public String getAccount() {
            return account;
        }

        public String getPassword() {
            return password;
        }

        public String getHost() {
            return host;
        }

        public String getPort() {
            return port;
        }

        public boolean isSsl() {
            return ssl;
        }

        public boolean isDebug() {
            return debug;
        }

        public String getSendName() {
            return sendName;
        }

        public List<String> getToMails() {
            return toMails;
        }

        public List<String> getCcMails() {
            return ccMails;
        }

        public List<String> getBccMails() {
            return bccMails;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public List<File> getFileList() {
            return fileList;
        }

        /**
         * 邮件请求构造器。
         *
         * @author liuxingyu01
         * @since 2026-05-06
         */
        public static class Builder {
            private final EmailRequest target = new EmailRequest();

            public Builder account(String account) {
                target.account = account;
                return this;
            }

            public Builder password(String password) {
                target.password = password;
                return this;
            }

            public Builder host(String host) {
                target.host = host;
                return this;
            }

            public Builder port(String port) {
                target.port = port;
                return this;
            }

            public Builder ssl(boolean ssl) {
                target.ssl = ssl;
                return this;
            }

            public Builder debug(boolean debug) {
                target.debug = debug;
                return this;
            }

            public Builder sendName(String sendName) {
                target.sendName = sendName;
                return this;
            }

            public Builder toMails(List<String> toMails) {
                target.toMails = toMails;
                return this;
            }

            public Builder ccMails(List<String> ccMails) {
                target.ccMails = ccMails;
                return this;
            }

            public Builder bccMails(List<String> bccMails) {
                target.bccMails = bccMails;
                return this;
            }

            public Builder title(String title) {
                target.title = title;
                return this;
            }

            public Builder content(String content) {
                target.content = content;
                return this;
            }

            public Builder fileList(List<File> fileList) {
                target.fileList = fileList;
                return this;
            }

            public EmailRequest build() {
                return target;
            }
        }
    }
}
