package com.springboot3.boilerplate.user.enduser;


import com.springboot3.boilerplate.user.auth.dto.OAuth2Response;
import com.springboot3.boilerplate.user.auth.dto.SignUpRequest;
import com.springboot3.boilerplate.user.enduser.dto.EnduserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface EnduserMapper {
    Enduser toEnduser(SignUpRequest signUpRequest);

    EnduserResponse toEnduserResponse(Enduser enduser);

    @Mapping(target = "roles", ignore = true)
    OAuth2Response toOAuth2Response(Enduser enduser);

    List<EnduserResponse> toEnduserResponseList(List<Enduser> endusers);
}