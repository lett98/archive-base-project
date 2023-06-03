package delete.job.listener;

import org.springframework.batch.core.listener.ChunkListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;

public class DeleteChunkListener extends ChunkListenerSupport {
    @Override
    public void afterChunk(ChunkContext context) {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        super.afterChunk(context);
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        super.afterChunkError(context);
    }
}
