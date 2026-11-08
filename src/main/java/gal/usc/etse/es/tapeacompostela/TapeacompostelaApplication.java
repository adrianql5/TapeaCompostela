package gal.usc.etse.es.tapeacompostela;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync // Activa @Async (lo usa PointsEventListener)
@EnableScheduling // Activa @Scheduled (lo usa RedemptionExpiryJob)
@SpringBootApplication
public class TapeacompostelaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TapeacompostelaApplication.class, args);
	}

}
