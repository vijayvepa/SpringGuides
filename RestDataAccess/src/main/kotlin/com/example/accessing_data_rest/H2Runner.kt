package com.example.accessing_data_rest

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.query
import org.springframework.stereotype.Component

private const val FIRST_NAME = "first_name"
private const val LAST_NAME = "last_name"
private const val ID = "id"
private const val CUSTOMERS = "customers"

@Component
@Profile("!postgres")
class RestDataRunner(val jdbcTemplate: JdbcTemplate) : CommandLineRunner {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass);



    override fun run(vararg args: String?) {


        logger.info("Creating tables");

        with(jdbcTemplate) {
            execute ("DROP TABLE $CUSTOMERS IF EXISTS")
            execute  ("CREATE TABLE $CUSTOMERS (" +
                            "$ID SERIAL, $FIRST_NAME VARCHAR(255), $LAST_NAME VARCHAR(255)" +
                        ")")

        }

        val splitUpNames: List<Array<String>> = listOf("John Woo", "Jeff Dean", "Josh Bloch", "Josh Long")
            .stream().map { it.split(" ").toTypedArray() }.toList();

        splitUpNames.forEach {
            logger.info("Inserting customer record for {} {}", it[0], it[1]);
        }


        jdbcTemplate.batchUpdate("INSERT INTO $CUSTOMERS ($FIRST_NAME, $LAST_NAME) VALUES (?,?)", splitUpNames);

        logger.info("Querying for customer records where $FIRST_NAME = 'Josh' :")

        jdbcTemplate.query("SELECT $ID, $FIRST_NAME, $LAST_NAME FROM $CUSTOMERS WHERE $FIRST_NAME = ?", "Josh"){ rs, row ->
            Customer(
                rs.getLong(ID),
                rs.getString(FIRST_NAME),
                rs.getString(LAST_NAME)
            )

        }.forEach { logger.info(it.toString()) }


    }
}