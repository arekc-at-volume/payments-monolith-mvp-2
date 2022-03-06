package com.volume.institutions.rest;

import com.volume.yapily.YapilyClient;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import yapily.sdk.Country;
import yapily.sdk.Institution;
import yapily.sdk.Media;

import java.util.List;
import java.util.stream.Collectors;

import static yapily.sdk.Institution.FeaturesEnum.*;

@Value
class InstitutionCountryDto {
    private final String displayName;
    private final String countryCode2;

    public static InstitutionCountryDto fromYapily(Country country) {
        return new InstitutionCountryDto(country.getDisplayName(), country.getCountryCode2());
    }
}

@Value
class InstitutionMediaDto {
    private final String type;
    private final String source;

    public static InstitutionMediaDto fromYapily(Media media) {
        return new InstitutionMediaDto(media.getType(), media.getSource());
    }
}

@Value
class InstitutionDto {
    private String id;
    private String name;
    private String fullName;
    private List<InstitutionMediaDto> media;
    private String environmentType;
    private String credentialsType;
    private List<String> features;
    private List<InstitutionCountryDto> countries;

    public static InstitutionDto fromYapily(Institution institution) {
        return new InstitutionDto(
                institution.getId(),
                institution.getName(),
                institution.getFullName(),
                institution.getMedia().stream().map(InstitutionMediaDto::fromYapily).collect(Collectors.toList()),
                institution.getEnvironmentType().toString(),
                institution.getCredentialsType().toString(),
                institution.getFeatures().stream().filter(InstitutionDto::isInterestingFeature).map(Institution.FeaturesEnum::toString).sorted().collect(Collectors.toList()),
                institution.getCountries().stream().map(InstitutionCountryDto::fromYapily).collect(Collectors.toList())
        );
    }

    private static boolean isInterestingFeature(Institution.FeaturesEnum feature) {
        return
                List.of(
                        INITIATE_DOMESTIC_SINGLE_PAYMENT,
                        CREATE_DOMESTIC_SINGLE_PAYMENT,
                        READ_DOMESTIC_SINGLE_REFUND,
                        INITIATE_DOMESTIC_VARIABLE_RECURRING_PAYMENT,
                        CREATE_DOMESTIC_VARIABLE_RECURRING_PAYMENT
                ).contains(feature);
    }
}


@RestController
@AllArgsConstructor
public class InstitutionsController {

    private final YapilyClient yapilyClient;

    @GetMapping("/api/institutions")
    List<InstitutionDto> getInstitutions() {
        return yapilyClient.getInstitutions()
                .stream()
                .map(InstitutionDto::fromYapily)
                .collect(Collectors.toList());
    }

}
