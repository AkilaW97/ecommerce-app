package com.ewis.ecommerce.service;

import com.ewis.ecommerce.model.Product;
import com.ewis.ecommerce.payload.ProductDto;
import com.ewis.ecommerce.payload.ProductResponse;

public interface ProductService {
    ProductDto addProduct(Long categoryId, Product product);

    ProductResponse getAllProducts();
}
