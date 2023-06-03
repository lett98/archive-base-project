package cm.job.step;

import common.model.trk.EntityTrk;
import common.repository.trk.EntityTrkRepository;
import lombok.Setter;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@Setter
public class CMWriter implements ItemWriter<EntityTrk> {
    private EntityTrkRepository entityTrkRepository;

    @Override
    public void write(List<? extends EntityTrk> items) throws Exception {
        entityTrkRepository.saveAll(items);
    }
}
