package com.duoc.nutrivida.commerce;

import com.duoc.nutrivida.catalog.Category;
import com.duoc.nutrivida.catalog.InventoryMovementType;
import com.duoc.nutrivida.catalog.InventoryService;
import com.duoc.nutrivida.catalog.Product;
import com.duoc.nutrivida.catalog.ProductRepository;
import com.duoc.nutrivida.common.BusinessRuleException;
import com.duoc.nutrivida.user.User;
import com.duoc.nutrivida.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private CustomerOrderRepository orderRepository;
    @Mock private SaleRepository saleRepository;
    @Mock private ShoppingCartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryService inventoryService;

    @InjectMocks private OrderService service;

    private User customer;
    private ShoppingCart cart;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(7L);
        customer.setEmail("cliente@nutrivida.cl");
        customer.setName("Cliente");

        cart = new ShoppingCart();
        cart.setId(11L);
        cart.setUser(customer);

        Category category = new Category();
        category.setId(3L);
        category.setName("Alimentos");

        product = new Product();
        product.setId(5L);
        product.setSku("SKU-5");
        product.setName("Avena");
        product.setPrice(new BigDecimal("1250.00"));
        product.setStock(8);
        product.setActive(true);
        product.setCategory(category);

        cartItem = new CartItem();
        cartItem.setId(13L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
    }

    @Test
    void checkoutCreaPedidoVentaYDescuentaInventario() {
        when(userRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        when(cartRepository.findByUserEmail(customer.getEmail())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByProductId(cart.getId())).thenReturn(List.of(cartItem));
        when(productRepository.findByIdForUpdate(product.getId())).thenReturn(Optional.of(product));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> {
            CustomerOrder order = invocation.getArgument(0);
            order.setId(20L);
            return order;
        });

        OrderResponse response = service.checkout(customer.getEmail());

        assertEquals(new BigDecimal("2500.00"), response.total());
        assertEquals(OrderStatus.CONFIRMED, response.status());
        assertEquals(6, product.getStock());
        assertEquals(1, response.items().size());
        verify(inventoryService).saveMovement(product, InventoryMovementType.SALE, -2, "Pedido #20",
                customer.getEmail());
        verify(saleRepository).save(any(Sale.class));
        verify(cartItemRepository).deleteAllByCartId(cart.getId());
    }

    @Test
    void checkoutRechazaStockInsuficienteSinCrearPedidoNiVenta() {
        product.setStock(1);
        when(userRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        when(cartRepository.findByUserEmail(customer.getEmail())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByProductId(cart.getId())).thenReturn(List.of(cartItem));
        when(productRepository.findByIdForUpdate(product.getId())).thenReturn(Optional.of(product));

        assertThrows(BusinessRuleException.class, () -> service.checkout(customer.getEmail()));

        assertEquals(1, product.getStock());
        verify(orderRepository, never()).save(any());
        verify(saleRepository, never()).save(any());
        verify(cartItemRepository, never()).deleteAllByCartId(cart.getId());
    }

    @Test
    void checkoutRechazaCarritoVacio() {
        when(userRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        when(cartRepository.findByUserEmail(customer.getEmail())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByProductId(cart.getId())).thenReturn(List.of());

        assertThrows(BusinessRuleException.class, () -> service.checkout(customer.getEmail()));

        verify(orderRepository, never()).save(any());
        verify(saleRepository, never()).save(any());
    }
}
