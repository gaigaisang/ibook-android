package xyz.xiaogai.ibook.bean;

public class Book {
    private String id;
    private String name;
    private String author;
    private double price;
    private String image;
    private String description;
    private String category_name;
    private int num;

    public Book() {
    }

    public Book(String id, String name, String author, double price, String image, String description, String category_name, int num) {
        super();
        this.id = id;
        this.name = name;
        this.author = author;
        this.price = price;
        this.image = image;
        this.description = description;
        this.category_name = category_name;
        this.num = num;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", price=" + price +
                ", image='" + image + '\'' +
                ", description='" + description + '\'' +
                ", category_id='" + category_name + '\'' +
                ", num=" + num +
                '}';
    }

}
