package vn.socialmedia.common.security;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.socialmedia.config.user.UserSecurity;
import vn.socialmedia.enums.ErrorCode;
import vn.socialmedia.exception.BusinessException;
import vn.socialmedia.model.User;

@Slf4j
@NoArgsConstructor
public class SecurityUtil {

    public static User getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED);
        }
        return ((UserSecurity) auth.getPrincipal()).user();
    }

}
