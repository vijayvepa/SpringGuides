package com.example.accessing_data_mongodb

import org.springframework.data.mongodb.repository.MongoRepository

interface CustomerRepository : MongoRepository<Customer, String> {
    fun  findByFirstName(firstName: String): Customer;
    fun findByLastName(lastName: String): List<Customer>;
}