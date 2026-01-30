package vn.socialmedia.mapper;

import org.mapstruct.Mapper;
import vn.socialmedia.dto.request.CommentCreationRequest;
import vn.socialmedia.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    Comment toEntity(CommentCreationRequest request);
}
