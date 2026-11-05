package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

public class MainApp {

    private static final String FISIER = "src/main/resources/angajati.json";

    public static void scriere(List<Angajat> lista) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            var file = new File(FISIER);
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, lista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Angajat> citire() {
        try {
            var file = new File(FISIER);
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(file, new TypeReference<List<Angajat>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean esteConducere(Angajat a) {
        var post = a.getPostul().toLowerCase();
        return post.contains("sef") || post.contains("șef") || post.contains("director");
    }

    public static void main(String[] args) {
        var angajati = citire();
        if (angajati == null) {
            System.out.println("Nu s-au putut citi datele.");
            return;
        }

        var anCurent = LocalDate.now().getYear();
        var anTrecut = anCurent - 1;

        System.out.println("1. Lista angajatilor:");
        angajati.forEach(System.out::println);

        System.out.println("\n2. Angajatii cu salariul peste 2500 RON:");
        angajati.stream()
                .filter(a -> a.getSalariul() > 2500)
                .forEach(System.out::println);

        System.out.println("\n3. Angajati de conducere din aprilie " + anTrecut + ":");
        var conducereAprilie = angajati.stream()
                .filter(a -> a.getData_angajarii().getYear() == anTrecut)
                .filter(a -> a.getData_angajarii().getMonth() == Month.APRIL)
                .filter(a -> esteConducere(a))
                .collect(Collectors.toList());
        conducereAprilie.forEach(System.out::println);

        System.out.println("\n4. Angajatii fara functie de conducere (salariu descrescator):");
        angajati.stream()
                .filter(a -> !esteConducere(a))
                .sorted((a1, a2) -> Float.compare(a2.getSalariul(), a1.getSalariul()))
                .forEach(System.out::println);

        System.out.println("\n5. Numele angajatilor cu majuscule:");
        var nume = angajati.stream()
                .map(a -> a.getNumele().toUpperCase())
                .collect(Collectors.toList());
        System.out.println(nume);

        System.out.println("\n6. Salariile mai mici de 3000 RON:");
        angajati.stream()
                .map(Angajat::getSalariul)
                .filter(s -> s < 3000)
                .forEach(System.out::println);

        System.out.println("\n7. Primul angajat al firmei:");
        var primul = angajati.stream()
                .min((a1, a2) -> a1.getData_angajarii().compareTo(a2.getData_angajarii()));
        if (primul.isPresent()) {
            System.out.println(primul.get());
        } else {
            System.out.println("Nu exista angajati in firma.");
        }

        System.out.println("\n8. Statistici salarii:");
        var statistici = angajati.stream()
                .collect(Collectors.summarizingDouble(Angajat::getSalariul));
        System.out.printf("Salariul mediu: %.2f%n", statistici.getAverage());
        System.out.printf("Salariul minim: %.2f%n", statistici.getMin());
        System.out.printf("Salariul maxim: %.2f%n", statistici.getMax());

        System.out.println("\n9. Verificare Ion:");
        angajati.stream()
                .filter(a -> List.of(a.getNumele().split(" ")).contains("Ion"))
                .findAny()
                .ifPresentOrElse(
                        a -> System.out.println("Firma are cel putin un Ion angajat"),
                        () -> System.out.println("Firma nu are nici un Ion angajat"));

        System.out.println("\n10. Angajati in vara anului " + anTrecut + ":");
        var nrVara = angajati.stream()
                .filter(a -> a.getData_angajarii().getYear() == anTrecut)
                .filter(a -> {
                    var luna = a.getData_angajarii().getMonth();
                    return luna == Month.JUNE || luna == Month.JULY || luna == Month.AUGUST;
                })
                .count();
        System.out.println("Numar: " + nrVara);

        scriere(angajati);
    }
}