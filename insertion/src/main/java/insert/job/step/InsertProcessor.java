package insert.job.step;

import common.model.TrackingStatus;
import common.model.trk.EntityTrk;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

import java.sql.Timestamp;
import java.util.Date;

@RequiredArgsConstructor
public class InsertProcessor implements ItemProcessor<EntityTrk, EntityTrk> {
    @Override
    public EntityTrk process(EntityTrk item) throws Exception {
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        item.setStatus(TrackingStatus.INSERTED);
        item.setUpdatedAt(timestamp);
        return item;
    }
}
