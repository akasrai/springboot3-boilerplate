package com.springboot3.boilerplate.user.enduser;

import com.springboot3.boilerplate.app.dto.ListResponse;
import com.springboot3.boilerplate.security.CurrentUser;
import com.springboot3.boilerplate.security.UserPrincipal;
import com.springboot3.boilerplate.user.enduser.dto.EnduserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
public class EnduserController {

    @Autowired
    private EnduserService enduserService;

    @GetMapping("")
    @PreAuthorize("hasRole('USER')")
    public EnduserResponse getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        return enduserService.getCurrentUser(userPrincipal.getEmail());
    }

    @GetMapping("/locked")
    @PreAuthorize("hasRole('ADMIN')")
    public ListResponse lockedUserList() {
        List<EnduserResponse> senderResponses = enduserService.getLockedUsers();

        return new ListResponse(senderResponses);
    }

    @GetMapping("/{referenceId}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public void unLock(@PathVariable("referenceId") UUID referenceId) {
        enduserService.unLock(referenceId);
    }
}