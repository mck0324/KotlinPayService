package com.example.payment.service

import com.example.payment.config.CacheKey
import com.example.payment.config.CacheManager
import com.example.payment.model.Product
import com.example.payment.repository.ProductRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import kotlin.time.Duration.Companion.minutes

@Service
class ProductService(
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val cacheManager: CacheManager,
    @Value("\${spring.active.profile:local}")
    private val profile: String,

) {
    
    val CACHE_KEY = "${profile}/payment/product/".also { cacheManager.ttl[it] = 10.minutes }
    suspend fun get(prodId: Long): Product? {

        val key = CacheKey(CACHE_KEY,prodId)
        return cacheManager.get(key) {
            productRepository.findById(prodId)
        }
    }
}