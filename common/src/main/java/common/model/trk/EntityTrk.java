package common.model.trk;

import common.model.TrackingStatus;
import common.model.converter.TrackingStatusConverter;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table
@Data
public class EntityTrk {
    @Column(name = "inserted_at")
    private Timestamp insertedAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Convert(converter = TrackingStatusConverter.class)
    @Column(name = "status")
    private TrackingStatus status;

    @Column(name = "cm_job_id")
    private Long cmJobId;

    @Column(name = "inserted_job_id")
    private Long insertedJobId;

    @Column(name = "deleted_job_id")
    private Long deletedJobId;
}
