package com.springboot3.boilerplate.user.admin;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface AdminMapper {
    @Mapping(target = "roles", ignore = true)
    AdminResponse toAdminResponse(Admin admin);
}
