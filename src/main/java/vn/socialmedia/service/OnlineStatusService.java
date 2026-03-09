package vn.socialmedia.service;

public interface OnlineStatusService {

    void userConnected(String username);

    void userDisconnected(String username);

    boolean isOnline(String username);

}
