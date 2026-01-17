package vn.socialmedia.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import vn.socialmedia.model.Hashtag;
import vn.socialmedia.repository.HashtagRepo;
import vn.socialmedia.service.HashtagService;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "HASHTAG-SERVICE")
public class HashtagServiceImpl implements HashtagService {
    private final HashtagRepo hashtagRepo;

    @Override
    @Transactional
    public Set<Hashtag> handleHashtags(List<String> hashtags) {

        Set<String> names = hashtags.stream()
                .map(this::normalizeHashtag)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());


        Set<Hashtag> results = new HashSet<>();

        for (String name : names) {
            int updated = hashtagRepo.incrementUsage(name);

            Hashtag tag;

            if (updated == 0) {
                try {
                    tag = hashtagRepo.save(
                            Hashtag.builder()
                                    .name(name)
                                    .usageCount(1)
                                    .build()
                    );
                } catch (DataIntegrityViolationException e) {
                    hashtagRepo.incrementUsage(name);
                    tag = hashtagRepo.findByName(name).orElseThrow();
                }
            } else {
                tag = hashtagRepo.findByName(name).orElseThrow();
            }
            results.add(tag);
        }
        return results;
    }

    private String normalizeHashtag(String hashtag) {
        if (hashtag == null) {
            return null;
        }
        hashtag = hashtag.toLowerCase().trim();

        if (hashtag.contains("#")) {
            hashtag = hashtag.replace("#", "");
        }
        return hashtag;
    }
}
