package com.walking.techie.csvtomysql.model;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface PersonJpaRespository extends CrudRepository<Person, Integer>{

	public List<Person> findAll();
}