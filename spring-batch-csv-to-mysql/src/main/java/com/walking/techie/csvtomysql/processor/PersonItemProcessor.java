package com.walking.techie.csvtomysql.processor;

import com.walking.techie.csvtomysql.model.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class PersonItemProcessor implements ItemProcessor<Person, Person> {

	@Override
	public Person process(Person person) throws Exception {
		
		return person;
	}
}