package common.util;

import common.model.trk.EntityTrk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class PartitionUtil {
    public static Map<String, ExecutionContext> partitioningEntityList(List<EntityTrk> entityTrkList, int partitionSize) {
        Map<String, ExecutionContext> result = new HashMap<>();
        int partitionNumber = 0;
        int min = 0;
        int start = min;
        int end = start + partitionSize - 1;
        int max = entityTrkList.size() - 1;
        while (start <= max) {
            ExecutionContext value = new ExecutionContext();
            result.put("partition" + partitionNumber, value);

            if(end >= max) {
                end = max;
            }
            value.put("entityTrk", entityTrkList.subList(start, end + 1));
            log.info(">>>> Partition: " +  partitionNumber + "- Start: " + start + " - End: " + end);
            start += partitionSize;
            end += partitionSize;

            partitionNumber++;
        }
        return result;
    }
}
