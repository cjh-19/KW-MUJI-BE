package com.muji_backend.kw_muji.mypage.repository;

import com.muji_backend.kw_muji.common.entity.ResumeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<ResumeEntity, Long> {
}
