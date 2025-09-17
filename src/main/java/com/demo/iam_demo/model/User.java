package com.demo.iam_demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints =
        {@UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
        })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    private String email;

    @NotBlank
    @Size(max = 120)
    private String password;

    @NotBlank
    @Past
    private LocalDate birthDate;

    @NotBlank
    private String address;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{10,11}$")
    private String phone;

    @NotBlank
    private String avatar;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public User(){}

    public User(String username, String email, String password, LocalDate birthDate, String address, String phone, String avatar){
        this.username = username;
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
        this.address = address;
        this.phone = phone;
        this.avatar = avatar;
    }

    public Long getId(){return id;}
    public void setId(Long id){this.id = id;}
    public String getUsername(){return username;}
    public void setUsername(String username){this.username = username;}
    public String getEmail(){return email;}
    public void setEmail(String email){this.email = email;}
    public String getPassword(){return password;}
    public void setPassword(String password){this.password = password;}
    public LocalDate getBirthDate(){return birthDate;}
    public void setBirthDate(LocalDate birthDate){this.birthDate = birthDate;}
    public String getAddress(){return address;}
    public void setAddress(String address){this.address = address;}
    public String getPhone(){return phone;}
    public void setPhone(String phone){this.phone = phone;}
    public String getAvatar(){return avatar;}
    public void setAvatar(String avatar){this.avatar = avatar;}
    public Set<Role> getRoles(){return roles;}
    public void setRoles(Set<Role> roles){this.roles = roles;}
}
