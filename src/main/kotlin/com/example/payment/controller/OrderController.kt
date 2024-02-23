package com.example.payment.controller

import com.example.payment.common.Beans
import com.example.payment.common.Beans.Companion.beanProductInOrderRepository
import com.example.payment.common.Beans.Companion.beanProductService
import com.example.payment.model.Order
import com.example.payment.model.PgStatus
import com.example.payment.repository.ProductInOrderRepository
import com.example.payment.service.OrderService
import com.example.payment.service.ProductService
import com.example.payment.service.ReqCreateOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.annotation.Id
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/order")
class OrderController(
    @Autowired private val orderService: OrderService
) {

    @GetMapping("/{orderId}")
    suspend fun get(@PathVariable orderId: Long): ResOrder {
        return orderService.get(orderId).toResOrder()
    }

    @GetMapping("/all/{userId}")
    suspend fun getAll(@PathVariable userId: Long): List<ResOrder> {
        return orderService.getAll(userId).map { it.toResOrder() }
    }

    @PostMapping("/create")
    suspend fun create(@RequestBody request: ReqCreateOrder): ResOrder {
        return orderService.create(request).toResOrder()
    }

    @DeleteMapping("/{orderId}")
    suspend fun delete(@PathVariable orderId: Long) {
        return orderService.delete(orderId)
    }

}
suspend fun Order.toResOrder(): ResOrder {
    return this.let {
        ResOrder(
            id = it.id,
            userId = it.userId,
            description = it.description,
            amount = it.amount,
            pgOrderId = it.pgOrderId,
            pgKey = it.pgKey,
            pgStatus = it.pgStatus,
            pgRetryCount = it.pgRetryCount,
            createdAt = it.createdAt,
            updatedAt = it.updatedAt,
            products = beanProductInOrderRepository.findByOrderId(it.id).map { prodInOrd ->
                ResProductQuantity(
                    id = prodInOrd.prodId,
                    name = beanProductService.get(prodInOrd.prodId)?.name ?: "unknown",
                    price = prodInOrd.price,
                    quantity = prodInOrd.quantity
                )
            },
        )
    }
}


data class ResOrder(
    val id: Long = 0,
    val userId : Long,
    val description: String? = null,
    val amount: Long = 0,
    val pgOrderId: String? = null,
    val pgKey: String? = null,
    val pgStatus: PgStatus = PgStatus.CREATE,
    val pgRetryCount: Int = 0,
    val createdAt: LocalDateTime?= null,
    val updatedAt: LocalDateTime?= null,
    val products: List<ResProductQuantity>
)

data class ResProductQuantity (
    val id: Long,
    val name: String,
    val price: Long,
    val quantity: Int
)
