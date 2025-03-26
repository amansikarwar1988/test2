@Component
public class RecordProcessingScheduler {

    private final RecordProcessorService processorService;

    public RecordProcessingScheduler(RecordProcessorService processorService) {
        this.processorService = processorService;
    }

    @Scheduled(fixedDelay = 5000) // Adjust the polling interval
    public void scheduleRecordProcessing() {
        processorService.processNextRecord();
    }
}


@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {

    @Query(value = """
        SELECT * FROM records
        WHERE status = 'NEW'
        ORDER BY updated_at
        FOR UPDATE SKIP LOCKED
        FETCH FIRST 1 ROWS ONLY
        """, nativeQuery = true)
    Optional<Record> findNextRecord();
}

@Service
public class RecordProcessorService {

    private final RecordRepository recordRepository;
    private final ThirdPartyClient thirdPartyClient;

    public RecordProcessorService(RecordRepository recordRepository, ThirdPartyClient thirdPartyClient) {
        this.recordRepository = recordRepository;
        this.thirdPartyClient = thirdPartyClient;
    }

    @Transactional
    public void processNextRecord() {
        recordRepository.findNextRecord().ifPresent(record -> {
            try {
                // Mark record as "PROCESSING"
                record.setStatus("PROCESSING");
                recordRepository.save(record);

                // Send to third-party queue
                thirdPartyClient.sendToQueue(record);

                // Mark as "PROCESSED"
                record.setStatus("PROCESSED");
                recordRepository.save(record);
            } catch (Exception e) {
                // Reset status on failure (optional: add retry logic here)
                record.setStatus("NEW");
                recordRepository.save(record);
                throw new RuntimeException("Processing failed", e);
            }
        });
    }
}
