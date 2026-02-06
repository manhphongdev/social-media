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
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "HASHTAG-SERVICE")
public class HashtagServiceImpl implements HashtagService {
    private final HashtagRepo hashtagRepo;

    @Override
    @Transactional
    public Set<Hashtag> handleHashtags(Set<String> hashtags) {

        Set<Hashtag> results = new HashSet<>();

        for (String hashtag : hashtags) {
            int updated = hashtagRepo.incrementUsage(hashtag);

            Hashtag tag;

            if (updated == 0) {
                try {
                    tag = hashtagRepo.save(
                            Hashtag.builder()
                                    .name(hashtag)
                                    .usageCount(1)
                                    .build()
                    );
                } catch (DataIntegrityViolationException e) {
                    hashtagRepo.incrementUsage(hashtag);
                    tag = hashtagRepo.findByName(hashtag).orElseThrow();
                }
            } else {
                tag = hashtagRepo.findByName(hashtag).orElseThrow();
            }
            results.add(tag);
        }
        return results;
    }

}
