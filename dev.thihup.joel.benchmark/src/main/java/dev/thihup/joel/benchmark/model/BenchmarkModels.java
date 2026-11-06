package dev.thihup.joel.benchmark.model;

import java.util.List;

public class BenchmarkModels {

    public static class Country {
        private String name;
        private String code;

        public Country(String name, String code) {
            this.name = name;
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    public static class Address {
        private String street;
        private String city;
        private String zipCode;
        private Country country;

        public Address(String street, String city, String zipCode, Country country) {
            this.street = street;
            this.city = city;
            this.zipCode = zipCode;
            this.country = country;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }

        public Country getCountry() {
            return country;
        }

        public void setCountry(Country country) {
            this.country = country;
        }
    }

    public static class Person {
        private String name;
        private int age;
        private boolean active;
        private Address address;
        private List<String> tags;

        public Person(String name, int age, boolean active, Address address, List<String> tags) {
            this.name = name;
            this.age = age;
            this.active = active;
            this.address = address;
            this.tags = tags;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }
    }

    public static class OrderItem {
        private String name;
        private double price;
        private int quantity;

        public OrderItem(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    public static class Order {
        private long id;
        private Person customer;
        private List<OrderItem> items;
        private double discount;

        public Order(long id, Person customer, List<OrderItem> items, double discount) {
            this.id = id;
            this.customer = customer;
            this.items = items;
            this.discount = discount;
        }

        public long getId() {
            return id;
        }

        public Person getCustomer() {
            return customer;
        }

        public List<OrderItem> getItems() {
            return items;
        }

        public double getDiscount() {
            return discount;
        }
    }

    public static class Calculator {
        public String ping() {
            return "pong";
        }

        public int increment(int value) {
            return value + 1;
        }

        public int add(int a, int b) {
            return a + b;
        }

        public double multiply(double a, double b) {
            return a * b;
        }

        public String compute(long x) {
            return "long:" + x;
        }

        public String compute(double x) {
            return "double:" + x;
        }

        public String compute(String x) {
            return "str:" + x;
        }

        public static int staticMax(int a, int b) {
            return Math.max(a, b);
        }

        public int sumAll(int a, int b, int c, int d) {
            return a + b + c + d;
        }
    }

    public value record PersonRecord(String name, int age, boolean active) {}

    public static class BenchmarkFunctions {
        public static String formatGreeting(String name) {
            return "Hello, " + name;
        }
    }
}
