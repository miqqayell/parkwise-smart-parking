package be.kdg.parkwise.config;

import be.kdg.parkwise.domain.ParkingArea;
import be.kdg.parkwise.domain.ParkingSpot;
import be.kdg.parkwise.repository.ParkingAreaRepo;
import be.kdg.parkwise.repository.ParkingSpotRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppRunner {

    @Bean
    CommandLineRunner seedDatabase(ParkingAreaRepo areaRepo, ParkingSpotRepo spotRepo) {
        return args -> {
            // Only seed when there are no spots yet
            if (spotRepo.count() > 0) {
                return;
            }

            // Campus areas
            ParkingArea groenplaats = new ParkingArea("Campus Groenplaats", 51.2180, 4.4009);
            groenplaats.setAreaPostcode("2000");
            groenplaats.setAddress("Groenplaats, Antwerpen");
            groenplaats.setTotalCapacity(50);
            groenplaats.setAreaType("CAMPUS");

            ParkingArea zuid = new ParkingArea("Campus Zuid (Brusselstraat)", 51.2030, 4.3900);
            zuid.setAreaPostcode("2000");
            zuid.setAddress("Brusselstraat, Antwerpen");
            zuid.setTotalCapacity(80);
            zuid.setAreaType("CAMPUS");

            ParkingArea hoboken = new ParkingArea("Campus Hoboken (Salesianenlaan)", 51.1740, 4.3700);
            hoboken.setAreaPostcode("2660");
            hoboken.setAddress("Salesianenlaan, Hoboken");
            hoboken.setTotalCapacity(60);
            hoboken.setAreaType("CAMPUS");

            ParkingArea sintLucas = new ParkingArea("Campus Sint Lucas Antwerpen (Van Schoonbekestraat)", 51.2040, 4.4050);
            sintLucas.setAreaPostcode("2018");
            sintLucas.setAddress("Van Schoonbekestraat, Antwerpen");
            sintLucas.setTotalCapacity(40);
            sintLucas.setAreaType("CAMPUS");

            ParkingArea pothoek = new ParkingArea("Campus Pothoek (Pothoekstraat)", 51.2260, 4.4360);
            pothoek.setAreaPostcode("2060");
            pothoek.setAddress("Pothoekstraat, Antwerpen");
            pothoek.setTotalCapacity(70);
            pothoek.setAreaType("CAMPUS");

            ParkingArea stadswag = new ParkingArea("Campus Stadswag (Predikerinnenstraat)", 51.2250, 4.4030);
            stadswag.setAreaPostcode("2000");
            stadswag.setAddress("Predikerinnenstraat, Antwerpen");
            stadswag.setTotalCapacity(30);
            stadswag.setAreaType("CAMPUS");

            // Persist areas first
            groenplaats = areaRepo.save(groenplaats);
            zuid = areaRepo.save(zuid);
            hoboken = areaRepo.save(hoboken);
            sintLucas = areaRepo.save(sintLucas);
            pothoek = areaRepo.save(pothoek);
            stadswag = areaRepo.save(stadswag);

            // One spot per area (spot_number = 1)
            spotRepo.save(createSpotForArea(groenplaats, 1));
            spotRepo.save(createSpotForArea(zuid, 1));
            spotRepo.save(createSpotForArea(hoboken, 1));
            spotRepo.save(createSpotForArea(sintLucas, 1));
            spotRepo.save(createSpotForArea(pothoek, 1));
            spotRepo.save(createSpotForArea(stadswag, 1));
        };
    }

    private ParkingSpot createSpotForArea(ParkingArea area, int spotNumber) {
        ParkingSpot spot = new ParkingSpot();
        spot.setArea(area);
        spot.setSpotNumber(spotNumber);
        spot.setOperating(true);
        return spot;
    }
}
