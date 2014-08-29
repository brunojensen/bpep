package no.bekk.boss.bpep.example;

public class PersonCreator {
    public static void main(final String[] args) {
        final Person person = new PersonBuilder().lastname("jensen").build();
        person.toString();
    }
}
