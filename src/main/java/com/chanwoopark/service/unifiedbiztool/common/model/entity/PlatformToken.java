package com.chanwoopark.service.unifiedbiztool.common.model.entity;

import com.chanwoopark.service.unifiedbiztool.common.model.enums.Platform;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "platform_token", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"platform"})
})
public class PlatformToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Setter
    @Column(nullable = false, length = 1024)
    private String accessToken;
}
