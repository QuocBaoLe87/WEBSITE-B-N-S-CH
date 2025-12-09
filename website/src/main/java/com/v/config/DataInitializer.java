package com.v.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.v.model.Book;
import com.v.model.BookImage;
import com.v.repository.BookRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BookRepository bookRepository;

    public DataInitializer(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Add images for existing books if present; don't fail startup if books missing
        bookRepository.findById(1L).ifPresent(bk1 -> {
            if (bk1.getImages().isEmpty()) {
                bk1.getImages().add(new BookImage(bk1, "/images/acer_nitro5.jpg"));
                bk1.getImages().add(new BookImage(bk1, "/images/acer_swift5.jpg"));
                bk1.getImages().add(new BookImage(bk1, "/images/acer_nitro5.jpg"));
                bk1.getImages().add(new BookImage(bk1, "/images/acer_swift5.jpg"));
                bookRepository.save(bk1);
            }
        });

        bookRepository.findById(2L).ifPresent(bk2 -> {
            if (bk2.getImages().isEmpty()) {
                bk2.getImages().add(new BookImage(bk2, "/images/acer_swift5.jpg"));
                bk2.getImages().add(new BookImage(bk2, "/images/acer_nitro5.jpg"));
                bk2.getImages().add(new BookImage(bk2, "/images/asus_rog.jpg"));
                bk2.getImages().add(new BookImage(bk2, "/images/acer_nitro5.jpg"));
                bookRepository.save(bk2);
            }
        });

        bookRepository.findById(3L).ifPresent(bk3 -> {
            if (bk3.getImages().isEmpty()) {
                bk3.getImages().add(new BookImage(bk3, "/images/acer_nitro5.jpg"));
                bk3.getImages().add(new BookImage(bk3, "/images/asus_rog.jpg"));
                bk3.getImages().add(new BookImage(bk3, "/images/asus_rog.jpg"));
                bk3.getImages().add(new BookImage(bk3, "/images/dell_xps13.jpg"));
                bookRepository.save(bk3);
            }
        });

        bookRepository.findById(4L).ifPresent(bk4 -> {
            if (bk4.getImages().isEmpty()) {
                bk4.getImages().add(new BookImage(bk4, "/images/dell_xps13.jpg"));
                bk4.getImages().add(new BookImage(bk4, "/images/dell_xps13.jpg"));
                bk4.getImages().add(new BookImage(bk4, "/images/dell_xps13.jpg"));
                bk4.getImages().add(new BookImage(bk4, "/images/dell_xps13.jpg"));
                bookRepository.save(bk4);
            }
        });

        bookRepository.findById(5L).ifPresent(bk5 -> {
            if (bk5.getImages().isEmpty()) {
                bk5.getImages().add(new BookImage(bk5, "/images/lenovo_x1.jpg"));
                bk5.getImages().add(new BookImage(bk5, "/images/lenovo1.jpg"));
                bk5.getImages().add(new BookImage(bk5, "/images/lenovo_x1.jpg"));
                bk5.getImages().add(new BookImage(bk5, "/images/lenovo_x1.jpg"));
                bookRepository.save(bk5);
            }
        });

        bookRepository.findById(6L).ifPresent(bk6 -> {
            if (bk6.getImages().isEmpty()) {
                bk6.getImages().add(new BookImage(bk6, "/images/macobook_air_m1.jpg"));
                bk6.getImages().add(new BookImage(bk6, "/images/macobook_air_m1_2.jpg"));
                bk6.getImages().add(new BookImage(bk6, "/images/macobook_air_m1_3.jpg"));
                bk6.getImages().add(new BookImage(bk6, "/images/lenovo_x1.jpg"));
                bookRepository.save(bk6);
            }
        });

        bookRepository.findById(7L).ifPresent(bk7 -> {
            if (bk7.getImages().isEmpty()) {
                bk7.getImages().add(new BookImage(bk7, "/images/msi_gf63.jpg"));
                bk7.getImages().add(new BookImage(bk7, "/images/msi_prestige.jpg"));
                bk7.getImages().add(new BookImage(bk7, "/images/msi_gf63_detail.jpg"));
                bk7.getImages().add(new BookImage(bk7, "/images/msi_prestige_detail.jpg"));
                bookRepository.save(bk7);
            }
        });

        bookRepository.findById(8L).ifPresent(bk8 -> {
            if (bk8.getImages().isEmpty()) {
                bk8.getImages().add(new BookImage(bk8, "/images/surface_laptop.jpg"));
                bk8.getImages().add(new BookImage(bk8, "/images/surface_laptop_2.jpg"));
                bk8.getImages().add(new BookImage(bk8, "/images/surface_laptop_3.jpg"));
                bk8.getImages().add(new BookImage(bk8, "/images/surface_laptop_detail.jpg"));
                bookRepository.save(bk8);
            }
        });

        bookRepository.findById(9L).ifPresent(bk9 -> {
            if (bk9.getImages().isEmpty()) {
                bk9.getImages().add(new BookImage(bk9, "/images/acer_nitro5.jpg"));
                bk9.getImages().add(new BookImage(bk9, "/images/acer_swift5.jpg"));
                bk9.getImages().add(new BookImage(bk9, "/images/acer_spin3.jpg"));
                bk9.getImages().add(new BookImage(bk9, "/images/acer_nitro5_detail.jpg"));
                bookRepository.save(bk9);
            }
        });

        bookRepository.findById(10L).ifPresent(bk10 -> {
            if (bk10.getImages().isEmpty()) {
                bk10.getImages().add(new BookImage(bk10, "/images/asus_rog.jpg"));
                bk10.getImages().add(new BookImage(bk10, "/images/asus_vivobook14.jpg"));
                bk10.getImages().add(new BookImage(bk10, "/images/asus_rog_detail.jpg"));
                bk10.getImages().add(new BookImage(bk10, "/images/asus_vivobook14_detail.jpg"));
                bookRepository.save(bk10);
            }
        });

        bookRepository.findById(11L).ifPresent(bk11 -> {
            if (bk11.getImages().isEmpty()) {
                bk11.getImages().add(new BookImage(bk11, "/images/dell_xps13.jpg"));
                bk11.getImages().add(new BookImage(bk11, "/images/dell_xps15.jpg"));
                bk11.getImages().add(new BookImage(bk11, "/images/dell1.jpg"));
                bk11.getImages().add(new BookImage(bk11, "/images/dell_xps13_detail.jpg"));
                bookRepository.save(bk11);
            }
        });

        bookRepository.findById(12L).ifPresent(bk12 -> {
            if (bk12.getImages().isEmpty()) {
                bk12.getImages().add(new BookImage(bk12, "/images/hp_envy13.jpg"));
                bk12.getImages().add(new BookImage(bk12, "/images/hp_spectre.jpg"));
                bk12.getImages().add(new BookImage(bk12, "/images/hp1.jpg"));
                bk12.getImages().add(new BookImage(bk12, "/images/hp_envy13_detail.jpg"));
                bookRepository.save(bk12);
            }
        });

        bookRepository.findById(13L).ifPresent(bk13 -> {
            if (bk13.getImages().isEmpty()) {
                bk13.getImages().add(new BookImage(bk13, "/images/lenovo_x1.jpg"));
                bk13.getImages().add(new BookImage(bk13, "/images/lenovo1.jpg"));
                bk13.getImages().add(new BookImage(bk13, "/images/lenovo_x1_detail.jpg"));
                bk13.getImages().add(new BookImage(bk13, "/images/lenovo1_detail.jpg"));
                bookRepository.save(bk13);
            }
        });

        bookRepository.findById(14L).ifPresent(bk14 -> {
            if (bk14.getImages().isEmpty()) {
                bk14.getImages().add(new BookImage(bk14, "/images/msi_gf63.jpg"));
                bk14.getImages().add(new BookImage(bk14, "/images/msi_gf63.jpg"));
                bk14.getImages().add(new BookImage(bk14, "/images/acer_nitro5.jpg"));
                bk14.getImages().add(new BookImage(bk14, "/images/acer_nitro5.jpg"));
                bookRepository.save(bk14);
            }
        });

        bookRepository.findById(15L).ifPresent(bk15 -> {
            if (bk15.getImages().isEmpty()) {
                bk15.getImages().add(new BookImage(bk15, "/images/msi_gf63.jpg"));
                bk15.getImages().add(new BookImage(bk15, "/images/acer_nitro5.jpg"));
                bk15.getImages().add(new BookImage(bk15, "/images/msi_gf63.jpg"));
                bk15.getImages().add(new BookImage(bk15, "/images/lenovo_x1.jpg"));
                bookRepository.save(bk15);
            }
        });

        bookRepository.findById(16L).ifPresent(bk16 -> {
            if (bk16.getImages().isEmpty()) {
                bk16.getImages().add(new BookImage(bk16, "/images/surface_laptop.jpg"));
                bk16.getImages().add(new BookImage(bk16, "/images/surface_laptop.jpg"));
                bk16.getImages().add(new BookImage(bk16, "/images/surface_laptop.jpg"));
                bk16.getImages().add(new BookImage(bk16, "/images/surface_laptop.jpg"));
                bookRepository.save(bk16);
            }
        });
    }
}
