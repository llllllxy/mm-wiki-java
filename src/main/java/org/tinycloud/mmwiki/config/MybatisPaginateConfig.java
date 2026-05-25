package org.tinycloud.mmwiki.config;

import jakarta.annotation.PostConstruct;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.tinycloud.paginate.MyBatisPaginateInterceptor;

import java.util.List;
import java.util.Properties;

/**
 * MyBatis分页插件配置
 */
@Configuration
public class MybatisPaginateConfig {

    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;

    /**
     * 注册tiny-mybatis-paginate分页拦截器
     */
    @PostConstruct
    public void addPaginateInterceptor() {
        MyBatisPaginateInterceptor pageInterceptor = new MyBatisPaginateInterceptor();
        Properties pageProperties = new Properties();
        pageProperties.setProperty("dialect", "mysql");
        pageProperties.setProperty("openRuntimeDbType", "false");
        pageInterceptor.setProperties(pageProperties);

        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            sqlSessionFactory.getConfiguration().addInterceptor(pageInterceptor);
        }
    }
}
