package com.example.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class BookingService {
  private final static Logger logger = LoggerFactory.getLogger(BookingService.class);
  private final JdbcTemplate jdbcTemplate;

  public BookingService(final JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Transactional
  public void book(String... persons) {
    for(String person : persons ) {
      logger.info("Booking person {} a seat...", person);
      jdbcTemplate.update("INSERT INTO BOOKINGS(FIRST_NAME) values (?)", person);
    }
  }

  public List<String> findAllBookings() {
    return jdbcTemplate.query("select FIRST_NAME from BOOKINGS",
        (rs,  rowNum) -> rs.getString("FIRST_NAME"));
  }
}
