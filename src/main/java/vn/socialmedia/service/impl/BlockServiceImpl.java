package vn.socialmedia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.socialmedia.model.User;
import vn.socialmedia.repository.BlockRepo;
import vn.socialmedia.service.BlockService;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

    private final BlockRepo blockRepo;

    @Override
    public boolean isBlock(User user, User targetUser) {
        return blockRepo.existsByBlockerAndBlocked(user, targetUser);
    }
}
