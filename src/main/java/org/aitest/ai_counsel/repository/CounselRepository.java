package org.aitest.ai_counsel.repository;

import org.aitest.ai_counsel.domain.Counsel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CounselRepository extends JpaRepository<Counsel, Long> {
    List<Counsel> findByCounselorIdOrderByCounselDateDesc(String counselorId);
    List<Counsel> findByCustomerIdOrderByCounselDateDesc(String customerId);
    List<Counsel> findByCounselDateBetween(LocalDateTime start, LocalDateTime end);
    List<Counsel> findByProductInfoContaining(String productInfo);
}
