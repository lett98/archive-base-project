package common.model.trk;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="archive_insert_trk")
@Getter
@Setter
public class ArcInsertTrk {
    @Id
    @Column(name="id")
    private Integer id;

    @Column(name="insert_job_id")
    private Long insertJobId;

    @Column(name="cm_job_id")
    private Long cmJobId;

    @Column(name="key_list")
    private String keyList;

    @Column(name="error_type")
    private String errorType;

    @Column(name="error_log")
    private String errorLog;

    @Column(name="status")
    private Short status;

    @Column(name="job_name")
    private String jobName;
}
