package com.lifemanager.life_manager.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password; // OAuth 사용자는 null

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String profileImageUrl;

    @Column(length = 50)
    private String provider = "local"; // 'local', 'google', 'kakao'

    @Column(length = 255)
    private String providerId; // OAuth 제공자의 사용자 ID

}
