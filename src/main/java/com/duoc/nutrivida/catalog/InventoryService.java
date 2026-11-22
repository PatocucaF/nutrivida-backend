package com.duoc.nutrivida.catalog;

import com.duoc.nutrivida.common.BusinessRuleException;
import com.duoc.nutrivida.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryMovementRepository movementRepository;

    public InventoryService(ProductRepository productRepository, InventoryMovementRepository movementRepository) {
        this.productRepository = productRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional
    public InventoryResponse adjust(Long productId, InventoryAdjustmentRequest request, String actorEmail) {
        if (request.type() != InventoryMovementType.RESTOCK) {
            throw new BusinessRuleException("Solo se permiten ajustes de tipo RESTOCK; ventas y anulaciones son automáticas");
        }
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        product.setStock(Math.addExact(product.getStock(), request.quantity()));
        productRepository.save(product);
        InventoryMovement movement = saveMovement(product, InventoryMovementType.RESTOCK,
                request.quantity(), request.note(), actorEmail);
        return InventoryResponse.from(movement);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> history(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Producto no encontrado");
        }
        return movementRepository.findTop100ByProductIdOrderByCreatedAtDesc(productId)
                .stream().map(InventoryResponse::from).toList();
    }

    public InventoryMovement saveMovement(Product product, InventoryMovementType type, int quantityChange,
                                          String note, String actorEmail) {
        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(product);
        movement.setType(type);
        movement.setQuantityChange(quantityChange);
        movement.setNote(note);
        movement.setActorEmail(actorEmail);
        return movementRepository.save(movement);
    }
}
