package com.ewis.ecommerce.service.impl;

import com.ewis.ecommerce.exceptions.APIException;
import com.ewis.ecommerce.exceptions.ResourceNotFoundException;
import com.ewis.ecommerce.model.Category;
import com.ewis.ecommerce.model.Product;
import com.ewis.ecommerce.payload.ProductDto;
import com.ewis.ecommerce.payload.ProductResponse;
import com.ewis.ecommerce.repository.CategoryRepository;
import com.ewis.ecommerce.repository.ProductRepository;
import com.ewis.ecommerce.service.FileService;
import com.ewis.ecommerce.service.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
 public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.images}")
    private String path;



    @Override
    public ProductDto addProduct(Long categoryId, ProductDto productDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        //validation: to see if the product is existed or not
        boolean isProductPresent = true;

        List<Product> products = category.getProducts();
        for (Product value : products) {
            //every product is comparing with the productDTO
            if (value.getProductName().equals(productDto.getProductName())) {
                isProductPresent = false;
                break;
            }
        }
        //End of validation

        if (isProductPresent) {
            Product product = modelMapper.map(productDto, Product.class);

            product.setImage("default.png");
            product.setCategory(category);
            double specialPrice = product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice());
            product.setSpecialPrice(specialPrice);
            Product savedProduct = productRepository.save(product);
            return modelMapper.map(savedProduct, ProductDto.class);
        }else{
            throw new APIException("Product already exist!");
        }
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        // 1. Decide sorting direction — ascending or descending
        // Example: sort by "productId" in "asc" or "desc" order
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // 2. Create a Pageable object that tells Spring:
        // - which page to fetch (pageNumber)
        // - how many records per page (pageSize)
        // - and how to sort (sortByAndOrder)
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        // 3. Fetch paginated products from the database
        // Spring Data JPA automatically uses LIMIT and OFFSET in SQL
        Page<Product> pageProducts = productRepository.findAll(pageDetails);

        // 4. Extract only the list of products (not the whole Page object)
        List<Product> products = pageProducts.getContent();

        // 5. Convert each Product entity into a ProductDto
        // DTO = Data Transfer Object (used to send clean data to the frontend)
        List<ProductDto> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .toList();

        // 6. Prepare the final ProductResponse object
        // This includes both product data and pagination info
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);                        // List of product DTOs
        productResponse.setPageNumber(pageProducts.getNumber());        // Current page number
        productResponse.setPageSize(pageProducts.getSize());            // Number of items per page
        productResponse.setTotalElements(pageProducts.getTotalElements()); // Total number of products
        productResponse.setTotalPages(pageProducts.getTotalPages());    // How many pages in total
        productResponse.setLastPage(pageProducts.isLast());             // True if it's the final page

        // 7. Return the response to the controller
        return productResponse;
    }


    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByCategoryOrderByPriceAsc(category, pageDetails);

        List<Product> products = pageProducts.getContent();

        List <ProductDto> productDtos = products.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDtos);
        productResponse.setPageNumber(pageProducts.getNumber());        // Current page number
        productResponse.setPageSize(pageProducts.getSize());            // Number of items per page
        productResponse.setTotalElements(pageProducts.getTotalElements()); // Total number of products
        productResponse.setTotalPages(pageProducts.getTotalPages());    // How many pages in total
        productResponse.setLastPage(pageProducts.isLast());

        return productResponse;
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%', pageDetails);

        List<Product> products = pageProducts.getContent();
        List<ProductDto> productDtos = products.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .toList();

        if(products.isEmpty()){
            throw new APIException("Products not found with keyword: " + keyword);
        }

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDtos);
        productResponse.setPageNumber(pageProducts.getNumber());        // Current page number
        productResponse.setPageSize(pageProducts.getSize());            // Number of items per page
        productResponse.setTotalElements(pageProducts.getTotalElements()); // Total number of products
        productResponse.setTotalPages(pageProducts.getTotalPages());    // How many pages in total
        productResponse.setLastPage(pageProducts.isLast());

        return productResponse;
    }

    @Override
    public ProductDto updateProduct(Long productId, ProductDto productDto) {

        //Get the existing product from DB
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        Product product = modelMapper.map(productDto, Product.class);

        //Update the shared user (product info with the one in request body)
        productFromDb.setProductName(product.getProductName());
        productFromDb.setDescription(product.getDescription());
        productFromDb.setQuantity(product.getQuantity());
        productFromDb.setDiscount(product.getDiscount());
        productFromDb.setPrice(product.getPrice());
        productFromDb.setSpecialPrice(product.getSpecialPrice());

        //Save it to the DB
        Product savedProduct = productRepository.save(productFromDb);
        return modelMapper.map(savedProduct, ProductDto.class);

    }

    @Override
    public ProductDto deleteProduct(Long productId) {

        //Get the existing product from DB
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        //Delete the product
        productRepository.delete(productFromDb);

        //return
        return modelMapper.map(productFromDb, ProductDto.class);
    }

    @Override
    public ProductDto updateProductImage(Long productId, MultipartFile image) throws IOException {
        //Get the existing product from DB
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        //Upload image to server
        //Get the file name of uploaded image
        String fileName = fileService.uploadImage(path, image);

        //Updating the new file name to the product
        productFromDb.setImage(fileName);

        //Save the product
        Product updatedProduct = productRepository.save(productFromDb);

        //return DTO after mapping product to DTO
        return modelMapper.map(updatedProduct, ProductDto.class);
    }


}
