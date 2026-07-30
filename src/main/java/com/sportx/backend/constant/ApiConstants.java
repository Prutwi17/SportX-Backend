package com.sportx.backend.constant;

public final class ApiConstants {

    private ApiConstants() {}

    public static final String BASE_URL = "/api/v1";

    public static final String AUTH = BASE_URL + "/auth";
    public static final String USERS = BASE_URL + "/users";
    public static final String ADDRESSES = BASE_URL + "/addresses";
    public static final String CATEGORIES = BASE_URL + "/categories";
    public static final String BRANDS = BASE_URL + "/brands";
    public static final String PRODUCTS = BASE_URL + "/products";
    public static final String CART = BASE_URL + "/cart";
    public static final String WISHLIST = BASE_URL + "/wishlist";
    public static final String ORDERS = BASE_URL + "/orders";
    public static final String PAYMENTS = BASE_URL + "/payments";
    public static final String REVIEWS = BASE_URL + "/reviews";
    public static final String COUPONS = BASE_URL + "/coupons";
    public static final String INVENTORY = BASE_URL + "/inventory";
    public static final String NOTIFICATIONS = BASE_URL + "/notifications";
    public static final String DASHBOARD = BASE_URL + "/dashboard";
    public static final String REPORTS = BASE_URL + "/reports";

    public static final String PAGE_DEFAULT = "0";
    public static final String SIZE_DEFAULT = "10";
    public static final String SORT_DEFAULT = "id";
}
