package no.bekk.boss.bpep.example;

public class Person {
    String firstname;
    String lastname;
    String address;
    String zipcode;
    String city;

    public Person(final PersonBuilder personBuilder) {
        address = personBuilder.address;
    }
}
