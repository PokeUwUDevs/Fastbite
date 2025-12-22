package com.fastbite.config;

import com.fastbite.model.Product;
import com.fastbite.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initProducts(ProductRepository productRepository) {
        return args -> {
            productRepository.count()
                    .filter(count -> count == 0)
                    .flatMapMany(count -> {
                        List<Product> products = List.of(
                                Product.builder()
                                        .name("Hamburguesa Clásica")
                                        .description("Carne de res, lechuga, tomate, cebolla y salsa especial")
                                        .price(new BigDecimal("8.99"))
                                        .imageUrl("🍔")
                                        .category("Hamburguesas")
                                        .available(true)
                                        .build(),
                                Product.builder()
                                        .name("Hamburguesa Doble")
                                        .description("Doble carne, doble queso, tocino y salsa BBQ")
                                        .price(new BigDecimal("12.99"))
                                        .imageUrl("🍔")
                                        .category("Hamburguesas")
                                        .available(true)
                                        .build(),
                                Product.builder()
                                        .name("Pizza Pepperoni")
                                        .description("Pizza mediana con pepperoni y queso mozzarella")
                                        .price(new BigDecimal("14.99"))
                                        .imageUrl("🍕")
                                        .category("Pizzas")
                                        .available(true)
                                        .build(),
                                Product.builder()
                                        .name("Pizza Hawaiana")
                                        .description("Pizza mediana con jamón y piña")
                                        .price(new BigDecimal("13.99"))
                                        .imageUrl("🍕")
                                        .category("Pizzas")
                                        .available(true)
                                        .build(),
                                Product.builder()
                                        .name("Papas Fritas")
                                        .description("Porción grande de papas fritas crujientes")
                                        .price(new BigDecimal("4.99"))
                                        .imageUrl("🍟")
                                        .category("Acompañamientos")
                                        .available(true)
                                        .build(),
                                Product.builder()
                                        .name("Aros de Cebolla")
                                        .description("Aros de cebolla empanizados")
                                        .price(new BigDecimal("5.99"))
                                        .imageUrl("🧅")
                                        .category("Acompañamientos")
                                        .available(true)
                                        .build(),
                                Product.builder()
                                        .name("Refresco")
                                        .description("Refresco de cola 500ml")
                                        .price(new BigDecimal("2.49"))
                                        .imageUrl("🥤")
                                        .category("Bebidas")
                                        .available(true)
                                        .build(),
                                Product.builder()
                                        .name("Agua Mineral")
                                        .description("Agua mineral 500ml")
                                        .price(new BigDecimal("1.99"))
                                        .imageUrl("💧")
                                        .category("Bebidas")
                                        .available(true)
                                        .build(),
                                Product.builder()
                                        .name("Hot Dog")
                                        .description("Hot dog con salchicha premium y toppings")
                                        .price(new BigDecimal("6.99"))
                                        .imageUrl("🌭")
                                        .category("Hot Dogs")
                                        .available(true)
                                        .build(),
                                Product.builder()
                                        .name("Tacos (3 pzas)")
                                        .description("3 tacos de carne asada con cilantro y cebolla")
                                        .price(new BigDecimal("9.99"))
                                        .imageUrl("🌮")
                                        .category("Tacos")
                                        .available(true)
                                        .build()
                        );
                        return productRepository.saveAll(products);
                    })
                    .subscribe(
                            product -> System.out.println("Producto creado: " + product.getName()),
                            error -> System.err.println("Error al crear productos: " + error.getMessage()),
                            () -> System.out.println("Inicialización de productos completada")
                    );
        };
    }
}
