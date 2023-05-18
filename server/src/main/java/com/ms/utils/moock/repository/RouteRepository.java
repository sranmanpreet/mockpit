package com.ms.utils.moock.repository;

import com.ms.utils.moock.domain.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    @Query("DELETE FROM Route r WHERE r.mock = :mockId")
    void deleteByMockId(@Param("mockId") Long mockId);
}

