package common.repository.tracing;

import common.model.tracing.ModelTracing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelTracingRepository extends JpaRepository<ModelTracing, Long> {
}
