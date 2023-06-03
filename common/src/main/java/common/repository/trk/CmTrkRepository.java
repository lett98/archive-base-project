package common.repository.trk;

import common.model.trk.CmTrk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

public interface CmTrkRepository extends JpaRepository<CmTrk, Long> {

    @Query(value = "select max(end_at) from cm_trk where job_type = :jobType", nativeQuery = true)
    Timestamp findMaxEndAt(@Param(value = "jobType") Integer jobType);

    @Modifying
    @Transactional
    @Query(value = "insert into cm_trk(job_id, start_at, end_at, status, job_type, job_start_at, job_end_at, picking_status, archiving_status) " +
            "values (:jobId, :startAt, :endAt, :status, :jobType, :jobStartAt, :jobEndAt, :pickingStatus, :archivingStatus)", nativeQuery = true)
    int insertNewCmJob(@Param(value = "jobId") Long jobId,
                       @Param(value = "startAt") Timestamp startAt,
                       @Param(value = "endAt") Timestamp endAt,
                       @Param(value = "status") String status,
                       @Param(value = "jobType") Integer jobType,
                       @Param(value = "jobStartAt") Timestamp jobStartAt,
                       @Param(value = "jobEndAt") Timestamp jobEndAt,
                       @Param(value = "pickingStatus") Integer pickingStatus,
                       @Param(value = "archivingStatus") Integer archivingStatus);

    CmTrk findFirstByJobId(Long jobId);

    //Insert & Delete

    @Query(value = "select min(job_id) from cm_trk where status=:status and picking_status=:pickingStatus and archiving_status=:archivingStatus and read_size > 0", nativeQuery = true)
    Long findByStatuses(@Param(value = "status") String status,
                        @Param(value = "pickingStatus") Integer pickingStatus,
                        @Param(value = "archivingStatus") Integer archivingStatus);

    @Query(value = "update cm_trk set picking_status = :pickingStatus, version = :newVersion where job_id = :jobId and version = :oldVersion", nativeQuery = true)
    @Modifying
    @Transactional
    int updateCmTrkWithVersion(@Param(value = "pickingStatus") Integer pickingStatus,
                                   @Param(value = "newVersion") Integer newVersion,
                                   @Param(value = "jobId") Long jobId,
                                   @Param(value = "oldVersion") Integer oldVersion);

    @Query(value = "update cm_trk set picking_status = :pickingStatus, archiving_status = :archivingStatus where job_id = :jobId", nativeQuery = true)
    @Modifying
    @Transactional
    int updateCmTrk(@Param(value = "pickingStatus") Integer pickingStatus,
                        @Param(value = "archivingStatus") Integer archivingStatus,
                        @Param(value = "jobId") Long jobId);

    @Query(value = "select * from cm_trk where picking_status = :pickingStatus and archiving_status = :archivingStatus and job_id = :jobId", nativeQuery = true)
    CmTrk findById(@Param(value = "pickingStatus") Integer pickingStatus,
                       @Param(value = "archivingStatus") Integer archivingStatus,
                       @Param(value = "jobId") Long jobId);

    @Query(value = "select * from cm_trk where picking_status = :pickingStatus and archiving_status = :archivingStatus and job_id = :jobId and version = :version", nativeQuery = true)
    CmTrk findByIdAndVersion(@Param(value = "pickingStatus") Integer pickingStatus,
                                 @Param(value = "archivingStatus") Integer archivingStatus,
                                 @Param(value = "jobId") Long jobId,
                                 @Param(value = "version") Integer version);

    @Query(value = "update cm_trk set read_size= :readSize, write_size= :writeSize, status= :status, job_end_at= :jobEndAt, archiving_status= :archivingStatus " +
            "where job_id = :jobId", nativeQuery = true)
    @Modifying
    @Transactional
    int updateNewSuccessCmJob(@Param(value = "readSize") Long readSize,
                              @Param(value = "writeSize") Long writeSize,
                              @Param(value = "status") String status,
                              @Param(value = "jobEndAt") Timestamp jobEndAt,
                              @Param(value = "archivingStatus") Integer archivingStatus,
                              @Param(value = "jobId") Long jobId);
}
