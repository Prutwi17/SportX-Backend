package com.sportx.backend.config;

import com.sportx.backend.entity.*;
import com.sportx.backend.enums.UserRole;
import com.sportx.backend.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    @Value("${app.seed.demo-data:true}")
    private boolean seedDemoData;

    @Value("${app.seed.admin:true}")
    private boolean seedAdmin;

    private final CategoryRepository categoryRepo;
    private final BrandRepository brandRepo;
    private final ProductRepository productRepo;
    private final ProductImageRepository productImageRepo;
    private final UserRepository userRepo;
    private final CouponRepository couponRepo;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final CartItemRepository cartItemRepo;
    private final WishlistItemRepository wishlistItemRepo;
    private final ReviewRepository reviewRepo;

    public DataSeeder(CategoryRepository categoryRepo, BrandRepository brandRepo,
                      ProductRepository productRepo, ProductImageRepository productImageRepo,
                      UserRepository userRepo, CouponRepository couponRepo,
                      PasswordEncoder passwordEncoder, OrderRepository orderRepo,
                      OrderItemRepository orderItemRepo, CartItemRepository cartItemRepo,
                      WishlistItemRepository wishlistItemRepo, ReviewRepository reviewRepo) {
        this.categoryRepo = categoryRepo;
        this.brandRepo = brandRepo;
        this.productRepo = productRepo;
        this.productImageRepo = productImageRepo;
        this.userRepo = userRepo;
        this.couponRepo = couponRepo;
        this.passwordEncoder = passwordEncoder;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.cartItemRepo = cartItemRepo;
        this.wishlistItemRepo = wishlistItemRepo;
        this.reviewRepo = reviewRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Admin User Setup (Only when seedAdmin is explicitly enabled)
        if (seedAdmin) {
            User adminUser = userRepo.findByEmail("admin@sportx.com").orElse(null);
            if (adminUser == null) {
                userRepo.save(User.builder()
                    .email("admin@sportx.com")
                    .password(passwordEncoder.encode("admin"))
                    .firstName("Admin")
                    .lastName("SportX")
                    .role(UserRole.ROLE_ADMIN)
                    .enabled(true)
                    .build()
                );
            }
        }

        // Coupons
        if (couponRepo.count() == 0) {
            couponRepo.saveAll(List.of(
                Coupon.builder().code("WELCOME10").discountPercent(new BigDecimal("10")).maxDiscount(new BigDecimal("500")).minOrderAmount(new BigDecimal("999")).usageLimit(100).active(true).build(),
                Coupon.builder().code("SPORTX20").discountPercent(new BigDecimal("20")).maxDiscount(new BigDecimal("1000")).minOrderAmount(new BigDecimal("2499")).usageLimit(50).active(true).build(),
                Coupon.builder().code("FREEDEL").discountPercent(new BigDecimal("5")).maxDiscount(new BigDecimal("200")).minOrderAmount(new BigDecimal("499")).usageLimit(200).active(true).build()
            ));
        }

        // Map Categories
        Map<String, Category> catMap = new HashMap<>();
        String[] catNames = {
            "Football Boots", "Football Jerseys", "Footballs", "Cricket Bats",
            "Cricket Balls", "Cricket Gloves", "Cricket Pads", "Cricket Helmets",
            "Tennis Rackets", "Tennis Balls", "Badminton Rackets", "Shuttlecocks",
            "Running Shoes", "Gym Equipment", "Cricket", "Football", "Basketball", "Tennis", "Fitness"
        };
        for (String name : catNames) {
            Category c = categoryRepo.findByName(name).orElseGet(() ->
                categoryRepo.save(Category.builder().name(name).description("Premium " + name).build())
            );
            catMap.put(name, c);
        }

        // Map Brands
        Map<String, Brand> brandMap = new HashMap<>();
        String[] brandNames = {
            "Nike", "Adidas", "Puma", "SG", "SS", "GM", "MRF", "Kookaburra",
            "Yonex", "Nivia", "Cosco", "Reebok", "ASICS", "New Balance", "Under Armour"
        };
        for (String name : brandNames) {
            Brand b = brandRepo.findByName(name).orElseGet(() ->
                brandRepo.save(Brand.builder().name(name).description(name + " Sports Equipment").build())
            );
            brandMap.put(name, b);
        }

        // Product Definition Class
        class ItemDef {
            String name; String desc; String cat; String brand; String price; String discPrice; int stock; String img;
            ItemDef(String n, String d, String c, String b, String p, String dp, int s, String i) {
                name = n; desc = d; cat = c; brand = b; price = p; discPrice = dp; stock = s; img = i;
            }
        }

        // Seed the 8 featured products ONLY when the products table is empty.
        // (Never wipe existing products/orders/carts/wishlists/reviews on startup —
        //  that would destroy admin-created products and customer order history.)
        if (!seedDemoData || productRepo.count() > 0) {
            return;
        }

        List<ItemDef> items = new ArrayList<>();

        // USER SPECIFIED EXACT 8 FEATURED PRODUCTS ONLY
        items.add(new ItemDef(
            "Nike Vapor 16 Pro Mercurial Dream Speed",
            "Explosive speed Flyknit football boots for firm ground grass pitches",
            "Football Boots", "Nike", "18995", "15995", 35,
            "/uploads/feature_product/nike_vapor_16_dream_speed.jpg"
        ));
        items.add(new ItemDef(
            "Nike Mercurial Vapor 16 Elite LV8",
            "Top-tier Elite LV8 edition FlyTouch football boots with metallic finish",
            "Football Boots", "Nike", "21995", "18495", 30,
            "/uploads/feature_product/nike_vapor_16_elite_lv8.jpg"
        ));
        items.add(new ItemDef(
            "Puma Electrify Nitro 4 Running Shoes",
            "Lightweight dual-cushion NITROFOAM running shoes in Off-White edition",
            "Running Shoes", "Puma", "11999", "9999", 40,
            "/uploads/feature_product/puma_electrify_nitro_4.png"
        ));
        items.add(new ItemDef(
            "Real Madrid Home Jersey Fan Edition",
            "Official Real Madrid 2024/25 fan edition home match kit with embroidered crest",
            "Football Jerseys", "Adidas", "4999", "4299", 90,
            "/uploads/feature_product/real_madrid_home_jersey.png"
        ));
        items.add(new ItemDef(
            "Puma x RCB Official Match Jersey 2026",
            "Official Royal Challengers Bengaluru 2026 match jersey with dryCELL tech",
            "Football Jerseys", "Puma", "3999", "3499", 100,
            "/uploads/feature_product/puma_rcb_jersey_2026.png"
        ));
        items.add(new ItemDef(
            "PUMA LaLiga 1 Accelerate Match Ball",
            "Official 12-panel FIFA Quality Pro match ball engineered for LaLiga matches",
            "Footballs", "Puma", "10999", "8999", 50,
            "/uploads/feature_product/puma_football_laliga_accelerate.png"
        ));
        items.add(new ItemDef(
            "Kookaburra Kahuna Pro English Willow Bat",
            "Handcrafted Grade 1 English Willow cricket bat with massive profile and light pickup",
            "Cricket Bats", "Kookaburra", "38999", "34999", 25,
            "/uploads/feature_product/cricket_bat_kookaburra.png"
        ));
        items.add(new ItemDef(
            "Red Leather Match Cricket Ball",
            "Premium alum-tanned 4-piece red leather match ball with hand-stitched seam",
            "Cricket Balls", "SG", "2499", "1999", 80,
            "/uploads/feature_product/cricket_leather_ball.jpg"
        ));

        // Insert EXACT 8 items
        for (ItemDef item : items) {
            Category cat = catMap.get(item.cat);
            Brand b = brandMap.get(item.brand);
            if (cat == null || b == null) continue;

            Product p = Product.builder()
                .name(item.name)
                .description(item.desc)
                .price(new BigDecimal(item.price))
                .discountedPrice(item.discPrice != null ? new BigDecimal(item.discPrice) : null)
                .category(cat)
                .brand(b)
                .stockQuantity(item.stock)
                .active(true)
                .averageRating(4.8)
                .ratingCount(120)
                .build();

            p = productRepo.save(p);

            productImageRepo.save(ProductImage.builder()
                .product(p)
                .imageUrl(item.img)
                .isPrimary(true)
                .build()
            );
        }
    }
}
