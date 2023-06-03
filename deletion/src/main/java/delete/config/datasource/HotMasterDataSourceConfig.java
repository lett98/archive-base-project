package delete.config.datasource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "deleteEntityManagerFactory",
        transactionManagerRef = "deleteTransactionManager",
        basePackages = {"common.repository.entity"}
)
@EnableTransactionManagement
public class HotMasterDataSourceConfig {
    private String MODEL_PACKAGE = "common.model.entity";

    @Bean("masterDataSource")
    @ConfigurationProperties(prefix = "spring.hotmaster.datasource")
    public DataSource hotMasterDataSource() {
        DataSource dataSource = DataSourceBuilder.create().build();
        return dataSource;
    }

    @Bean(name = "deleteEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean masterEntityManagerFactory(EntityManagerFactoryBuilder entityManagerFactoryBuilder) {
        return entityManagerFactoryBuilder
                .dataSource(hotMasterDataSource())
                .packages(MODEL_PACKAGE)
                .build();
    }

    @Bean(name = "deleteTransactionManager")
    public PlatformTransactionManager masterTransactionManager(@Qualifier("deleteEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
