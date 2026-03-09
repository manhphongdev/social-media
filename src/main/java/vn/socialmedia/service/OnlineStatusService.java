package vn.socialmedia.service;

public interface OnlineStatusService {

    void userConnected(Long userid);

    void userDisconnected(Long userId);

    boolean isOnline(Long userId);

}
