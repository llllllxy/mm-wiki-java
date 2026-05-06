package org.tinycloud.mmwiki;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@MapperScan("org.tinycloud.mmwiki.mapper")
@ConfigurationPropertiesScan
public class MmwikiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MmwikiApplication.class, args);
	}

}
