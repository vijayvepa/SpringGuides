package com.example.accessing_data_mongodb

import org.springframework.boot.CommandLineRunner
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import org.springframework.stereotype.Component

@Suppress("unused")
@Component
class MongoRunner(private val repository: CustomerRepository): CommandLineRunner {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)


    override fun run(vararg args: String?) {
        logger.info("Deleting  all customers")
        repository.deleteAll()

        repository.save<Customer>(Customer("Alice", "Smith"))
        repository.save<Customer>(Customer("Bob", "Smith"))

        logger.info("Customers found with findAll")

        repository.findAll().printAllCustomers(logger)

        logger.info("Looking up by first name Alice")
        val alice = repository.findByFirstName("Alice")
        logger.info("Alice -> {}", alice)

        logger.info("Looking up by last name Smith")
        repository.findByLastName("Smith").printAllCustomers(logger)
    }

    fun List<Customer>.printAllCustomers(logger: Logger) {
        this.forEach { logger.info("Customer -> {}", it) }
    }
}