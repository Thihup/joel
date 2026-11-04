package dev.thihup.joel.integration.beans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("cartBean")
@RequestScoped
public class FacesCartBean implements Serializable {

    public static class CartItem {
        private String name;
        private int quantity;
        private double price;

        public CartItem(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }

        public double getSubtotal() {
            return quantity * price;
        }
    }

    private String customerName = "Thiago";
    private boolean premiumMember = true;
    private int loyaltyPoints = 350;
    private String couponCode = "DISCOUNT2026";
    private List<CartItem> items = new ArrayList<>(List.of(
            new CartItem("Mechanical Keyboard", 1, 120.0),
            new CartItem("Ergonomic Mouse", 2, 45.0),
            new CartItem("USB-C Hub", 1, 35.0)
    ));

    public String getCustomerName() {
        return customerName;
    }

    public boolean isPremiumMember() {
        return premiumMember;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double getTotalAmount() {
        return items.stream().mapToDouble(CartItem::getSubtotal).sum();
    }

    public double getDiscountedTotal() {
        return premiumMember ? getTotalAmount() * 0.9 : getTotalAmount();
    }
}

