package com.oj.platform.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:}")
    private String rawUrl;

    @Value("${spring.datasource.username:}")
    private String rawUsername;

    @Value("${spring.datasource.password:}")
    private String rawPassword;

    @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        String mysqlUrl = System.getenv("MYSQL_URL");
        if (mysqlUrl == null || mysqlUrl.isBlank()) {
            mysqlUrl = System.getenv("DATABASE_URL");
        }
        if (mysqlUrl == null || mysqlUrl.isBlank()) {
            mysqlUrl = System.getenv("SPRING_DATASOURCE_URL");
        }

        String jdbcUrl = rawUrl;
        String username = rawUsername;
        String password = rawPassword;

        if (mysqlUrl != null && !mysqlUrl.isBlank()) {
            try {
                if (mysqlUrl.startsWith("mysql://")) {
                    URI uri = new URI(mysqlUrl);
                    String host = uri.getHost();
                    int port = uri.getPort() == -1 ? 3306 : uri.getPort();
                    String path = uri.getPath();
                    String dbName = (path != null && path.length() > 1) ? path.substring(1) : "railway";
                    String userInfo = uri.getUserInfo();
                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        username = parts[0];
                        password = parts[1];
                    }
                    jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
                    log.info("Auto-configured DataSource from MYSQL_URL: host={}, port={}, db={}", host, port, dbName);
                } else if (mysqlUrl.startsWith("jdbc:mysql://")) {
                    jdbcUrl = mysqlUrl;
                }
            } catch (Exception e) {
                log.warn("Could not parse database URI ({}), falling back: {}", mysqlUrl, e.getMessage());
            }
        }

        // Direct Railway environment variables check
        String envHost = System.getenv("MYSQLHOST");
        String envPort = System.getenv("MYSQLPORT");
        String envUser = System.getenv("MYSQLUSER");
        String envPass = System.getenv("MYSQLPASSWORD");
        String envDb = System.getenv("MYSQLDATABASE");

        if (envHost != null && !envHost.isBlank()) {
            int port = (envPort != null && !envPort.isBlank()) ? Integer.parseInt(envPort) : 3306;
            String db = (envDb != null && !envDb.isBlank()) ? envDb : "railway";
            jdbcUrl = "jdbc:mysql://" + envHost + ":" + port + "/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
            if (envUser != null && !envUser.isBlank()) username = envUser;
            if (envPass != null && !envPass.isBlank()) password = envPass;
            log.info("Auto-configured DataSource from Railway MYSQLHOST variables: host={}, port={}, db={}", envHost, port, db);
        }

        log.info("Initializing HikariDataSource with JDBC URL: {}", jdbcUrl);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        if (username != null && !username.isBlank()) {
            config.setUsername(username);
        }
        if (password != null && !password.isBlank()) {
            config.setPassword(password);
        }
        config.setDriverClassName(driverClassName);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }
}
