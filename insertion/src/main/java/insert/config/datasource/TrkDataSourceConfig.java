package insert.config.datasource;

import org.springframework.beans.factory.annotation.Qualifier;
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
        entityManagerFactoryRef = "trkInsertEntityManagerFactory",
        transactionManagerRef = "trkTransactionManager",
        basePackages = {"common.repository.trk"}
)
@EnableTransactionManagement
public class TrkDataSourceConfig {
    private String MODEL_PACKAGE = "common.model.trk";
    @Bean(name = "trkInsertEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean trkEntityManagerFactory(EntityManagerFactoryBuilder entityManagerFactoryBuilder,
                                                                          @Qualifier("trkDataSource") DataSource dataSource) {
        return entityManagerFactoryBuilder
                .dataSource(dataSource)
                .packages(MODEL_PACKAGE)
                .build();
    }

    @Bean(name = "trkTransactionManager")
    public PlatformTransactionManager trkTransactionManager(@Qualifier("trkInsertEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}