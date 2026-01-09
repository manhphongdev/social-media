package vn.socialmedia.common.security;

import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.socialmedia.model.User;
import vn.socialmedia.security.user.UserSecurity;

@NoArgsConstructor
public class SecurityUtil {

    public static Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserSecurity(User user)) {
            return user.getId();
        }
        return null;
    }

}
