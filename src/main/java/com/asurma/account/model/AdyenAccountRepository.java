package com.asurma.account.model;

import java.util.List;



import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
@Transactional
public interface AdyenAccountRepository extends CrudRepository<AdyenAccount, Integer> {

  List<AdyenAccount> findByFileName(String fileName);

  List<AdyenAccount> findByFileNameAndStatus(String fileName, String status);

}
