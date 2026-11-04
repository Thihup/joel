package dev.thihup.joel.integration.beans;

import java.util.ArrayList;
import java.util.List;

public class IntegrationBeans {

    public static class AddressBean {
        private String city;
        private String country;

        public AddressBean(String city, String country) {
            this.city = city;
            this.country = country;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }

    public static class UserBean {
        private String id;
        private String name;
        private String role;
        private boolean active;
        private int points;
        private AddressBean address;
        private List<String> roles = new ArrayList<>();

        public UserBean(String id, String name, String role, boolean active, int points, AddressBean address) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.active = active;
            this.points = points;
            this.address = address;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public AddressBean getAddress() {
            return address;
        }

        public void setAddress(AddressBean address) {
            this.address = address;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        public String sayHello(String recipient) {
            return "Hello, " + recipient + "! From " + name;
        }
    }

    public static class OrderBean {
        private String orderNumber;
        private UserBean customer;
        private final List<String> items = new ArrayList<>();
        private boolean submitted = false;

        public OrderBean(String orderNumber, UserBean customer) {
            this.orderNumber = orderNumber;
            this.customer = customer;
        }

        public String getOrderNumber() {
            return orderNumber;
        }

        public UserBean getCustomer() {
            return customer;
        }

        public List<String> getItems() {
            return items;
        }

        public boolean isSubmitted() {
            return submitted;
        }

        public String submitOrder() {
            this.submitted = true;
            return "order-confirmed";
        }

        public int addItem(String itemName, int quantity) {
            for (int i = 0; i < quantity; i++) {
                items.add(itemName);
            }
            return items.size();
        }
    }

    public static class JspFunctions {
        public static String toUpperCase(String input) {
            return input != null ? input.toUpperCase() : null;
        }

        public static String join(String a, String b) {
            return a + "-" + b;
        }

        public static int add(int a, int b) {
            return a + b;
        }
    }
}
