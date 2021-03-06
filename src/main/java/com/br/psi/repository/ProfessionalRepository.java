package com.br.psi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.psi.model.Person;
import com.br.psi.model.Professional;

public interface ProfessionalRepository extends JpaRepository<Professional, String> {

	Professional findAllById(Long id);
	Professional findByPerson(Person person);

}