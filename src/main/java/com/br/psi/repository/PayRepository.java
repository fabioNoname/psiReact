package com.br.psi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.psi.model.Pay;

public interface PayRepository extends JpaRepository<Pay, String> {
}