package cm.config.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class LocalDataSourceConfig {
    @Primary
    @Bean("reposDataSource")
    @ConfigurationProperties(prefix = "spring.jobrepos.datasource")
    public DataSource jobReposDataSource() {
        DataSource dataSource = DataSourceBuilder.create().build();
        return dataSource;
    }

    @Bean(name = "slaveDataSource")
    @ConfigurationProperties(prefix = "spring.hotslave.datasource")
    public DataSource slaveDataSource() {
        DataSource dataSource = DataSourceBuilder.create().build();
        return dataSource;
    }
}
