package com.security.jwt.dao;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.security.jwt.entity.Cliente;

@Repository
public interface IClienteDao extends PagingAndSortingRepository<Cliente, Long>{

}
