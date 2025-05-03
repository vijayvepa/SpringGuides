package com.example.accessing_data_rest

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.query
import org.springframework.stereotype.Component
import java.util.Arrays

@Component
class RestDataRunner(val jdbcTemplate: JdbcTemplate) : CommandLineRunner {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass);



    override fun run(vararg args: String?) {

        logger.info("Creating tables");

        with(jdbcTemplate) {
            execute ("DROP TABLE customers IF EXISTS")
            execute  ("CREATE TABLE customers (" +
                            "id SERIAL, first_name VARCHAR(255), last_name VARCHAR(255)" +
                        ")")

        }

        val splitUpNames: List<Array<String>> = listOf("John Woo", "Jeff Dean", "Josh Bloch", "Josh Long")
            .stream().map { it.split(" ").toTypedArray() }.toList();

        splitUpNames.forEach {
            logger.info("Inserting customer record for {} {}", it[0], it[1]);
        }


        jdbcTemplate.batchUpdate("INSERT INTO customers (first_name, last_name) VALUES (?,?)", splitUpNames);

        logger.info("Querying for customer records where first_name = 'Josh' :")

        jdbcTemplate.query("SELECT id, first_name, last_name FROM customers WHERE first_name = ?", "Josh"){rs, row ->
            Customer(
                rs.getLong("id"),
                rs.getString("first_name"),
                rs.getString("last_name")
            )

        }.forEach { logger.info(it.toString()) }


    }
}