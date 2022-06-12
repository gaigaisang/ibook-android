package xyz.xiaogai.ibook.bean;

public class CartItem {
    private Book book;
    private int num;
    private double price;

    public CartItem() {
        super();
    }

    public CartItem(Book book, int num, double price) {
        super();
        this.book = book;
        this.num = num;
        this.price = price;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public double getPrice() {
        return book.getPrice()*num;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "book=" + book +
                ", num=" + num +
                ", price=" + getPrice() +
                '}';
    }
}
