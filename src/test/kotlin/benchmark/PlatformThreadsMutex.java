package benchmark;

import br.ufrn.dimap.services.ExecutionService;
import br.ufrn.dimap.services.FileManagementService;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;

@State(Scope.Benchmark)
public class PlatformThreadsMutex {
    @Setup
    public void loadDataset() throws IOException, InterruptedException {
        FileManagementService.INSTANCE.importRandomData();
        FileManagementService.INSTANCE.importUnknownLocations();
    }

    @Benchmark
    @Warmup(iterations = 5)
    @Measurement(iterations = 5)
    @Fork(value = 2)
    public void execute() throws InterruptedException {
        ExecutionService.INSTANCE.runPlatformThreads(ExecutionService.INSTANCE.getInterpolationTasks());
    }
}
