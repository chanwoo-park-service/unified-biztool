package com.chanwoopark.service.unifiedbiztool.common.repository;

import com.chanwoopark.service.unifiedbiztool.common.model.entity.PlatformToken;
import com.chanwoopark.service.unifiedbiztool.common.model.enums.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformTokenRepository extends JpaRepository<PlatformToken, Long> {

    Optional<PlatformToken> findByPlatform(Platform platform);

}
