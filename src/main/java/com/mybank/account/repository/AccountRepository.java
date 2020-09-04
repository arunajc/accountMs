package com.mybank.account.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mybank.account.entity.AccountDetailsEntity;

@Repository
public interface AccountRepository extends CrudRepository<AccountDetailsEntity, Long> {
	
	@Transactional
	@Modifying(clearAutomatically = true)
	@Query("update AccountDetailsEntity acc set acc.locked =:locked where acc.accountId =:accountId and acc.locked=:currentLockStatus")
	int lockorUnlockAccount(
			@Param("accountId") long accountId, 
			@Param("locked") int locked, 
			@Param("currentLockStatus") int currentLockStatus);

}
