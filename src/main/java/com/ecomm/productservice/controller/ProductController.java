package com.ecomm.productservice.controller;

import com.ecomm.productservice.model.Product;
import com.ecomm.productservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    // List full catalog
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // View single product
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Add a new product to catalog
    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        Product saved = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Called internally by order-service before confirming an order.
    // Returns 200 + remaining stock if reservation succeeds, 409 if insufficient stock,
    // 404 if product doesn't exist (simulates real inter-service dependency).
    @PutMapping("/{id}/reserve")
    public ResponseEntity<?> reserveStock(@PathVariable Long id, @RequestParam Integer quantity) {
        Optional<Product> optProduct = productRepository.findById(id);
        if (optProduct.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found: " + id);
        }
        Product product = optProduct.get();
        if (product.getStockQuantity() < quantity) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Insufficient stock for product " + id + ". Available: " + product.getStockQuantity());
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
        return ResponseEntity.ok(product);
    }
}
