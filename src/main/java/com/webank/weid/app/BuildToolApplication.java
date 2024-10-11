

package com.webank.weid.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import com.webank.weid.config.StaticConfig;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ComponentScan("com.webank.weid")
@ServletComponentScan("com.webank.weid")
public class BuildToolApplication extends StaticConfig {
    
    public static String[] args;
    public static ConfigurableApplicationContext context;
    
    public static void main(String[] args) {
        BuildToolApplication.args = args;
        BuildToolApplication.context = SpringApplication.run(BuildToolApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
