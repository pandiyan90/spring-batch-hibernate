package com.walking.techie;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.walking.techie.csvtomysql.listener.JobCompletionNotificationListener;
import com.walking.techie.csvtomysql.model.Person;
import com.walking.techie.csvtomysql.model.PersonDTO;
import com.walking.techie.csvtomysql.processor.PersonItemProcessor;

import lombok.extern.slf4j.Slf4j;

@EnableAutoConfiguration
@Configuration
@ComponentScan
@EnableBatchProcessing
@Slf4j
@EnableScheduling
public class BatchConfiguration {

    @Value("${spring.datasource.driver-class-name}")
    private String databaseDriver;
    
    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

	@Bean
	public FlatFileItemReader<PersonDTO> reader() {
		FlatFileItemReader<PersonDTO> reader = new FlatFileItemReader<>();
		reader.setResource(new ClassPathResource("person.csv"));

		DefaultLineMapper<PersonDTO> lineMapper = new DefaultLineMapper<>();
		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		String[] names={"firstName","lastName"};
		lineTokenizer.setNames(names);
		lineMapper.setLineTokenizer(lineTokenizer);

		BeanWrapperFieldSetMapper<PersonDTO> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
		fieldSetMapper.setTargetType(PersonDTO.class);
		lineMapper.setFieldSetMapper(fieldSetMapper);

		reader.setLineMapper(lineMapper);

		return reader;
	}

	@Bean
	public ItemProcessor<PersonDTO, Person> processor() {
		return new PersonItemProcessor();
	}

	@Bean
	public ItemWriter<Person> writer() {
		JpaItemWriter<Person> jpaWriter = new JpaItemWriter<>();
		jpaWriter.setEntityManagerFactory(entityManagerFactory().getObject());
		return jpaWriter;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(){
		LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
		bean.setPackagesToScan("com.walking.techie");
		bean.setDataSource(dataSource());
		bean.setJpaVendorAdapter(jpaVendorAdapter());
		bean.setJpaProperties(new Properties());
		
		return bean;
	}

	@Bean
	public JpaVendorAdapter jpaVendorAdapter() {
		HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
		adapter.setDatabase(Database.MYSQL);
		adapter.setGenerateDdl(true);
		adapter.setShowSql(false);
		adapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
		return adapter;
	}

	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(databaseDriver);
		dataSource.setUrl(databaseUrl);
		dataSource.setUsername(databaseUsername);
		dataSource.setPassword(databasePassword);
		return dataSource;
	}

	@Bean
	public Job importPerson(JobBuilderFactory jobs, Step step) {
		return jobs.get("import")
				.incrementer(new RunIdIncrementer())
				.flow(step)
				.end()
				.build();
	}

	@Bean
	public Step step1(StepBuilderFactory stepBuilderFactory, ItemReader<PersonDTO> reader, ItemWriter<Person> writer, ItemProcessor<PersonDTO, Person> processor) {
		return stepBuilderFactory.get("step1")
				.<PersonDTO, Person>chunk(1000)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();
	}

}