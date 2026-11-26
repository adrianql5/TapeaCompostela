package gal.usc.etse.es.tapeacompostela;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync // Activa @Async (lo usan PointsEventListener y SummaryService)
@EnableScheduling // Activa @Scheduled (lo usa RedemptionExpiryJob)
@EnableCaching // Activa @Cacheable y @CacheEvict (los usa TagService)
@EnableResilientMethods // Activa @Retryable y @ConcurrencyLimit (PointsEventListener usa @Retryable)
@SpringBootApplication
public class TapeacompostelaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TapeacompostelaApplication.class, args);
	}

}
