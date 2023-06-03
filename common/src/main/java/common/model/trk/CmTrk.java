package common.model.trk;

import common.model.PickingStatus;
import common.model.TrackingStatus;
import common.model.converter.PickingStatusConverter;
import common.model.converter.TrackingStatusConverter;
import lombok.Data;
import lombok.Getter;

import javax.persistence.*;
import java.sql.Timestamp;

@Table
@Entity
@Data
@Getter
public class CmTrk {
    @Id
    @Column(name="job_id")
    private Long jobId;

    @Column(name="start_at")
    private Timestamp startAt;

    @Column(name="end_at")
    private Timestamp endAt;

    @Column(name="read_size")
    private Integer readSize;

    @Column(name="write_size")
    private Integer writeSize;

    @Column(name="status")
    private String status;

    @Column(name="job_type")
    private Integer jobType;

    @Column(name="job_start_at")
    private Timestamp jobStartAt;

    @Column(name="job_end_at")
    private Timestamp jobEndAt;

    @Convert(converter = PickingStatusConverter.class)
    @Column(name="picking_status")
    private PickingStatus pickingStatus;

    @Convert(converter = TrackingStatusConverter.class)
    @Column(name="archiving_status")
    private TrackingStatus archivingStatus;

    @Column(name="version")
    private Integer version;
}
