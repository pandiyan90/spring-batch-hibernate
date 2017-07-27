package com.walking.techie.csvtomysql.processor;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.database.HibernateItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.walking.techie.csvtomysql.model.Person;
import com.walking.techie.csvtomysql.model.PersonJpaRespository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PersonItemWriter extends HibernateItemWriter<Person> {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private PersonJpaRespository personJpa;

	@Override
	protected void doWrite(SessionFactory sessionFactory, List<? extends Person> persons) {
		this.sessionFactory=sessionFactory;
		Session session = sessionFactory.getCurrentSession();
		for(Person person : persons){
			session.save(person);
			personJpa.save(person);
		}
		super.doWrite(sessionFactory, persons);
	}

	@Override
	public void write(List<? extends Person> persons) {
		doWrite(sessionFactory,persons);
		super.write(persons);
	}

}