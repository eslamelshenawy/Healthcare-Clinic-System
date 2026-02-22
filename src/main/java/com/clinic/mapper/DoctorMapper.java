package com.clinic.mapper;

import com.clinic.dto.response.DoctorResponse;
import com.clinic.entity.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface DoctorMapper {

    @Mapping(target = "specialty", expression = "java(doctor.getSpecialty().name())")
    DoctorResponse toResponse(Doctor doctor);

    List<DoctorResponse> toResponseList(List<Doctor> doctors);
}
