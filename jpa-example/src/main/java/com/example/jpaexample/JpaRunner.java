package com.example.jpaexample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JpaRunner implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(JpaRunner.class);
  private final CustomerRepository customerRepository;

  public JpaRunner(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  @Override
  public void run(String... args) throws Exception {
    customerRepository.save(new Customer("Jack", "Bauer"));
    customerRepository.save(new Customer("Chloe", "O'Brian"));
    customerRepository.save(new Customer("Kim", "Bauer"));
    customerRepository.save(new Customer("David", "Palmer"));
    customerRepository.save(new Customer("Michelle", "Brussels"));

    logger.info("Customers found with findAll");
    logger.info("---");
    customerRepository.findAll().forEach(customer -> {
      logger.info(customer.toString());
    });

    logger.info("");

    Customer customer = customerRepository.findById(2L);
    logger.info("Customer found with findById(2L)");
    logger.info("---");
    logger.info(customer.toString());

    logger.info("Customer found by lastname Bauer");
    customerRepository.findByLastName("Bauer").forEach(bauer ->
        logger.info(bauer.toString())
    );

    logger.info("");
  }
}
