package com.volume.payments.shared.infrastructure.persistence;

import lombok.*;
import org.junit.jupiter.api.Test;

import javax.persistence.*;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

//@Value
//class EmailAddress implements ValueObject {
//    private final String email;
//
//    public EmailAddress(String email) {
//        this.email = validate(email);
//    }
//
//    public static String validate(String email) {
//        try {
//            if (!isValid(email))
//                throw new IllegalArgumentException(format("Invalid email: ", email));
//        } catch (IllegalArgumentException exception) {
//            throw exception;
//        }
//        return email;
//    }
//
//    public static boolean isValid(String email) {
//        if (Strings.isBlank(email))
//            throw new IllegalArgumentException(format("Email cannot be empty or null"));
//        if (!email.contains("@"))
//            throw new IllegalArgumentException(format("Email %s does not contain @ character", email));
//        String[] parts = email.split("@");
//        if (parts[0].length() > 64)
//            throw new IllegalArgumentException(format("User name part of email %s cannot be longer than 64 characters", email));
//        if (email.length() > 254)
//            throw new IllegalArgumentException(format("Email address cannot be longer than 254 characters", email));
//
//        return true;
//    }
//
//    public static EmailAddress testExample() {
//        return new EmailAddress("arek@test.com");
//    }
//}
//
//@Converter
//class EmailAddressAttributeConverter implements AttributeConverter<EmailAddress, String> {
//
//    @Override
//    public EmailAddress convertToEntityAttribute (String attribute) {
//        return attribute == null ? null : new EmailAddress(attribute);
//    }
//
//    @Override
//    public String convertToDatabaseColumn(EmailAddress emailAddress) {
//        return emailAddress == null ? null : emailAddress.getEmail().toString();
//    }
//}
//
//@Embeddable
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@Getter
//@EqualsAndHashCode
//@ToString
//class Address implements ValueObject {
//    private String country;
//    private String city;
//    private String street;
//
//    public Address(String country, String city, String street) {
//        this.country = Objects.requireNonNull(country);
//        this.city = Objects.requireNonNull(city);
//        this.street = Objects.requireNonNull(street);
//    }
//
//    public static Address testExample() {
//        return new Address("Poland", "Kraków", "Obozowa");
//    }
//}
//
//@Entity
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor
//@Getter
//@ToString
//class CustomerEntity {
//    @Id
//    UUID id;
//    @Convert(converter = EmailAddressAttributeConverter.class)
//    @Column(length = 320)
//    private EmailAddress email;
//    @Embedded
//    private Address address;
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) return true;
//        if (obj == null) return false;
//        if (this.getClass() != obj.getClass()) return false;
//        CustomerEntity other = (CustomerEntity) obj;
//
//        if (id == null) return false;
//        if (id.equals(other.id)) return true;
//
//        return false;
//    }
//
//    @Override
//    public int hashCode() {
//        var prime = 31;
//        return prime + ((id == null) ? 0 : id.hashCode());
//    }
//}

@Entity
@ToString
class SimpleEntityToCompareEquality extends BaseKeyedVersionedEntity<UUID> {
    private String name;

    protected SimpleEntityToCompareEquality() {}

    public SimpleEntityToCompareEquality(UUID id, String name) {
        super(id);
        this.name = name;
    }
}

/**
 * Tests whether BaseKeyedVersionedEntity behaves correctly when it comes to equality.
 *
 * TODO: rework these tests to follow official equality requirements. All these
 * - reflexive
 * - symmetric
 * - transitive
 * - consistent
 */
public class BaseKeyedVersionedEntityEqualityTests {

    @Test
    void comparingToRightNullReturnsFalse() {
        var instance = new SimpleEntityToCompareEquality(UUID.randomUUID(), "arek");
        assertThat(instance.equals(null)).isFalse();
    }

    @Test
    void comparingToRightOfAnotherTypeReturnsFalse() {
        var instance = new SimpleEntityToCompareEquality(UUID.randomUUID(), "arek");
        assertThat(instance.equals(new String("terefere"))).isFalse();
    }

    @Test
    void comparingWithLeftHavingNullIdReturnsFalse() {
        var instance1 = new SimpleEntityToCompareEquality(null, "arek");
        var instance2 = new SimpleEntityToCompareEquality(UUID.randomUUID(), "arek");
        assertThat(instance1.equals(instance2)).isFalse();
    }

    @Test
    void comparingWhenBothHaveNullIdReturnsFalse() {
        var instance1 = new SimpleEntityToCompareEquality(null, "arek");
        var instance2 = new SimpleEntityToCompareEquality(null, "arek");
        assertThat(instance1.equals(instance2)).isFalse();
    }

    @Test
    void comparingWhenRightHasNullIdReturnsFalse() {
        var instance1 = new SimpleEntityToCompareEquality(UUID.randomUUID(), "arek");
        var instance2 = new SimpleEntityToCompareEquality(null, "arek");
        assertThat(instance1.equals(instance2)).isFalse();
    }

    @Test
    void comparingWithBothIdsNotNullButDifferentReturnsFalse() {
        var instance1 = new SimpleEntityToCompareEquality(UUID.randomUUID(), "arek");
        var instance2 = new SimpleEntityToCompareEquality(UUID.randomUUID(), "arek");
        assertThat(instance1.equals(instance2)).isFalse();
    }

    @Test
    void comparingWithBothIdsNotNullAndTheSameReturnsFalse() {
        var leftId = UUID.randomUUID();
        var rightId = UUID.randomUUID();
        var instance1 = new SimpleEntityToCompareEquality(leftId, "arek");
        var instance2 = new SimpleEntityToCompareEquality(rightId, "arek");
        assertThat(instance1.equals(instance2)).isFalse();
    }

}
