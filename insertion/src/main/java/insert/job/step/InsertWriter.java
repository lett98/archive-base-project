package insert.job.step;

import common.model.trk.EntityTrk;
import insert.service.InsertArcService;
import insert.service.InsertService;
import insert.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;

import java.util.List;


@Setter
@Slf4j
@RequiredArgsConstructor
public class InsertWriter implements ItemWriter<EntityTrk> {
    private InsertService insertService;
    private TrackingService trackingService;
    private InsertArcService insertArcService;
    private Long insertedJobId;
    private String jobName;

    //TODO
    @Override
    public void write(List<? extends EntityTrk> items) throws Exception {

    }
}
