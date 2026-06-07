package order.repository.file;

import cart.model.CartItem;
import order.model.Order;
import order.model.OrderStatus;
import order.repository.impl.file.FileOrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.model.electronics.Electronics;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class FileOrderRepositoryTest {

    private static final String TEST_FILE = "test-orders.json";
    private FileOrderRepository repository;

    @BeforeEach
    void setUp() {
        new File(TEST_FILE).delete(); // fresh start każdy test
        repository = new FileOrderRepository(TEST_FILE);
    }

    @AfterEach
    void tearDown() {
        new File(TEST_FILE).delete();
    }

    private Order makeOrder(Long id) {
        Electronics product = new Electronics(1L, "Monitor", new BigDecimal("800"), 10);
        CartItem item = new CartItem(product, 1);
        return new Order(id, 1L, List.of(item));
    }

    // ── Runtime cache (bieżąca sesja) ────────────────────────────────

    @Test
    void shouldSaveAndFindOrderInCurrentSession() {
        Order order = makeOrder(1L);
        repository.save(order);

        Optional<Order> result = repository.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void shouldReturnAllOrdersFromCurrentSession() {
        repository.save(makeOrder(1L));
        repository.save(makeOrder(2L));

        assertThat(repository.getAll()).hasSize(2);
    }

    @Test
    void shouldDeleteOrderFromCurrentSession() {
        repository.save(makeOrder(1L));
        repository.delete(1L);

        assertThat(repository.findById(1L)).isEmpty();
        assertThat(repository.getAll()).isEmpty();
    }

    @Test
    void shouldGenerateSequentialIds() {
        assertThat(repository.getNextId()).isEqualTo(1L);
        assertThat(repository.getNextId()).isEqualTo(2L);
        assertThat(repository.getNextId()).isEqualTo(3L);
    }

    // ── Persystencja do pliku ─────────────────────────────────────────

    @Test
    void shouldCreateFileOnFirstSave() {
        repository.save(makeOrder(1L));

        assertThat(new File(TEST_FILE)).exists();
    }

    @Test
    void shouldPersistSnapshotsToFile() {
        repository.save(makeOrder(1L));
        repository.save(makeOrder(2L));

        // Nowe repo — ładuje snapshoty z pliku
        FileOrderRepository reloaded = new FileOrderRepository(TEST_FILE);

        assertThat(reloaded.getPersistedSnapshots()).hasSize(2);
        assertThat(reloaded.getPersistedCount()).isEqualTo(2);
    }

    @Test
    void shouldRemoveSnapshotFromFileOnDelete() {
        repository.save(makeOrder(1L));
        repository.save(makeOrder(2L));
        repository.delete(1L); // usuwa z cache i z pliku

        FileOrderRepository reloaded = new FileOrderRepository(TEST_FILE);

        assertThat(reloaded.getPersistedSnapshots()).hasSize(1);
        assertThat(reloaded.getPersistedSnapshots().get(0).id()).isEqualTo(2L);
    }

    @Test
    void shouldResumeIdSequenceAfterReload() {
        repository.save(makeOrder(repository.getNextId())); // id=1
        repository.save(makeOrder(repository.getNextId())); // id=2

        FileOrderRepository reloaded = new FileOrderRepository(TEST_FILE);

        // Po reloadzie kolejne ID to 3, nie 1
        assertThat(reloaded.getNextId()).isEqualTo(3L);
    }

    @Test
    void shouldWorkCorrectlyOnEmptyFileAtStart() {
        assertThat(repository.getAll()).isEmpty();
        assertThat(repository.getPersistedSnapshots()).isEmpty();
        assertThat(repository.findById(1L)).isEmpty();
    }

    @Test
    void shouldPersistCorrectSnapshotFields() {
        Order order = makeOrder(1L);
        order.confirm();
        repository.save(order);

        FileOrderRepository reloaded = new FileOrderRepository(TEST_FILE);
        var snapshot = reloaded.getPersistedSnapshots().get(0);

        assertThat(snapshot.id()).isEqualTo(1L);
        assertThat(snapshot.customerId()).isEqualTo(1L);
        assertThat(snapshot.totalPrice()).isEqualByComparingTo(new BigDecimal("800"));
        assertThat(snapshot.status()).isEqualTo(OrderStatus.CONFIRMED.name());
        assertThat(snapshot.createdAt()).isNotNull();
        assertThat(snapshot.confirmedAt()).isNotNull();
    }

    @Test
    void shouldAccumulateSnapshotsAcrossSessions() {
        // Sesja 1: zapisz 2 zamówienia
        repository.save(makeOrder(repository.getNextId())); // id=1
        repository.save(makeOrder(repository.getNextId())); // id=2

        // Sesja 2: dodaj jeszcze jedno
        FileOrderRepository session2 = new FileOrderRepository(TEST_FILE);
        session2.save(makeOrder(session2.getNextId())); // id=3

        // Sesja 3: powinny być widoczne wszystkie 3 snapshoty
        FileOrderRepository session3 = new FileOrderRepository(TEST_FILE);
        assertThat(session3.getPersistedSnapshots()).hasSize(3);
    }

    @Test
    void shouldNotDuplicateSnapshotOnMultipleSaves() {
        Order order = makeOrder(1L);
        repository.save(order); // zapis 1
        repository.save(order); // zapis 2 tego samego

        FileOrderRepository reloaded = new FileOrderRepository(TEST_FILE);
        assertThat(reloaded.getPersistedSnapshots()).hasSize(1);
    }
}
