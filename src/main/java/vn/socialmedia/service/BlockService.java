package vn.socialmedia.service;

import vn.socialmedia.model.User;

public interface BlockService {

    boolean isBlock(User user, User targetUser);
}
