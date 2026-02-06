package vn.socialmedia.service;

import vn.socialmedia.model.Hashtag;

import java.util.Set;

public interface HashtagService {

    Set<Hashtag> handleHashtags(Set<String> hashtags);
}
