package com.walking.techie.csvtomysql.listener;


import com.walking.techie.csvtomysql.model.Person;
import com.walking.techie.csvtomysql.model.PersonJpaRespository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

	@Autowired
	PersonJpaRespository personJpa;

	@Override
	public void afterJob(JobExecution jobExecution) {
		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
			log.info("!!! JOB FINISHED! Time to verify the results");

			List<Person> results = personJpa.findAll();

			for (Person person : results) {
				log.info("Found <" + person + "> in the database.");
			}

		}
	}
}