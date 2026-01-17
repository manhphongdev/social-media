package vn.socialmedia.service;

import vn.socialmedia.model.Hashtag;

import java.util.List;
import java.util.Set;

public interface HashtagService {

    Set<Hashtag> handleHashtags(List<String> hashtags);
}
