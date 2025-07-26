package com.hypo.mochikabu.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_index;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String company_code;
    private String firstname;
    private String lastname;
    private String auth_email;
    private String kaiin_code;
    private String id;

}
