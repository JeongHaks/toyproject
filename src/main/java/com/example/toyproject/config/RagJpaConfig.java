package com.example.toyproject.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.example.toyproject.rag.chunk.repo",
        entityManagerFactoryRef = "ragEntityManagerFactory",
        transactionManagerRef = "ragTransactionManager"
)
public class RagJpaConfig {

    @Bean(name = "ragDataSource")
    @ConfigurationProperties(prefix = "rag.datasource")
    public DataSource ragDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "ragJdbcTemplate")   // ✅ 추가
    public JdbcTemplate ragJdbcTemplate(@Qualifier("ragDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean(name = "ragEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean ragEntityManagerFactory(
            @Qualifier("ragDataSource") DataSource dataSource
    ) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);

        // ✅ RAG 엔티티 패키지(스샷상 rag.chunk.domain 밑일 확률 높음)
        emf.setPackagesToScan("com.example.toyproject.rag.chunk.domain");

        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setPersistenceUnitName("rag");
        return emf;
    }

    @Bean(name = "ragTransactionManager")
    public PlatformTransactionManager ragTransactionManager(
            @Qualifier("ragEntityManagerFactory") EntityManagerFactory emf
    ) {
        return new JpaTransactionManager(emf);
    }
}
