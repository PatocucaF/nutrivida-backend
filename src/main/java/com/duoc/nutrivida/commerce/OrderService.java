package com.duoc.nutrivida.commerce;

import com.duoc.nutrivida.catalog.InventoryMovementType;
import com.duoc.nutrivida.catalog.InventoryService;
import com.duoc.nutrivida.catalog.Product;
import com.duoc.nutrivida.catalog.ProductRepository;
import com.duoc.nutrivida.common.BusinessRuleException;
import com.duoc.nutrivida.common.NotFoundException;
import com.duoc.nutrivida.user.User;
import com.duoc.nutrivida.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final SaleRepository saleRepository;
    private final ShoppingCartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    public OrderService(CustomerOrderRepository orderRepository, SaleRepository saleRepository,
                        ShoppingCartRepository cartRepository, CartItemRepository cartItemRepository,
                        UserRepository userRepository, ProductRepository productRepository,
                        InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.saleRepository = saleRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public OrderResponse checkout(String email) {
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        ShoppingCart cart = cartRepository.findByUserEmail(email)
                .orElseThrow(() -> new BusinessRuleException("El carrito está vacío"));
        List<CartItem> cartItems = cartItemRepository.findByCartIdOrderByProductId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BusinessRuleException("El carrito está vacío");
        }

        CustomerOrder order = new CustomerOrder();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CONFIRMED);
        BigDecimal total = BigDecimal.ZERO;
        List<Product> lockedProducts = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findByIdForUpdate(cartItem.getProduct().getId())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
            if (!product.isActive()) {
                throw new BusinessRuleException("El producto " + product.getName() + " ya no está disponible");
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BusinessRuleException("Stock insuficiente para el producto " + product.getName());
            }
            product.setStock(product.getStock() - cartItem.getQuantity());
            lockedProducts.add(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setSku(product.getSku());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            order.getItems().add(orderItem);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        order.setTotal(total);
        CustomerOrder savedOrder = orderRepository.save(order);
        for (int i = 0; i < cartItems.size(); i++) {
            inventoryService.saveMovement(lockedProducts.get(i), InventoryMovementType.SALE,
                    -cartItems.get(i).getQuantity(), "Pedido #" + savedOrder.getId(), email);
        }

        Sale sale = new Sale();
        sale.setOrder(savedOrder);
        sale.setTotal(total);
        sale.setStatus(SaleStatus.VALID);
        saleRepository.save(sale);
        cartItemRepository.deleteAllByCartId(cart.getId());
        return OrderResponse.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listCustomerOrders(String email, Pageable pageable) {
        return orderRepository.findByCustomer_EmailOrderByCreatedAtDesc(email, pageable)
                .map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse getCustomerOrder(String email, Long id) {
        CustomerOrder order = orderRepository.findById(id)
                .filter(found -> found.getCustomer().getEmail().equalsIgnoreCase(email))
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getAnyOrder(Long id) {
        return OrderResponse.from(orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado")));
    }

    @Transactional
    public OrderResponse cancel(Long id, String actorEmail) {
        CustomerOrder order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessRuleException("El pedido ya fue cancelado");
        }
        List<Product> products = order.getItems().stream()
                .map(OrderItem::getProduct).map(Product::getId).sorted()
                .map(productId -> productRepository.findByIdForUpdate(productId)
                        .orElseThrow(() -> new NotFoundException("Producto asociado al pedido no encontrado")))
                .toList();
        for (int i = 0; i < order.getItems().size(); i++) {
            OrderItem item = order.getItems().get(i);
            Product product = products.stream().filter(p -> p.getId().equals(item.getProduct().getId()))
                    .findFirst().orElseThrow();
            product.setStock(Math.addExact(product.getStock(), item.getQuantity()));
            inventoryService.saveMovement(product, InventoryMovementType.CANCELLATION, item.getQuantity(),
                    "Cancelación del pedido #" + order.getId(), actorEmail);
        }
        order.setStatus(OrderStatus.CANCELLED);
        Sale sale = saleRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new BusinessRuleException("El pedido no tiene una venta asociada"));
        sale.setStatus(SaleStatus.VOIDED);
        return OrderResponse.from(orderRepository.save(order));
    }
}
