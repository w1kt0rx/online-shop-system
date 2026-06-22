package order.repository.impl.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OrderSnapshotFileStore {

    private final File file;
    private final ObjectMapper mapper;

    public OrderSnapshotFileStore(File file, ObjectMapper mapper) {
        this.file = file;
        this.mapper = mapper;
    }

    public List<OrderSnapshot> load() {
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }

        try {
            return mapper.readValue(file, new TypeReference<List<OrderSnapshot>>() {});
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not load order snapshots from file: " + file.getPath(), e
            );
        }
    }

    public void save(List<OrderSnapshot> snapshots) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, snapshots);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not persist order snapshots to file: " + file.getPath(), e
            );
        }
    }
}