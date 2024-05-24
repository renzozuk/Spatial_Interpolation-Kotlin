package benchmark;

import br.ufrn.dimap.services.ExecutionService;
import br.ufrn.dimap.services.FileManagementService;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;

@State(Scope.Benchmark)
public class Serial {
    @Setup
    public void loadDataset() throws IOException, InterruptedException {
        FileManagementService.INSTANCE.importRandomData();
        FileManagementService.INSTANCE.importUnknownLocations();
    }

    @Benchmark
    @Warmup(iterations = 2)
    @Measurement(iterations = 2)
    @Fork(value = 2, warmups = 1)
    public void execute() {
        ExecutionService.INSTANCE.runSerial(ExecutionService.INSTANCE.getInterpolationTasks());
    }
}
