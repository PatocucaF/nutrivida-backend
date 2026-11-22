package com.duoc.nutrivida.catalog;

import com.duoc.nutrivida.common.ConflictException;
import com.duoc.nutrivida.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryService inventoryService;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
                          InventoryService inventoryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list(Long categoryId, boolean includeInactive) {
        List<Product> products;
        if (includeInactive) {
            products = productRepository.findAll();
            if (categoryId != null) {
                products = products.stream().filter(p -> p.getCategory().getId().equals(categoryId)).toList();
            }
        } else {
            products = categoryId == null ? productRepository.findByActiveTrueOrderByNameAsc()
                    : productRepository.findByCategoryIdAndActiveTrueOrderByNameAsc(categoryId);
        }
        return products.stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductResponse.from(findProduct(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request, String actorEmail) {
        if (productRepository.existsBySkuIgnoreCase(request.sku().trim())) {
            throw new ConflictException("Ya existe un producto con ese SKU");
        }
        Product product = new Product();
        apply(product, request);
        product.setStock(request.initialStock());
        Product saved = productRepository.save(product);
        if (saved.getStock() > 0) {
            inventoryService.saveMovement(saved, InventoryMovementType.INITIAL, saved.getStock(),
                    "Stock inicial", actorEmail);
        }
        return ProductResponse.from(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = findProduct(id);
        if (productRepository.existsBySkuIgnoreCaseAndIdNot(request.sku().trim(), id)) {
            throw new ConflictException("Ya existe un producto con ese SKU");
        }
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
        product.setSku(request.sku().trim());
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(category);
        product.setActive(request.active() == null || request.active());
        return ProductResponse.from(productRepository.save(product));
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new NotFoundException("Producto no encontrado"));
    }

    private void apply(Product product, ProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
        product.setSku(request.sku().trim());
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(category);
        product.setActive(request.active() == null || request.active());
    }
}
