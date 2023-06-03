package common.repository.trk;

import common.model.trk.ArcInsertTrk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ArcInsertTrkRepository extends JpaRepository<ArcInsertTrk, Long> {

    @Modifying
    @Transactional
    @Query(value = "insert into archive_insert_trk(cm_job_id, insert_job_id, error_type, error_log, key_list, job_name) " +
            "VALUES (:cmJobId, :insertJobId, :errorType, :errorLog, :keyList, :jobName)", nativeQuery = true)
    int insertArcInsertTrk( @Param(value = "cmJobId") Long cmJobId,
                            @Param(value = "insertJobId") Long insertJobId,
                            @Param(value = "errorType") String errorType,
                            @Param(value = "errorLog") String errorLog,
                            @Param(value = "keyList") String keyList,
                            @Param(value = "jobName") String jobName);
}
