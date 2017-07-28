package com.walking.techie;

import java.util.Properties;

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
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
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
    private String dbDriver;
    
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.jpa.database-platform}")
    private String dbPlatform;

    @Value("${spring.jpa.show-sql}")
    private boolean dbShowSql;

    @Value("${spring.jpa.generate-ddl}")
    private boolean dbGenerateDdl;

    String packageName="com.walking.techie";
    
	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Bean
	public FlatFileItemReader<PersonDTO> reader() {
		FlatFileItemReader<PersonDTO> reader = new FlatFileItemReader<>();
		reader.setResource(new ClassPathResource("person.csv"));
		reader.setLinesToSkip(1);

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
		bean.setPackagesToScan(packageName);
		bean.setDataSource(dataSource());
		bean.setJpaVendorAdapter(jpaVendorAdapter());
		bean.setJpaProperties(new Properties());
		
		return bean;
	}

	@Bean
	public JpaVendorAdapter jpaVendorAdapter() {
		HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
		adapter.setDatabase(Database.MYSQL);
		adapter.setGenerateDdl(dbGenerateDdl);
		adapter.setShowSql(dbShowSql);
		adapter.setDatabasePlatform(dbPlatform);
		return adapter;
	}

	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(dbDriver);
		dataSource.setUrl(dbUrl);
		dataSource.setUsername(dbUsername);
		dataSource.setPassword(dbPassword);
		return dataSource;
	}

	@Bean
	public Job importPerson(JobCompletionNotificationListener listener) {
		return jobBuilderFactory.get("import")
				.incrementer(new RunIdIncrementer())
				.listener(listener)
				.flow(step1())
				.end()
				.build();
	}

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1")
				.<PersonDTO, Person>chunk(10)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();
	}

}