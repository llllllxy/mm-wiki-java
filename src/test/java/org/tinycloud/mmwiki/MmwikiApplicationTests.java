package org.tinycloud.mmwiki;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.profiles.active=test",
		"logging.file.name=target/test-logs/mm-wiki.log",
		"mmwiki.document-root-dir=target/test-data"
})
class MmwikiApplicationTests {

	@Test
	void contextLoads() {
	}

}
