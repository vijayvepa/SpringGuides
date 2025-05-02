package com.example.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class MultiStepService {
  private final TransactionTemplate transactionTemplate;
  private final JdbcTemplate jdbcTemplate;

  private static final Logger logger = LoggerFactory.getLogger(MultiStepService.class);

  public MultiStepService(PlatformTransactionManager transactionManager, JdbcTemplate jdbcTemplate) {
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.jdbcTemplate = jdbcTemplate;
    this.transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    this.transactionTemplate.setTimeout(30);
  }

  public Object someServiceMethod() {
    return transactionTemplate.execute(new TransactionCallback<Object>() {
      @Override
      public Object doInTransaction(TransactionStatus status) {
        try {
          updateOperation1();
          return resultOfUpdateOperation2();
        }catch (RuntimeException ex){
          status.setRollbackOnly();
          return null;
        }
      }

    });
  }


  private void updateOperation1() {
    //save some stuff
  }

  private Object resultOfUpdateOperation2() {
    return new Object();
  }

  private void updateOperation3() {

  }

  public void book(String... persons) {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {

      public void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          for(String person : persons ) {
            logger.info("Booking person {} a seat...", person);
            jdbcTemplate.update("INSERT INTO BOOKINGS(FIRST_NAME) values (?)", person);
          }
        }catch (RuntimeException ex) {
          logger.warn("Got exception , rolling back", ex);
          status.setRollbackOnly();
        }
      }
    });
  }
}
