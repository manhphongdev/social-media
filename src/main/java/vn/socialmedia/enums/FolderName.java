package vn.socialmedia.enums;

import lombok.Getter;

@Getter
public enum FolderName {
    AVATAR("/avatar"),
    POST_IMAGE("/postImage"),
    POST_VIDEO("/postVideo"),
    MESSAGE_IMAGE("/messageImage"),
    MESSAGE_VIDEO("/messageVideo"),
    ;

    private final String path;

    FolderName(String path) {
        this.path = path;
    }
}
