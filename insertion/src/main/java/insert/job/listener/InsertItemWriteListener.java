package insert.job.listener;

import common.model.trk.EntityTrk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemWriteListener;

import java.util.List;


@Slf4j
public class InsertItemWriteListener implements ItemWriteListener<EntityTrk> {

    @Override
    public void beforeWrite(List<? extends EntityTrk> items) {
    }

    @Override
    public void afterWrite(List<? extends EntityTrk> items) {
        log.info(">>> Inserting completed. Size=" + items.size());
    }

    @Override
    public void onWriteError(Exception exception, List<? extends EntityTrk> items) {

    }
}
