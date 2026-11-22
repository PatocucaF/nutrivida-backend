package com.duoc.nutrivida.commerce;

import com.duoc.nutrivida.catalog.Product;
import com.duoc.nutrivida.catalog.ProductRepository;
import com.duoc.nutrivida.common.BusinessRuleException;
import com.duoc.nutrivida.common.NotFoundException;
import com.duoc.nutrivida.user.User;
import com.duoc.nutrivida.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    private final ShoppingCartRepository cartRepository;
    private final CartItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(ShoppingCartRepository cartRepository, CartItemRepository itemRepository,
                      ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.itemRepository = itemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse get(String email) {
        return CartResponse.from(cartRepository.findByUserEmail(email)
                .map(cart -> itemRepository.findByCartIdOrderByProductId(cart.getId())).orElse(List.of()));
    }

    @Transactional
    public CartResponse add(String email, Long productId, int quantity) {
        ShoppingCart cart = getOrCreate(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        if (!product.isActive()) {
            throw new BusinessRuleException("El producto no está disponible");
        }
        CartItem item = itemRepository.findByCartIdAndProductId(cart.getId(), productId).orElseGet(() -> {
            CartItem created = new CartItem();
            created.setCart(cart);
            created.setProduct(product);
            return created;
        });
        if (quantity > product.getStock()) {
            throw new BusinessRuleException("La cantidad solicitada supera el stock disponible");
        }
        item.setQuantity(quantity);
        itemRepository.save(item);
        return get(email);
    }

    @Transactional
    public CartResponse remove(String email, Long productId) {
        ShoppingCart cart = cartRepository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("El carrito no existe"));
        CartItem item = itemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new NotFoundException("El producto no está en el carrito"));
        itemRepository.delete(item);
        return get(email);
    }

    @Transactional
    public void clear(String email) {
        cartRepository.findByUserEmail(email)
                .ifPresent(cart -> itemRepository.deleteAllByCartId(cart.getId()));
    }

    private ShoppingCart getOrCreate(String email) {
        return cartRepository.findByUserEmail(email).orElseGet(() -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
            ShoppingCart cart = new ShoppingCart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }
}
