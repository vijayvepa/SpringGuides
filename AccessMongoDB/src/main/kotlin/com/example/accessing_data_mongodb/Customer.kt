package com.example.accessing_data_mongodb

import org.springframework.data.annotation.Id

data class Customer( val firstName: String, val lastName: String, @Id var id: String? = null);