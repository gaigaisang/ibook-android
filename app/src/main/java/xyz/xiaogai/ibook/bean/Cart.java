package xyz.xiaogai.ibook.bean;

import java.util.List;
import java.util.Objects;

public class Cart {
    private User user; //userid
    private List<CartItem> cartitems;
    private double price;

    public Cart() {
    }
    public Cart(User user, List<CartItem> books, double price) {
        this.user = user;
        this.cartitems = books;
        this.price = price;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<CartItem> getCartitems() {
        return cartitems;
    }

    public void setCartitems(List<CartItem> cartitems) {
        this.cartitems = cartitems;
    }

    public double getPrice() {

        for (CartItem item : cartitems) {
            price += item.getPrice();
        }
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "user=" + user +
                ", books=" + cartitems +
                ", price=" + getPrice() +
                '}';
    }

}
