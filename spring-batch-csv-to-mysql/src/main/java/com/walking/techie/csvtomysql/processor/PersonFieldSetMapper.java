package com.walking.techie.csvtomysql.processor;

import com.walking.techie.csvtomysql.model.Person;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

@Slf4j
public class PersonFieldSetMapper implements FieldSetMapper<Person>{

	@Override
	public Person mapFieldSet(FieldSet fieldSet) throws BindException {
		Person person = new Person();
		person.setFirstName(fieldSet.readString(0));
		person.setLastName(fieldSet.readString(1));
		return person;
	}
}