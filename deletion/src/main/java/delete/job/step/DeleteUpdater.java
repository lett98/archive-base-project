package delete.job.step;

import common.model.tracing.ModelTracing;
import common.model.trk.EntityTrk;
import delete.service.DeleteService;
import delete.service.TrackingService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
import java.util.Vector;

@Setter
@Slf4j
public class DeleteUpdater implements ItemWriter<EntityTrk> {
    private DeleteService deleteService;
    private TrackingService trackingService;
    private Long deleteJobId;
    private Vector<ModelTracing> modelTracings;
    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();
        modelTracings = (Vector<ModelTracing>) jobExecutionContext.get("modelTracing");
    }

    @Override
    public void write(List<? extends EntityTrk> items) throws Exception {
       //TODO
    }
}
