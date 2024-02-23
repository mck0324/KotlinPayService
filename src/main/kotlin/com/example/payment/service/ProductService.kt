package com.example.payment.service

import com.example.payment.model.Product
import com.example.payment.repository.ProductRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ProductService(
    @Autowired private val productRepository: ProductRepository
) {
    suspend fun get(prodId: Long): Product? {
        return productRepository.findById(prodId)
    }
}