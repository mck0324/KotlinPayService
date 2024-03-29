package com.example.payment.repository

import com.example.payment.model.ProductInOrder
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductInOrderRepository: CoroutineCrudRepository<ProductInOrder, Long> {
    suspend fun countByOrderId(orderId: Long): Long
    suspend fun findByOrderId(orderId: Long): List<ProductInOrder>
}