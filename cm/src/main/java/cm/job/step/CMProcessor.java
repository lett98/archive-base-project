package cm.job.step;

import cm.model.DataInput;
import common.model.TrackingStatus;
import common.model.trk.EntityTrk;
import lombok.AllArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

import java.sql.Timestamp;
import java.util.Date;

@AllArgsConstructor
public class CMProcessor implements ItemProcessor<DataInput, EntityTrk> {
    private Long cmJobId;

    @Override
    public EntityTrk process(DataInput item) throws Exception {
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        EntityTrk entityTrk = new EntityTrk();
        entityTrk.setCmJobId(cmJobId);
        entityTrk.setInsertedAt(timestamp);
        entityTrk.setStatus(TrackingStatus.PENDING);
        //TODO: Thêm Key
        return entityTrk;
    }

}
