package com.sportx.backend.config;

import com.sportx.backend.entity.*;
import com.sportx.backend.enums.UserRole;
import com.sportx.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepo;
    private final BrandRepository brandRepo;
    private final ProductRepository productRepo;
    private final ProductImageRepository productImageRepo;
    private final UserRepository userRepo;
    private final CouponRepository couponRepo;

    public DataSeeder(CategoryRepository categoryRepo, BrandRepository brandRepo,
                      ProductRepository productRepo, ProductImageRepository productImageRepo,
                      UserRepository userRepo, CouponRepository couponRepo) {
        this.categoryRepo = categoryRepo;
        this.brandRepo = brandRepo;
        this.productRepo = productRepo;
        this.productImageRepo = productImageRepo;
        this.userRepo = userRepo;
        this.couponRepo = couponRepo;
    }

    @Override
    public void run(String... args) {
        if (categoryRepo.count() > 0) return;

        Category cricket = categoryRepo.save(Category.builder().name("Cricket").description("Cricket equipment & gear").build());
        Category football = categoryRepo.save(Category.builder().name("Football").description("Football & soccer gear").build());
        Category basketball = categoryRepo.save(Category.builder().name("Basketball").description("Basketball equipment").build());
        Category tennis = categoryRepo.save(Category.builder().name("Tennis").description("Tennis rackets & accessories").build());
        Category fitness = categoryRepo.save(Category.builder().name("Fitness").description("Gym & fitness equipment").build());

        Brand nike = brandRepo.save(Brand.builder().name("Nike").description("Leading sports brand").build());
        Brand adidas = brandRepo.save(Brand.builder().name("Adidas").description("Premium sportswear").build());
        Brand puma = brandRepo.save(Brand.builder().name("Puma").description("Sport & lifestyle").build());
        Brand sg = brandRepo.save(Brand.builder().name("SG").description("Cricket specialist").build());
        Brand yonex = brandRepo.save(Brand.builder().name("Yonex").description("Racket sports expert").build());

        List<String> cricketImages = List.of(
            "https://images.unsplash.com/photo-1531415074968-036ba1b575da?w=400",
            "https://images.unsplash.com/photo-1561715276-a2d2e2848d4b?w=400",
            "https://images.unsplash.com/photo-1591022020517-1f90e8d2465d?w=400",
            "https://images.unsplash.com/photo-1611312449408-fcece27cdbb7?w=400"
        );
        List<String> footballImages = List.of(
            "https://images.unsplash.com/photo-1614632537197-38a17061c2bd?w=400",
            "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=400",
            "https://images.unsplash.com/photo-1623854988844-13b096aaa240?w=400",
            "https://images.unsplash.com/photo-1622964313878-6d2f8ae49f7c?w=400"
        );
        List<String> basketballImages = List.of(
            "https://images.unsplash.com/photo-1519861531473-9200262188bf?w=400",
            "https://images.unsplash.com/photo-1515524738708-327f6b0037a7?w=400",
            "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400"
        );
        List<String> tennisImages = List.of(
            "https://images.unsplash.com/photo-1617083934555-ac7d4e0d0b2d?w=400",
            "https://images.unsplash.com/photo-1623854988844-13b096aaa240?w=400",
            "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=400"
        );
        List<String> fitnessImages = List.of(
            "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=400",
            "https://images.unsplash.com/photo-1638536532686-d610adfc8e5c?w=400",
            "https://images.unsplash.com/photo-1598289431512-b97b0917affc?w=400",
            "https://images.unsplash.com/photo-1601422407692-ec4eeec1d9b3?w=400",
            "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=400"
        );

        List<Product> products = productRepo.saveAll(List.of(
            Product.builder().name("Cricket Bat").description("Professional grade Kashmir willow cricket bat").price(new BigDecimal("2499")).category(cricket).brand(sg).stockQuantity(50).build(),
            Product.builder().name("Cricket Ball (Leather)").description("Premium leather cricket ball for match play").price(new BigDecimal("799")).category(cricket).brand(sg).stockQuantity(200).build(),
            Product.builder().name("Cricket Helmet").description("Lightweight helmet with steel grille").price(new BigDecimal("3499")).discountedPrice(new BigDecimal("2999")).category(cricket).brand(sg).stockQuantity(30).build(),
            Product.builder().name("Cricket Gloves").description("Full-featured batting gloves with PU protection").price(new BigDecimal("1599")).category(cricket).brand(sg).stockQuantity(75).build(),
            Product.builder().name("Football").description("Size 5 FIFA-approved match ball").price(new BigDecimal("1999")).discountedPrice(new BigDecimal("1499")).category(football).brand(nike).stockQuantity(100).build(),
            Product.builder().name("Football Boots").description("Lightweight FG/AG football boots").price(new BigDecimal("5999")).category(football).brand(adidas).stockQuantity(40).build(),
            Product.builder().name("Goalkeeper Gloves").description("Professional cut latex goalkeeper gloves").price(new BigDecimal("2499")).category(football).brand(puma).stockQuantity(35).build(),
            Product.builder().name("Shin Guards").description("Lightweight slip-in shin guards").price(new BigDecimal("699")).category(football).brand(nike).stockQuantity(150).build(),
            Product.builder().name("Basketball").description("Size 7 indoor/outdoor basketball").price(new BigDecimal("1799")).category(basketball).brand(nike).stockQuantity(80).build(),
            Product.builder().name("Basketball Jersey").description("Breathable mesh basketball jersey").price(new BigDecimal("1299")).category(basketball).brand(adidas).stockQuantity(60).build(),
            Product.builder().name("Basketball Shoes").description("High-top basketball shoes with ankle support").price(new BigDecimal("7999")).discountedPrice(new BigDecimal("6499")).category(basketball).brand(nike).stockQuantity(25).build(),
            Product.builder().name("Tennis Racket").description("Graphite frame tennis racket, 300g").price(new BigDecimal("4499")).category(tennis).brand(yonex).stockQuantity(30).build(),
            Product.builder().name("Tennis Balls (Pack of 3)").description("Pressureless training tennis balls").price(new BigDecimal("499")).category(tennis).brand(yonex).stockQuantity(200).build(),
            Product.builder().name("Tennis Shoes").description("Clay court tennis shoes").price(new BigDecimal("5499")).category(tennis).brand(adidas).stockQuantity(20).build(),
            Product.builder().name("Yoga Mat").description("Extra thick non-slip yoga mat").price(new BigDecimal("1499")).category(fitness).brand(puma).stockQuantity(100).build(),
            Product.builder().name("Dumbbell Set (10kg)").description("Adjustable dumbbell set with stand").price(new BigDecimal("3999")).discountedPrice(new BigDecimal("3499")).category(fitness).brand(nike).stockQuantity(20).build(),
            Product.builder().name("Resistance Bands Set").description("5-level resistance band set").price(new BigDecimal("999")).category(fitness).brand(adidas).stockQuantity(150).build(),
            Product.builder().name("Jump Rope").description("Speed jump rope with ball bearings").price(new BigDecimal("399")).category(fitness).brand(puma).stockQuantity(300).build(),
            Product.builder().name("Water Bottle").description("1L insulated stainless steel water bottle").price(new BigDecimal("899")).category(fitness).brand(nike).stockQuantity(250).build()
        ));

        List<List<String>> allImages = List.of(cricketImages, footballImages, basketballImages, tennisImages, fitnessImages);
        int[] productCounts = {4, 4, 3, 3, 5};

        int idx = 0;
        for (int catIdx = 0; catIdx < allImages.size(); catIdx++) {
            for (int imgIdx = 0; imgIdx < productCounts[catIdx]; imgIdx++) {
                Product p = products.get(idx);
                String url = allImages.get(catIdx).get(imgIdx);
                productImageRepo.save(ProductImage.builder().product(p).imageUrl(url).isPrimary(true).build());
                idx++;
            }
        }

        if (userRepo.count() == 0) {
            userRepo.save(User.builder()
                .email("admin@sportx.com")
                .password("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy")
                .firstName("Admin")
                .lastName("SportX")
                .role(UserRole.ROLE_ADMIN)
                .enabled(true)
                .build()
            );
        }

        if (couponRepo.count() == 0) {
            couponRepo.saveAll(List.of(
                Coupon.builder().code("WELCOME10").discountPercent(new BigDecimal("10")).maxDiscount(new BigDecimal("500")).minOrderAmount(new BigDecimal("999")).usageLimit(100).active(true).build(),
                Coupon.builder().code("SPORTX20").discountPercent(new BigDecimal("20")).maxDiscount(new BigDecimal("1000")).minOrderAmount(new BigDecimal("2499")).usageLimit(50).active(true).build(),
                Coupon.builder().code("FREEDEL").discountPercent(new BigDecimal("5")).maxDiscount(new BigDecimal("200")).minOrderAmount(new BigDecimal("499")).usageLimit(200).active(true).build()
            ));
        }
    }
}
