package org.tinycloud.mmwiki;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.tinycloud.mmwiki.util.LocalHostUtils;

import java.net.UnknownHostException;

/**
 * MM-Wiki Spring Boot 启动类。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */

@SpringBootApplication
@MapperScan("org.tinycloud.mmwiki.mapper")
public class MmwikiApplication {
	private static final Logger log = LoggerFactory.getLogger(MmwikiApplication.class);

	public static void main(String[] args) throws UnknownHostException {
		ConfigurableApplicationContext application = SpringApplication.run(MmwikiApplication.class, args);
		Environment env = application.getEnvironment();
		String ip = LocalHostUtils.getLocalHost();
		String port = env.getProperty("server.port");

		log.info("\n----------------------------------------------------------\n\t" +
				"tiny-job 启动成功！\n\t" +
				"┌─┐┬ ┬┌─┐┌─┐┌─┐┌─┐┌─┐  ┌─┐┌┬┐┌─┐┬─┐┌┬┐┌─┐┌┬┐   ┬\n\t" +
				"└─┐│ ││  │  ├┤ └─┐└─┐  └─┐ │ ├─┤├┬┘ │ ├┤  ││   │\n\t" +
				"└─┘└─┘└─┘└─┘└─┘└─┘└─┘  └─┘ ┴ ┴ ┴┴└─ ┴ └─┘─┴┘   o\n\t" +
				"-------------------------------------------------------------------------\n\t" +
				"Access URLs:\n\t" +
				"Local: \t\thttp://localhost:" + port + "/\n\t" +
				"External: \thttp://" + ip + ":" + port + "/\n\t" +
				"Swagger-UI: http://" + ip + ":" + port + "/doc.html\n\t" +
				"-------------------------------------------------------------------------");
	}
}
