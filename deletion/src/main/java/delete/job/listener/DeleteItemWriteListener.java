package delete.job.listener;

import common.model.trk.EntityTrk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemWriteListener;

import java.util.List;


@Slf4j
public class DeleteItemWriteListener implements ItemWriteListener<EntityTrk> {

    @Override
    public void beforeWrite(List<? extends EntityTrk> list) {

    }

    @Override
    public void afterWrite(List<? extends EntityTrk> items) {
        log.info(">>> Deleting completed. Size=" + items.size());
    }

    @Override
    public void onWriteError(Exception exception, List<? extends EntityTrk> items) {

    }
}
