package com.example.payment.service

import com.example.payment.exception.NoOrderFound
import com.example.payment.exception.NoProductFound
import com.example.payment.model.Order
import com.example.payment.model.PgStatus
import com.example.payment.model.ProductInOrder
import com.example.payment.repository.OrderRepository
import com.example.payment.repository.ProductInOrderRepository
import com.example.payment.repository.ProductRepository
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID


private val logger = KotlinLogging.logger {  }
@Service
class OrderService(
    @Autowired private val orderRepository: OrderRepository,
    @Autowired private val productService: ProductService,
//    @Autowired private val productRepository: ProductRepository,
    @Autowired private val productInOrderRepository: ProductInOrderRepository
) {
    @Transactional
    suspend fun create(request: ReqCreateOrder): Order {
        val prodIds = request.products.map { it.prodId }.toSet()
//        캐시사용전
//        val productById = productRepository.findAllById(prodIds).toList().associateBy { it.id }
//        캐시 사용후
        val productById = request.products.mapNotNull { productService.get(it.prodId) }.associateBy { it.id  }
        prodIds.filter { !productById.containsKey(it) }.let { remains ->
            if (remains.isNotEmpty())
                throw NoProductFound("prod ids: $remains")
        }
        val amount = request.products.sumOf { productById[it.prodId]!!.price * it.quantity }

        val description = request.products.joinToString(", ") { "${productById[it.prodId]!!.name} X ${it.quantity}" }

        val newOrder = orderRepository.save(Order(
            userId = request.userId,
            description = description,
            amount =  amount,
            pgOrderId = "${UUID.randomUUID()}".replace("-",""),
            pgStatus = PgStatus.CREATE,

        ))

        request.products.forEach {
            productInOrderRepository.save(ProductInOrder(
                orderId = newOrder.id,
                prodId = it.prodId,
                price = productById[it.prodId]!!.price,
                quantity = it.quantity
            ))
        }
        return newOrder
    }

    suspend fun get(orderId: Long): Order {
        return orderRepository.findById(orderId) ?: throw NoOrderFound("OrderId $orderId")
    }

    suspend fun getAll(userId: Long): List<Order> {
        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
    }
}

data class ReqCreateOrder(
    val userId: Long,
    var products: List<ReqProQuantity>
)

data class ReqProQuantity(
    val prodId: Long,
    val quantity: Int,
)
