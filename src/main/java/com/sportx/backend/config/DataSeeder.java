package com.sportx.backend.config;

import com.sportx.backend.entity.*;
import com.sportx.backend.enums.UserRole;
import com.sportx.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepo;
    private final BrandRepository brandRepo;
    private final ProductRepository productRepo;
    private final ProductImageRepository productImageRepo;
    private final UserRepository userRepo;
    private final CouponRepository couponRepo;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CategoryRepository categoryRepo, BrandRepository brandRepo,
                      ProductRepository productRepo, ProductImageRepository productImageRepo,
                      UserRepository userRepo, CouponRepository couponRepo,
                      PasswordEncoder passwordEncoder) {
        this.categoryRepo = categoryRepo;
        this.brandRepo = brandRepo;
        this.productRepo = productRepo;
        this.productImageRepo = productImageRepo;
        this.userRepo = userRepo;
        this.couponRepo = couponRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Admin User Setup
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
        } else {
            adminUser.setPassword(passwordEncoder.encode("admin"));
            adminUser.setRole(UserRole.ROLE_ADMIN);
            adminUser.setEnabled(true);
            userRepo.save(adminUser);
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

        List<ItemDef> items = new ArrayList<>();

        // High quality Unsplash sports image URLs
        String imgBoot = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600";
        String imgJersey = "https://images.unsplash.com/photo-1580089054957-3822a9a07ce0?w=600";
        String imgFootball = "https://images.unsplash.com/photo-1614632537197-38a17061c2bd?w=600";
        String imgBat = "https://images.unsplash.com/photo-1531415074968-036ba1b575da?w=600";
        String imgBall = "https://images.unsplash.com/photo-1561715276-a2d2e2848d4b?w=600";
        String imgGloves = "https://images.unsplash.com/photo-1611312449408-fcece27cdbb7?w=600";
        String imgPads = "https://images.unsplash.com/photo-1631726454161-d5f9b334baa6?w=600";
        String imgHelmet = "https://images.unsplash.com/photo-1591022020517-1f90e8d2465d?w=600";
        String imgTennisRacket = "https://images.unsplash.com/photo-1617083934555-ac7d4e0d0b2d?w=600";
        String imgTennisBall = "https://images.unsplash.com/photo-1622279457486-28e24c392b03?w=600";
        String imgBadmintonRacket = "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=600";
        String imgShuttle = "https://images.unsplash.com/photo-1599474924187-334a4ae5bd3c?w=600";
        String imgShoes = "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=600";
        String imgGym = "https://images.unsplash.com/photo-1638536532686-d610adfc8e5c?w=600";

        // 1. FOOTBALL BOOTS (10 items)
        items.add(new ItemDef("Nike Mercurial Superfly 9 Elite FG", "Lightweight Flyknit football boot for explosive speed", "Football Boots", "Nike", "21995", "19995", 40, imgBoot));
        items.add(new ItemDef("Nike Phantom GX II Elite FG", "Precision grip football boot with Gripknit technology", "Football Boots", "Nike", "20995", "18495", 35, imgBoot));
        items.add(new ItemDef("Nike Tiempo Legend 10 Elite", "FlyTouch Plus engineered leather touch boot", "Football Boots", "Nike", "19495", null, 45, imgBoot));
        items.add(new ItemDef("Adidas Predator Elite FT FG", "Fold-over tongue boots with Strikeskin rubber fins", "Football Boots", "Adidas", "22999", "20499", 30, imgBoot));
        items.add(new ItemDef("Adidas Copa Pure 2.1 FG", "Fusionskin leather boots for silky touch and comfort", "Football Boots", "Adidas", "17999", null, 50, imgBoot));
        items.add(new ItemDef("Adidas X Crazyfast.1 FG", "Aerocage speedframe boot designed for pure acceleration", "Football Boots", "Adidas", "19999", "17999", 40, imgBoot));
        items.add(new ItemDef("Puma Future 7 Ultimate FG/AG", "FUZIONFIT360 dual mesh upper with PWRTAPE stability", "Football Boots", "Puma", "18999", "16999", 35, imgBoot));
        items.add(new ItemDef("Puma Ultra Ultimate FG/AG", "ULTRAWEAVE ultra-light fabric boot for extreme velocity", "Football Boots", "Puma", "17999", null, 42, imgBoot));
        items.add(new ItemDef("Puma King Ultimate FG", "K-BETTER non-animal upper material match boot", "Football Boots", "Puma", "15999", "13999", 30, imgBoot));
        items.add(new ItemDef("Nivia Carbonite Real Football Studs", "Durable TPU studs for firm Indian ground grass pitches", "Football Boots", "Nivia", "1499", "1299", 100, imgBoot));

        // 2. FOOTBALL JERSEYS (14 items)
        items.add(new ItemDef("Real Madrid Home Jersey 2024/25", "Official Adidas Real Madrid home kit with gold detailing", "Football Jerseys", "Adidas", "4999", "4499", 60, imgJersey));
        items.add(new ItemDef("Real Madrid Away Jersey 2024/25", "Official Adidas Real Madrid orange away kit", "Football Jerseys", "Adidas", "4999", null, 50, imgJersey));
        items.add(new ItemDef("FC Barcelona Home Jersey 2024/25", "Official Nike Blaugrana home kit with Dri-FIT ADV", "Football Jerseys", "Nike", "4999", "4299", 55, imgJersey));
        items.add(new ItemDef("Manchester United Home Jersey", "Official Adidas Red Devils home match jersey", "Football Jerseys", "Adidas", "4999", null, 48, imgJersey));
        items.add(new ItemDef("Liverpool FC Home Jersey", "Official Nike Reds home kit with ribbed collar", "Football Jerseys", "Nike", "4999", "4399", 52, imgJersey));
        items.add(new ItemDef("Arsenal FC Home Jersey", "Official Adidas Gunners home kit with gold cannon emblem", "Football Jerseys", "Adidas", "4999", null, 45, imgJersey));
        items.add(new ItemDef("Chelsea FC Home Jersey", "Official Nike Blues home stadium jersey", "Football Jerseys", "Nike", "4999", null, 40, imgJersey));
        items.add(new ItemDef("Manchester City Home Jersey", "Official Puma Sky Blue home kit with 0161 city code", "Football Jerseys", "Puma", "4999", "4499", 58, imgJersey));
        items.add(new ItemDef("PSG Home Jersey 2024/25", "Official Nike Paris Saint-Germain home kit", "Football Jerseys", "Nike", "4999", null, 38, imgJersey));
        items.add(new ItemDef("Juventus Home Jersey 2024/25", "Official Adidas Bianconeri striped home kit", "Football Jerseys", "Adidas", "4999", null, 35, imgJersey));
        items.add(new ItemDef("India Cricket Official Match Jersey", "Official Adidas Team India T20 World Cup Champions jersey", "Football Jerseys", "Adidas", "3499", "2999", 120, imgJersey));
        items.add(new ItemDef("RCB Official IPL Jersey", "Royal Challengers Bengaluru official fan match jersey", "Football Jerseys", "Puma", "1999", "1699", 150, imgJersey));
        items.add(new ItemDef("SRH Official IPL Jersey", "Sunrisers Hyderabad orange army official IPL jersey", "Football Jerseys", "Puma", "1799", null, 80, imgJersey));
        items.add(new ItemDef("GT Gujarat Titans Official Jersey", "Gujarat Titans blue & teal official IPL match jersey", "Football Jerseys", "Puma", "1799", null, 75, imgJersey));

        // 3. FOOTBALLS (6 items)
        items.add(new ItemDef("Nike Flight FIFA Quality Pro Football", "3D printed Aerowsculpt grooves for true flight path", "Footballs", "Nike", "11995", "9995", 30, imgFootball));
        items.add(new ItemDef("Adidas Euro 2024 Fussballliebe Match Ball", "Official UEFA Euro match ball with Connected Ball Tech", "Footballs", "Adidas", "12999", null, 25, imgFootball));
        items.add(new ItemDef("Puma Orbita La Liga Official Ball", "12-panel FIFA Quality Pro match ball", "Footballs", "Puma", "10999", "8999", 28, imgFootball));
        items.add(new ItemDef("Nivia Shining Star Football", "32-panel hand stitched TPU outer football", "Footballs", "Nivia", "1299", "999", 150, imgFootball));
        items.add(new ItemDef("Nivia Ashtang FIFA Approved Football", "Thermally bonded seamless official match ball", "Footballs", "Nivia", "1899", null, 80, imgFootball));
        items.add(new ItemDef("Cosco Rio Football", "Size 5 synthetic rubber molded training ball", "Footballs", "Cosco", "899", "699", 200, imgFootball));

        // 4. CRICKET BATS (12 items)
        items.add(new ItemDef("SG Players Edition English Willow Bat", "Hand-crafted Grade 1+ English Willow used by international pros", "Cricket Bats", "SG", "48000", "42500", 15, imgBat));
        items.add(new ItemDef("SG Sunny Tonny Classic English Willow Bat", "Traditional round handle bat with thick edges and deep sweet spot", "Cricket Bats", "SG", "24999", "21999", 25, imgBat));
        items.add(new ItemDef("SS Ton Reserve Edition English Willow Bat", "Air dried Grade 1 English Willow with massive profile", "Cricket Bats", "SS", "38000", "34000", 18, imgBat));
        items.add(new ItemDef("SS Super Select Player Grade Bat", "Custom shaped lightweight English Willow with superb balance", "Cricket Bats", "SS", "45000", null, 12, imgBat));
        items.add(new ItemDef("GM Diamond 909 English Willow Bat", "Ben Stokes signature profile with hex grip and L555 blade", "Cricket Bats", "GM", "32000", "28500", 20, imgBat));
        items.add(new ItemDef("GM Icon Original English Willow Bat", "Mid-to-high sweet spot bat engineered for dynamic strokeplay", "Cricket Bats", "GM", "29000", null, 22, imgBat));
        items.add(new ItemDef("MRF Genius Grand Edition Bat", "Virat Kohli signature series Grade 1 English Willow bat", "Cricket Bats", "MRF", "42000", "37999", 16, imgBat));
        items.add(new ItemDef("MRF Legend VK 18 English Willow Bat", "Optimum spine height with ultra-thick toe and light pickup", "Cricket Bats", "MRF", "35000", null, 20, imgBat));
        items.add(new ItemDef("Kookaburra Kahuna Pro English Willow Bat", "Iconic green Kahuna profile used by Ricky Ponting & Jos Buttler", "Cricket Bats", "Kookaburra", "39000", "35000", 15, imgBat));
        items.add(new ItemDef("Kookaburra Ghost Pro English Willow Bat", "Clean white graphics with full profile and minimal scalloping", "Cricket Bats", "Kookaburra", "31000", null, 18, imgBat));
        items.add(new ItemDef("SG Sierra Kashmir Willow Bat", "Premium Kashmir Willow bat ideal for club and academy players", "Cricket Bats", "SG", "2999", "2499", 60, imgBat));
        items.add(new ItemDef("SS Master Kashmir Willow Bat", "Full size Kashmir Willow bat with protective toe guard", "Cricket Bats", "SS", "2799", null, 70, imgBat));

        // 5. CRICKET BALLS (6 items)
        items.add(new ItemDef("Kookaburra Turf Pink Test Ball", "Official International Day-Night Test Match Ball", "Cricket Balls", "Kookaburra", "5499", "4899", 50, imgBall));
        items.add(new ItemDef("Kookaburra Turf Red Match Ball", "Hand stitched 4-piece alum tanned red leather ball", "Cricket Balls", "Kookaburra", "4999", null, 60, imgBall));
        items.add(new ItemDef("Kookaburra White One-Day Ball", "Official ODI white ball with high visibility coating", "Cricket Balls", "Kookaburra", "4799", null, 55, imgBall));
        items.add(new ItemDef("SG Test Red Leather Ball", "BCCI official Test Match red leather cricket ball", "Cricket Balls", "SG", "2299", "1999", 120, imgBall));
        items.add(new ItemDef("SG Tournament Leather Ball", "Alum tanned 4-piece leather ball for 50-over matches", "Cricket Balls", "SG", "1499", null, 150, imgBall));
        items.add(new ItemDef("SG Club White Leather Ball", "High quality white leather ball for T20 tournaments", "Cricket Balls", "SG", "1299", "1099", 180, imgBall));

        // 6. CRICKET GLOVES (5 items)
        items.add(new ItemDef("SG Test Batting Gloves", "Pittards leather palm with high density foam finger rolls", "Cricket Gloves", "SG", "3499", "2999", 40, imgGloves));
        items.add(new ItemDef("SS Ton Limited Edition Gloves", "Premium sheep leather palm with split-finger flexibility", "Cricket Gloves", "SS", "3899", null, 35, imgGloves));
        items.add(new ItemDef("GM Diamond Pro Batting Gloves", "Calf leather palm with Poron XRD impact protection", "Cricket Gloves", "GM", "2999", null, 45, imgGloves));
        items.add(new ItemDef("Kookaburra Kahuna Pro Gloves", "Kookaburra MAX FLO ventilation with airflow mesh", "Cricket Gloves", "Kookaburra", "3299", "2899", 38, imgGloves));
        items.add(new ItemDef("SG Club Batting Gloves", "Durable PU gloves with cotton palm for practice sessions", "Cricket Gloves", "SG", "1499", null, 80, imgGloves));

        // 7. CRICKET PADS (4 items)
        items.add(new ItemDef("SG Test Batting Pads", "Ultra-lightweight cane construction with molded knee cap", "Cricket Pads", "SG", "5999", "5199", 30, imgPads));
        items.add(new ItemDef("SS Ton Super Select Pads", "High density foam bolster with memory foam instep", "Cricket Pads", "SS", "6499", null, 25, imgPads));
        items.add(new ItemDef("GM Icon Pro Batting Legguards", "Vertical cane ribs with twin-wing protection design", "Cricket Pads", "GM", "5299", null, 28, imgPads));
        items.add(new ItemDef("Kookaburra Ghost Pro Pads", "Traditional 7 cane construction with mesh instep", "Cricket Pads", "Kookaburra", "5899", "5199", 26, imgPads));

        // 8. CRICKET HELMETS (3 items)
        items.add(new ItemDef("SG Lightweight Titanium Grille Helmet", "High impact ABS shell with titanium face grille", "Cricket Helmets", "SG", "4999", "4299", 30, imgHelmet));
        items.add(new ItemDef("SS Matrix Steel Grille Helmet", "Adjustable rear headband with powder-coated steel grille", "Cricket Helmets", "SS", "2999", null, 40, imgHelmet));
        items.add(new ItemDef("SG Optipro Cricket Helmet", "Traditional cloth covered shell with ear protection pads", "Cricket Helmets", "SG", "2499", "2199", 50, imgHelmet));

        // 9. TENNIS RACKETS (4 items)
        items.add(new ItemDef("Yonex EZONE 98 Tennis Racket", "ISOMETRIC head shape with Vibration Dampening Mesh (VDM)", "Tennis Rackets", "Yonex", "21990", "19490", 25, imgTennisRacket));
        items.add(new ItemDef("Yonex VCORE 98 Tennis Racket", "Spin-oriented graphite racket used by Denis Shapovalov", "Tennis Rackets", "Yonex", "22490", null, 20, imgTennisRacket));
        items.add(new ItemDef("Yonex Percept 100 Racket", "Control-focused frame with Servo Filter technology", "Tennis Rackets", "Yonex", "20990", "18990", 22, imgTennisRacket));
        items.add(new ItemDef("Yonex Astrel 105 Racket", "Lightweight power racket ideal for club players", "Tennis Rackets", "Yonex", "16990", null, 30, imgTennisRacket));

        // 10. TENNIS BALLS (3 items)
        items.add(new ItemDef("Cosco Championship Tennis Balls (Pack of 3)", "ITF approved pressurized tennis balls for all court surfaces", "Tennis Balls", "Cosco", "449", null, 300, imgTennisBall));
        items.add(new ItemDef("Cosco Tournament Tennis Balls (Pack of 6)", "High durability woven felt balls for match play", "Tennis Balls", "Cosco", "849", "749", 200, imgTennisBall));
        items.add(new ItemDef("Yonex Tour Championship Tennis Balls (Pack of 4)", "Premium woven felt pressurized balls", "Tennis Balls", "Yonex", "699", null, 250, imgTennisBall));

        // 11. BADMINTON RACKETS (5 items)
        items.add(new ItemDef("Yonex Astrox 100ZZ Badminton Racket", "Namd graphite head-heavy racket used by Viktor Axelsen", "Badminton Rackets", "Yonex", "18990", "16990", 30, imgBadmintonRacket));
        items.add(new ItemDef("Yonex Nanoflare 800 Pro Racket", "Head-light speed racket with Sonic Flare System", "Badminton Rackets", "Yonex", "17990", null, 25, imgBadmintonRacket));
        items.add(new ItemDef("Yonex Arcsaber 11 Pro Racket", "Even-balance control racket used by Aaron Chia", "Badminton Rackets", "Yonex", "18490", "16490", 28, imgBadmintonRacket));
        items.add(new ItemDef("Yonex Muscle Power 29 Racket", "Full graphite frame with Muscle Power frame architecture", "Badminton Rackets", "Yonex", "2990", "2590", 80, imgBadmintonRacket));
        items.add(new ItemDef("Yonex GR 303 Beginner Racket", "Aluminum frame sturdy racket with full cover", "Badminton Rackets", "Yonex", "990", null, 150, imgBadmintonRacket));

        // 12. SHUTTLECOCKS (3 items)
        items.add(new ItemDef("Yonex AS-30 Feather Shuttlecocks (Pack of 12)", "Goose feather tournament grade shuttlecocks", "Shuttlecocks", "Yonex", "2490", "2190", 100, imgShuttle));
        items.add(new ItemDef("Yonex Mavis 350 Nylon Shuttlecocks (Pack of 6)", "Precision nylon shuttlecocks with natural cork base", "Shuttlecocks", "Yonex", "1190", null, 250, imgShuttle));
        items.add(new ItemDef("Cosco Aero 77 Feather Shuttlecocks", "Duck feather shuttlecocks for club matches", "Shuttlecocks", "Cosco", "1490", "1290", 120, imgShuttle));

        // 13. RUNNING SHOES (10 items)
        items.add(new ItemDef("Nike Alphafly 3 Road Racing Shoes", "Marathon carbon plate racing shoe with Zoom Air pods", "Running Shoes", "Nike", "22795", "20495", 25, imgShoes));
        items.add(new ItemDef("Nike Pegasus 40 Running Shoes", "Versatile daily trainer with React foam & dual Zoom Air", "Running Shoes", "Nike", "11895", "9995", 50, imgShoes));
        items.add(new ItemDef("Adidas Ultraboost Light Running Shoes", "Light Boost foam shoe for maximum energy return", "Running Shoes", "Adidas", "18999", "15999", 40, imgShoes));
        items.add(new ItemDef("Adidas Adizero Adios Pro 3", "Marathon record-breaking shoe with EnergyRods 2.0", "Running Shoes", "Adidas", "21999", null, 20, imgShoes));
        items.add(new ItemDef("ASICS GEL-Nimbus 26 Running Shoes", "PureGEL technology cushioning shoe for long distance", "Running Shoes", "ASICS", "15999", "13999", 35, imgShoes));
        items.add(new ItemDef("ASICS GEL-Kayano 30 Stability Shoes", "4D GUIDANCE SYSTEM for ultimate stability and support", "Running Shoes", "ASICS", "16999", null, 30, imgShoes));
        items.add(new ItemDef("New Balance Fresh Foam X 1080v13", "Plush Fresh Foam midsole running shoe", "Running Shoes", "New Balance", "14999", "12999", 32, imgShoes));
        items.add(new ItemDef("Under Armour HOVR Phantom 3", "UA HOVR cushioning with SpeedForm 2.0 sockliner", "Running Shoes", "Under Armour", "12999", null, 28, imgShoes));
        items.add(new ItemDef("Puma Velocity Nitro 3 Running Shoes", "NITROFOAM responsive cushioning daily trainer", "Running Shoes", "Puma", "10999", "8999", 45, imgShoes));
        items.add(new ItemDef("Reebok Floatride Energy 5", "Floatride Energy Foam lightweight endurance shoe", "Running Shoes", "Reebok", "8999", "7499", 50, imgShoes));

        // 14. GYM EQUIPMENT (15 items)
        items.add(new ItemDef("Under Armour TriBase Reign 6 Gym Shoes", "TriBase ground control shoes for CrossFit and lifting", "Gym Equipment", "Under Armour", "11999", "10499", 30, imgGym));
        items.add(new ItemDef("Nike Metcon 9 Training Shoes", "Hyperlift plate shoe with rubber rope wrap for lifting", "Gym Equipment", "Nike", "12795", "11295", 35, imgGym));
        items.add(new ItemDef("Adidas Powerlift 5 Lifting Shoes", "High heel wedge weightlifting shoe with instep strap", "Gym Equipment", "Adidas", "9999", null, 25, imgGym));
        items.add(new ItemDef("Dumbbell Set 20kg Adjustable Cast Iron", "20kg chrome plated adjustable dumbbell set with case", "Gym Equipment", "Puma", "6499", "5499", 40, imgGym));
        items.add(new ItemDef("Commercial Olympic Barbell 20kg 7ft", "700lb capacity chrome Olympic bar with needle bearings", "Gym Equipment", "Under Armour", "12999", null, 20, imgGym));
        items.add(new ItemDef("Bumper Weight Plates Set 50kg", "High density rubber Olympic bumper plates (2x5kg, 2x10kg, 2x15kg)", "Gym Equipment", "Reebok", "14999", "12999", 15, imgGym));
        items.add(new ItemDef("Heavy Duty Adjustable Bench Press", "Flat, incline, decline 7-position utility workout bench", "Gym Equipment", "Under Armour", "8999", "7499", 25, imgGym));
        items.add(new ItemDef("Speed Cable Jump Rope Pro", "Aluminum handle speed rope with dual ball bearings", "Gym Equipment", "Nike", "799", "599", 150, imgGym));
        items.add(new ItemDef("Heavy Resistance Loop Bands Set of 5", "100% natural latex powerlifting loop bands", "Gym Equipment", "Adidas", "1299", "999", 120, imgGym));
        items.add(new ItemDef("Kettlebell 16kg Cast Iron", "Powder coated cast iron kettlebell with color coded ring", "Gym Equipment", "Reebok", "2999", null, 40, imgGym));
        items.add(new ItemDef("Pull Up Bar Multi-Grip Doorway", "Heavy duty doorway pull up & chin up bar", "Gym Equipment", "Puma", "1999", "1699", 80, imgGym));
        items.add(new ItemDef("Ab Roller Wheel with Knee Mat", "Dual wheel ab trainer with stainless steel axle", "Gym Equipment", "Nike", "899", null, 100, imgGym));
        items.add(new ItemDef("Foam Roller High Density Muscle Recovery", "Grid pattern EVA foam roller for deep tissue massage", "Gym Equipment", "Puma", "1199", "999", 90, imgGym));
        items.add(new ItemDef("Padded Lifting Straps Pair", "Neoprene padded heavy duty cotton weightlifting straps", "Gym Equipment", "Under Armour", "499", null, 200, imgGym));
        items.add(new ItemDef("Leather Weightlifting Belt 4 inch", "Genuine leather 4-inch wide lumbar support belt", "Gym Equipment", "Reebok", "1999", "1599", 60, imgGym));

        // Insert items safely without deleting any existing records
        for (ItemDef item : items) {
            if (productRepo.findByName(item.name).isPresent()) {
                continue;
            }

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
                .averageRating(4.5 + (Math.random() * 0.5))
                .ratingCount(15 + (int)(Math.random() * 150))
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
