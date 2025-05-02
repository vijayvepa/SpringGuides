package com.example.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;


@Component
public class AppRunner implements CommandLineRunner {

  private final BookingService bookingService;
  private final MultiStepService multiStepService;
  private final static Logger logger = LoggerFactory.getLogger(AppRunner.class);

  public AppRunner(BookingService bookingService, MultiStepService multiStepService) {
    this.bookingService = bookingService;
    this.multiStepService = multiStepService;
  }

  @Override
  public void run(String... args) throws Exception {
    bookingService.book("Alice", "Bob", "Carol");
    Assert.isTrue(bookingService.findAllBookings().size() == 3,
        "First booking should work with no problem");

    try{
      bookingService.book("Chris", "Samuel");
    }catch (RuntimeException ex) {
      logger.info("v --- The following exception is expected because Samuel is too big for the DB.");
      logger.error(ex.getMessage());
    }

    for(String person: bookingService.findAllBookings()){
      logger.info("So far, {} is booked.", person);
    }
    logger.info("You shouldn't see Chris or Samuel , Samuel violated DB constraints and Chris was rolled back in the same TX");


    Assert.isTrue(bookingService.findAllBookings().size() == 3,
        "Samuel should've triggered a rollback");


    try{
      multiStepService.book("Buddy", null);
    }catch (RuntimeException ex) {
      logger.info("v --- The following exception is expected because null is not valid for the DB.");
      logger.error(ex.getMessage());
    }

    for(String person: bookingService.findAllBookings()){
      logger.info("So far, {} is booked.", person);
    }
    logger.info("You shouldn't see Buddy or null , null should've triggered a rolled back");


    Assert.isTrue(bookingService.findAllBookings().size() == 3,
        "null should've triggered a rollback");


  }
}
