package insert.job.listener;

import common.model.PickingStatus;
import common.model.TrackingStatus;
import common.model.trk.CmTrk;
import common.model.trk.EntityTrk;
import common.repository.trk.EntityTrkRepository;
import common.service.CMService;
import common.util.PartitionUtil;
import common.vo.JobEmptyException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@Setter
@Slf4j
public class InsertPartitionerAndJobListener extends JobExecutionListenerSupport implements Partitioner {
    private Long jobCMId;
    private PickingStatus pickingStatus;
    private TrackingStatus trackingStatus;
    private TrackingStatus beforeTrackingStatus;
    private CMService cmService;
    private EntityTrkRepository entityTrkRepository;
    private void resetAll() {
        this.jobCMId = null;
        this.pickingStatus = null;
        this.trackingStatus = null;
        this.beforeTrackingStatus = null;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        resetAll();
        try {
            jobCMId = jobExecution.getJobParameters().getLong("JobCMId");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info(">>> Just Picked a Job: " + jobCMId);
        super.beforeJob(jobExecution);
    }


    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        List<EntityTrk> entityTrks = entityTrkRepository.findAllByCmJobIdAndStatus(jobCMId, TrackingStatus.PENDING);
        int partitionSize = entityTrks.size() / gridSize + 1;
        Map<String, ExecutionContext> result = PartitionUtil.partitioningEntityList(entityTrks, partitionSize);
        return result;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info(">>> AfterInsertionJob: JobID: " + jobExecution.getJobId() + " -Status: " + jobExecution.getExitStatus().getExitCode());
        pickingStatus = PickingStatus.PENDING;
        beforeTrackingStatus = TrackingStatus.PENDING;
        trackingStatus = TrackingStatus.INSERTED;
        // whole job execution
        List<Throwable> exceptions = jobExecution.getAllFailureExceptions();
        if (!exceptions.isEmpty()) {
            pickingStatus = PickingStatus.FAILURE;
            trackingStatus = beforeTrackingStatus;
            if (exceptions.get(0).getCause() instanceof JobEmptyException) {
                return;
            }
        }

        CmTrk cmTrkReleased = cmService.release(jobCMId,pickingStatus,trackingStatus);
        log.info(">>> AfterInsertionJob: Job is completed. Released: " + cmTrkReleased);
        super.afterJob(jobExecution);
    }
}
