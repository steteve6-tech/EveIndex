package com.certification.repository.common;

import com.certification.entity.common.DevicePmaRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DevicePmaRecordRepository extends JpaRepository<DevicePmaRecord, Long> {

    Optional<DevicePmaRecord> findByPmaNumber(String pmaNumber);

    Optional<DevicePmaRecord> findByPmaNumberAndSupplementNumber(String pmaNumber, String supplementNumber);

    List<DevicePmaRecord> findByProductCode(String productCode);

    List<DevicePmaRecord> findByApplicantContaining(String applicant);

    List<DevicePmaRecord> findByTradeNameContaining(String tradeName);

    List<DevicePmaRecord> findByDecisionDate(LocalDate decisionDate);

    List<DevicePmaRecord> findByDateReceived(LocalDate dateReceived);

    @Query("select d from DevicePmaRecord d where d.dateReceived >= :start and d.dateReceived <= :end")
    List<DevicePmaRecord> findByDateReceivedBetween(@Param("start") LocalDate start,
                                                    @Param("end") LocalDate end);

    List<DevicePmaRecord> findByDataSource(String dataSource);

    List<DevicePmaRecord> findByJdCountry(String jdCountry);

    List<DevicePmaRecord> findByDataSourceAndJdCountry(String dataSource, String jdCountry);
}
