package com.walking.techie.csvtomysql.processor;

import com.walking.techie.csvtomysql.model.Person;
import com.walking.techie.csvtomysql.model.PersonDTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class PersonItemProcessor implements ItemProcessor<PersonDTO, Person> {

	@Override
	public Person process(PersonDTO personDTO) throws Exception {
		Person person = new Person();
		person.setFirstName(personDTO.getFirstName());
		person.setLastName(personDTO.getLastName());
		log.info("Processor: "+person);
		return person;
	}
}