package delete.config.datasource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "jobRepoEntityManagerFactory",
        transactionManagerRef = "jobRepoTransactionManager",
        basePackages = {"common.repository.tracing"}
)
public class LocalDataSourceConfig {
    private String MODEL_PACKAGE = "common.model.tracing";
    @Primary
    @Bean("reposDataSource")
    @ConfigurationProperties(prefix = "spring.jobrepos.datasource")
    public DataSource jobReposDataSource() {
        DataSource dataSource = DataSourceBuilder.create().build();
        return dataSource;
    }

    @Bean(name = "jobRepoEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean jobRepoEntityManagerFactory(EntityManagerFactoryBuilder entityManagerFactoryBuilder) {
        return entityManagerFactoryBuilder
                .dataSource(jobReposDataSource())
                .packages(MODEL_PACKAGE)
                .build();
    }

    @Bean(name = "jobRepoTransactionManager")
    public PlatformTransactionManager jobRepoTransactionManager(@Qualifier("jobRepoEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
