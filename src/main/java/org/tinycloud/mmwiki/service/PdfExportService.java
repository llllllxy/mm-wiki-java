package org.tinycloud.mmwiki.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;
import org.tinycloud.mmwiki.config.MmwikiProperties;
import org.tinycloud.mmwiki.domain.Document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文档 PDF 导出服务。
 *
 * <p>将 Markdown 渲染为 HTML 后再生成 PDF，便于复用 Markdown 的表格、列表、代码块等展示能力。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-11
 */
@Service
public class PdfExportService {
    private static final Logger log = LoggerFactory.getLogger(PdfExportService.class);

    /**
     * 匹配 Markdown 转成 HTML 后的 img 标签 src 属性，用于把本地图片内嵌到 PDF。
     */
    private static final Pattern IMAGE_SRC_PATTERN = Pattern.compile("(?i)(<img\\b[^>]*?\\bsrc\\s*=\\s*)([\"'])(.*?)(\\2)");

    /**
     * 项目内置中文字体候选列表。
     *
     * <p>打成 jar 后仍然可以通过 classpath 读取，适合 Linux/容器部署场景。</p>
     */
    private static final List<String> BUNDLED_CJK_FONTS = List.of(
            "classpath:/fonts/pdf/SourceHanSerifCN-Regular.ttf",
            "classpath:/fonts/pdf/SimHei.ttf",
            "classpath:/fonts/pdf/Alibaba-PuHuiTi-Regular.ttf"
    );

    /**
     * classpath 字体提取缓存。
     *
     * <p>jar 内字体不能稳定当作普通 File 读取，所以首次使用时复制到固定临时目录，后续复用同一个文件。</p>
     */
    private static final Map<String, Path> CLASSPATH_FONT_FILE_CACHE = new ConcurrentHashMap<>();

    /**
     * Markdown 解析器，负责把 Markdown 文本解析成语法树。
     */
    private final Parser markdownParser;

    /**
     * HTML 渲染器，负责把 Markdown 语法树渲染为 HTML 片段。
     */
    private final HtmlRenderer htmlRenderer;

    /**
     * 文档文件服务，用于解析文档图片在磁盘中的实际路径。
     */
    @Autowired
    private DocumentFileService documentFileService;

    /**
     * 系统配置属性，用于读取 PDF 字体路径等配置。
     */
    @Autowired
    private MmwikiProperties mmwikiProperties;

    /**
     * Spring 资源加载器，用于读取 classpath 下的内置字体文件。
     */
    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * 初始化 Markdown 解析与渲染组件。
     *
     * <p>这里启用了 GFM 表格和任务列表扩展，让 PDF 中的展示尽量贴近页面预览效果。</p>
     */
    public PdfExportService() {
        List<Extension> extensions = List.of(
                TablesExtension.create(),
                TaskListItemsExtension.create()
        );
        this.markdownParser = Parser.builder()
                .extensions(extensions)
                .build();
        this.htmlRenderer = HtmlRenderer.builder()
                .extensions(extensions)
                .build();
    }

    /**
     * 导出单篇文档为 PDF 字节数组。
     *
     * @param document 当前导出的文档信息
     * @param markdown 文档 Markdown 原文
     * @return PDF 文件内容
     * @throws IOException 读取图片、字体或生成 PDF 失败时抛出
     */
    public byte[] export(Document document, String markdown) throws IOException {
        String bodyHtml = renderMarkdown(markdown);
        String html = buildHtml(document, inlineLocalImages(bodyHtml));
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            configureCjkFont(builder);
            builder.withHtmlContent(html, null);
            builder.toStream(output);
            builder.run();
            return output.toByteArray();
        }
    }

    /**
     * 将 Markdown 文本渲染为 HTML 片段。
     *
     * @param markdown Markdown 原文，允许为空
     * @return 渲染后的 HTML 片段
     */
    private String renderMarkdown(String markdown) {
        Node document = markdownParser.parse(markdown == null ? "" : markdown);
        return htmlRenderer.render(document);
    }

    /**
     * 组装完整 HTML 文档。
     *
     * <p>OpenHTMLToPDF 需要完整 HTML 和内联 CSS，这里统一控制 PDF 的基础排版样式。</p>
     *
     * @param document 当前导出的文档信息
     * @param bodyHtml 文档正文 HTML
     * @return 可交给 PDF 渲染器处理的完整 HTML
     */
    private String buildHtml(Document document, String bodyHtml) {
        String title = HtmlUtils.htmlEscape(document.getName(), StandardCharsets.UTF_8.name());
        String exportTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        @page {
                            size: A4;
                            margin: 18mm 16mm;
                        }
                        * {
                            box-sizing: border-box;
                        }
                        body {
                            color: #2f3337;
                            font-family: 'MMWikiCJK', 'Microsoft YaHei', 'SimHei', sans-serif;
                            font-size: 13px;
                            line-height: 1.72;
                        }
                        h1, h2, h3, h4, h5, h6 {
                            color: #1f2933;
                            font-weight: 700;
                            line-height: 1.35;
                            margin: 22px 0 10px;
                        }
                        h1 {
                            border-bottom: 1px solid #d9e2ec;
                            font-size: 26px;
                            padding-bottom: 10px;
                        }
                        h2 {
                            border-bottom: 1px solid #edf2f7;
                            font-size: 21px;
                            padding-bottom: 8px;
                        }
                        h3 {
                            font-size: 17px;
                        }
                        .document-title {
                            border-bottom: 2px solid #1f2933;
                            font-size: 28px;
                            margin-top: 0;
                            padding-bottom: 12px;
                        }
                        .document-meta {
                            color: #7b8794;
                            font-size: 12px;
                            margin: -4px 0 22px;
                        }
                        p {
                            margin: 8px 0 12px;
                        }
                        a {
                            color: #1f6feb;
                            text-decoration: none;
                        }
                        blockquote {
                            border-left: 4px solid #d9e2ec;
                            color: #52606d;
                            margin: 14px 0;
                            padding: 4px 0 4px 14px;
                        }
                        code {
                            background: #f5f7fa;
                            border-radius: 3px;
                            color: #c7254e;
                            font-family: 'Courier New', monospace;
                            padding: 2px 4px;
                        }
                        pre {
                            background: #f5f7fa;
                            border: 1px solid #d9e2ec;
                            border-radius: 4px;
                            margin: 14px 0;
                            overflow-wrap: break-word;
                            padding: 12px;
                            white-space: pre-wrap;
                            word-break: break-word;
                        }
                        pre code {
                            background: transparent;
                            color: #2f3337;
                            padding: 0;
                        }
                        table {
                            border-collapse: collapse;
                            margin: 14px 0;
                            width: 100%;
                        }
                        th, td {
                            border: 1px solid #d9e2ec;
                            padding: 7px 9px;
                            vertical-align: top;
                        }
                        th {
                            background: #f5f7fa;
                            font-weight: 700;
                        }
                        img {
                            display: block;
                            height: auto;
                            margin: 12px auto;
                            max-width: 100%;
                        }
                        ul, ol {
                            margin: 8px 0 12px 22px;
                            padding: 0;
                        }
                        li {
                            margin: 3px 0;
                        }
                        hr {
                            border: 0;
                            border-top: 1px solid #d9e2ec;
                            margin: 20px 0;
                        }
                        input[type='checkbox'] {
                            margin-right: 5px;
                        }
                    </style>
                </head>
                <body>
                    <h1 class="document-title">__DOCUMENT_TITLE__</h1>
                    <div class="document-meta">导出时间：__EXPORT_TIME__</div>
                    <article>__DOCUMENT_BODY__</article>
                </body>
                </html>
                """
                .replace("__DOCUMENT_TITLE__", title)
                .replace("__EXPORT_TIME__", exportTime)
                .replace("__DOCUMENT_BODY__", bodyHtml);
    }

    /**
     * 将 HTML 中的本地图片转换为 base64 data URI。
     *
     * <p>这样 PDF 生成时不依赖浏览器、登录态或静态资源映射，上传图片也能稳定进入 PDF。</p>
     *
     * @param html Markdown 渲染后的 HTML
     * @return 图片已经尽量内嵌后的 HTML
     */
    private String inlineLocalImages(String html) {
        Matcher matcher = IMAGE_SRC_PATTERN.matcher(html);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String src = matcher.group(3);
            String inlinedSrc = toDataUri(src);
            matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(1) + matcher.group(2) + inlinedSrc + matcher.group(4)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * 将单个图片地址转换为 data URI。
     *
     * <p>目前只处理本系统上传图片生成的 /images/... 路径；外链图片保持原样，避免导出时访问外网。</p>
     *
     * @param src img 标签中的原始 src
     * @return 转换后的 data URI，转换失败时返回原始 src
     */
    private String toDataUri(String src) {
        if (src == null || src.isBlank() || src.startsWith("data:") || src.startsWith("http://") || src.startsWith("https://")) {
            return src;
        }
        String cleanSrc = src.split("[?#]", 2)[0];
        String relativePath = cleanSrc.startsWith("/") ? cleanSrc.substring(1) : cleanSrc;
        if (!relativePath.startsWith("images/")) {
            return src;
        }
        try {
            Path imagePath = documentFileService.resolveAttachmentPath(URLDecoder.decode(relativePath, StandardCharsets.UTF_8));
            if (!Files.exists(imagePath) || Files.isDirectory(imagePath)) {
                return src;
            }
            String contentType = Files.probeContentType(imagePath);
            if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
                contentType = "image/png";
            }
            return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(imagePath));
        } catch (Exception ex) {
            return src;
        }
    }

    /**
     * 为 PDF 渲染器配置中文字体。
     *
     * <p>优先级：配置指定字体 -> 项目内置字体。不再自动使用操作系统字体，避免部署环境差异和授权风险。</p>
     *
     * @param builder PDF 渲染器构建器
     * @throws IOException 读取 classpath 字体失败时抛出
     */
    private void configureCjkFont(PdfRendererBuilder builder) throws IOException {
        String configuredFont = mmwikiProperties.getPdf().getFontPath();
        if (StringUtils.hasText(configuredFont) && useFont(builder, configuredFont)) {
            log.info("PDF 导出使用配置字体1：{}", configuredFont);
            return;
        }
        for (String bundledFont : BUNDLED_CJK_FONTS) {
            if (useFont(builder, bundledFont)) {
                log.info("PDF 导出使用内置字体2：{}", bundledFont);
                return;
            }
        }
        log.warn("PDF 导出未找到可用中文字体，中文内容可能显示为方块或空白。可配置 mmwiki.pdf.font-path 或内置 .ttf 字体到 classpath:/fonts/pdf/");
    }

    /**
     * 尝试向 PDF 渲染器注册一个字体。
     *
     * @param builder  PDF 渲染器构建器
     * @param location 字体位置，支持 classpath:/... 或普通文件路径
     * @return true 表示字体存在且注册成功，false 表示字体不存在
     * @throws IOException 读取或提取字体文件失败时抛出
     */
    private boolean useFont(PdfRendererBuilder builder, String location) throws IOException {
        Path font;
        if (location.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX)) {
            font = extractClasspathFont(location);
            if (font == null) {
                return false;
            }
        } else {
            font = Path.of(location);
            if (!Files.exists(font) || !Files.isRegularFile(font)) {
                return false;
            }
        }

        if (!isSupportedPdfFont(font)) {
            log.warn("PDF 导出跳过不支持的字体：{}。当前 PDF 引擎仅使用 TrueType 字体（.ttf）。", font.toAbsolutePath().normalize());
            return false;
        }
        builder.useFont(font.toFile(), "MMWikiCJK");
        return true;
    }

    /**
     * 校验字体是否能被当前 PDF 渲染链路稳定加载。
     *
     * <p>部分 CFF/OpenType 字体虽然扩展名是 .otf，但 PDFBox 会按 TrueType 读取并报 loca is mandatory，
     * 这里提前过滤，避免渲染阶段出现 NullPointerException。</p>
     *
     * @param font 字体文件路径
     * @return true 表示字体可以注册给 OpenHTMLToPDF 使用
     */
    private boolean isSupportedPdfFont(Path font) {
        String fileName = font.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!fileName.endsWith(".ttf")) {
            return false;
        }
        try {
            try (TrueTypeFont ignored = new TTFParser(true, true).parse(font.toFile())) {
                return true;
            }
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * 将 jar/classpath 中的字体提取为固定临时文件。
     *
     * <p>避免每次导出都通过 InputStream 加载大字体，也避免某些字体实现反复生成临时字体文件。</p>
     *
     * @param location classpath 字体位置
     * @return 提取后的字体文件路径，字体不存在时返回 null
     * @throws IOException 复制字体失败时抛出
     */
    private Path extractClasspathFont(String location) throws IOException {
        Path cached = CLASSPATH_FONT_FILE_CACHE.get(location);
        if (cached != null && Files.exists(cached)) {
            return cached;
        }
        synchronized (CLASSPATH_FONT_FILE_CACHE) {
            cached = CLASSPATH_FONT_FILE_CACHE.get(location);
            if (cached != null && Files.exists(cached)) {
                return cached;
            }

            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                return null;
            }

            String fileName = resource.getFilename();
            if (!StringUtils.hasText(fileName)) {
                fileName = location.substring(location.lastIndexOf('/') + 1);
            }
            Path fontDir = Path.of(System.getProperty("java.io.tmpdir"), "mmwiki-fonts");
            Files.createDirectories(fontDir);
            Path target = fontDir.resolve(fileName).toAbsolutePath().normalize();
            try (var inputStream = resource.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            CLASSPATH_FONT_FILE_CACHE.put(location, target);
            log.info("PDF 导出已提取内置字体：{} -> {}", location, target);
            return target;
        }
    }

}
