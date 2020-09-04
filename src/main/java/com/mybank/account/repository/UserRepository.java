package com.mybank.account.repository;

import org.springframework.data.repository.CrudRepository;

import com.mybank.account.entity.UserDetailsEntity;

public interface UserRepository extends CrudRepository<UserDetailsEntity, String> {
}