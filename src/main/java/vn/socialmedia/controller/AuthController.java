package vn.socialmedia.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import vn.socialmedia.sevice.JwtService;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final JwtService jwtService;

}
