package org.istiaqfuad.eventhub.user.controller;

import org.istiaqfuad.eventhub.security.web.AuthenticatedUser;
import org.istiaqfuad.eventhub.security.web.CurrentUser;
import org.istiaqfuad.eventhub.user.dto.UserResponse;
import org.istiaqfuad.eventhub.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/users", version = "1")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id, @CurrentUser AuthenticatedUser caller) {
        return userService.get(id, caller);
    }
}
