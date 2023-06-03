package common.repository.trk;

import common.model.TrackingStatus;
import common.model.trk.EntityTrk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

public interface EntityTrkRepository extends JpaRepository<EntityTrk, Integer> {
    List<EntityTrk> findAllByCmJobIdAndStatus(Long cmJobId, TrackingStatus status);

    @Modifying
    @Transactional
    int updateInsertEntityTrk(@Param(value = "updatedAt") Timestamp updatedAt,
                            @Param(value = "status") Integer status,
                            @Param(value = "insertedJobId") Long insertedJobId,
                            @Param(value = "billIds") List<Integer> billIds);

    @Modifying
    @Transactional
    int updateDeleteEntityTrk(@Param(value = "updatedAt") Timestamp updatedAt,
                            @Param(value = "status") Integer status,
                            @Param(value = "deletedJobId") Long deletedJobId,
                            @Param(value = "billIds") List<Integer> billIds);

}
