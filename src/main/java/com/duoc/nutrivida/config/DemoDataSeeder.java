package com.duoc.nutrivida.config;

import com.duoc.nutrivida.catalog.CategoryRepository;
import com.duoc.nutrivida.catalog.CategoryRequest;
import com.duoc.nutrivida.catalog.CategoryService;
import com.duoc.nutrivida.catalog.ProductRepository;
import com.duoc.nutrivida.catalog.ProductRequest;
import com.duoc.nutrivida.catalog.ProductService;
import com.duoc.nutrivida.user.Role;
import com.duoc.nutrivida.user.RoleName;
import com.duoc.nutrivida.user.RoleRepository;
import com.duoc.nutrivida.user.User;
import com.duoc.nutrivida.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Carga datos de ejemplo para la demo: un vendedor, un cliente, categorias y productos.
 * Es idempotente (no duplica si ya existen) y se desactiva con app.demo-data.enabled=false.
 * Se ejecuta despues de DataSeeder, que crea los roles y el administrador.
 */
@Component
@Order(2)
public class DemoDataSeeder implements CommandLineRunner {

    private static final String ACTOR = "sistema";

    @Value("${app.demo-data.enabled:false}")
    private boolean enabled;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public DemoDataSeeder(RoleRepository roleRepository, UserRepository userRepository,
                          PasswordEncoder passwordEncoder, CategoryRepository categoryRepository,
                          CategoryService categoryService, ProductRepository productRepository,
                          ProductService productService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        createUser("Vendedor Demo", "vendedor@nutrivida.cl", "Vendedor123", RoleName.VENDEDOR);
        createUser("Cliente Demo", "cliente@nutrivida.cl", "Cliente123", RoleName.CLIENTE);

        Long suplementos = createCategory("Suplementos", "Vitaminas y minerales");
        Long snacks = createCategory("Snacks saludables", "Colaciones bajas en azúcar");
        Long bebidas = createCategory("Bebidas", "Infusiones y bebidas funcionales");

        createProduct("SUP-001", "Vitamina C 500 mg", "Frasco de 60 cápsulas", "8990", 40, suplementos);
        createProduct("SUP-002", "Omega 3", "Frasco de 90 cápsulas", "14990", 25, suplementos);
        createProduct("SNK-001", "Barra de avena y frutos secos", "Caja de 12 unidades", "9990", 30, snacks);
        createProduct("SNK-002", "Mix de frutos secos", "Bolsa de 250 g sin sal", "4990", 50, snacks);
        createProduct("BEB-001", "Té verde orgánico", "Caja de 20 sobres", "3490", 60, bebidas);
        createProduct("BEB-002", "Kombucha de jengibre", "Botella de 330 ml", "2490", 35, bebidas);
    }

    private void createUser(String name, String email, String password, RoleName roleName) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setRoles(new HashSet<>(Set.of(role)));
        userRepository.save(user);
    }

    /** Devuelve el id de la categoria, creandola si no existe. */
    private Long createCategory(String name, String description) {
        if (!categoryRepository.existsByNameIgnoreCase(name)) {
            categoryService.create(new CategoryRequest(name, description, true));
        }
        return categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst().orElseThrow().getId();
    }

    private void createProduct(String sku, String name, String description, String price, int stock, Long categoryId) {
        if (productRepository.existsBySkuIgnoreCase(sku)) {
            return;
        }
        productService.create(new ProductRequest(sku, name, description, new BigDecimal(price), stock,
                categoryId, true), ACTOR);
    }
}
