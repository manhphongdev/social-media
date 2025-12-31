package vn.socialmedia.auth.sevice;

import vn.socialmedia.dto.request.LoginRequest;
import vn.socialmedia.dto.response.TokenResponse;

public interface AuthenticationService {

    TokenResponse getAccessToken(LoginRequest req);
}
