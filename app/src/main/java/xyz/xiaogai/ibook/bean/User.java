package xyz.xiaogai.ibook.bean;

import java.util.Date;

public class User {
    private String id;
    private String username;
    private String password;
    private String email;
    private String telephone;
    private boolean gender;
    private String introduce;
    private String activeCode;
    private boolean state;
    private boolean role;
    private Date registTime;

    public User() {
    }

    public User(String id, String username, String password, String email, String telephone, boolean gender, String introduce, String activeCode, boolean state, boolean role, Date registTime) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.telephone = telephone;
        this.gender = gender;
        this.introduce = introduce;
        this.activeCode = activeCode;
        this.state = state;
        this.role = role;
        this.registTime = registTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getActiveCode() {
        return activeCode;
    }

    public void setActiveCode(String activeCode) {
        this.activeCode = activeCode;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public boolean isRole() {
        return role;
    }

    public void setRole(boolean role) {
        this.role = role;
    }

    public Date getRegistTime() {
        return registTime;
    }

    public void setRegistTime(Date registTime) {
        this.registTime = registTime;
    }

}




