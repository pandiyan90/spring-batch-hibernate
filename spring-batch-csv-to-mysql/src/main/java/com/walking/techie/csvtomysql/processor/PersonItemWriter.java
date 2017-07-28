/*package com.walking.techie.csvtomysql.processor;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.database.HibernateItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.walking.techie.csvtomysql.model.Person;
import com.walking.techie.csvtomysql.model.PersonJpaRespository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PersonItemWriter extends ItemWriter<Person> {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public void write(List<? extends Person> persons) {
		Session session = sessionFactory.getCurrentSession();
		for(Person person : persons){
			session.save(person);
		}
		super.write(persons);
	}

}*/