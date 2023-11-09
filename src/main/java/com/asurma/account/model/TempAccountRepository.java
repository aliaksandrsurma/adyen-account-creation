package com.asurma.account.model;

import java.util.List;



import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
@Transactional
public interface TempAccountRepository extends CrudRepository<TempAccount, Integer> {

  List<TempAccount> findByLogin(String login);

}
