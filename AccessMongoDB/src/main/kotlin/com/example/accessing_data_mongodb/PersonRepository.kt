package com.example.accessing_data_mongodb

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(collectionResourceRel = "people", path = "people")
interface PersonRepository : MongoRepository<Person, String> {
    fun findByLastName(@Param("name") name: String): List<Person>;
    fun findByFirstName(@Param("name") name: String): List<Person>;
}