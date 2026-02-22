package com.clinic.mapper;

import com.clinic.dto.request.PatientRegistrationRequest;
import com.clinic.dto.response.PatientResponse;
import com.clinic.dto.response.PatientWithAppointmentsResponse;
import com.clinic.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PatientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "address.street", source = "street")
    @Mapping(target = "address.city", source = "city")
    @Mapping(target = "address.region", source = "region")
    Patient toEntity(PatientRegistrationRequest request);

    @Mapping(target = "street", source = "address.street")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "region", source = "address.region")
    PatientResponse toResponse(Patient patient);

    @Mapping(target = "street", source = "address.street")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "region", source = "address.region")
    PatientWithAppointmentsResponse toResponseWithAppointments(Patient patient);
}
