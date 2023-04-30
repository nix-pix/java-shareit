package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingAllDto;
import ru.practicum.shareit.exception.IncorrectParameterException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemAllDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static ru.practicum.shareit.enums.State.PAST;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final BookingService bookingService;

    @Override
    public ItemAllDto get(Long id, Long userId) {
        Item item = itemRepository.findById(id).orElseThrow(
                () -> new ObjectNotFoundException("Вещь с id " + id + " не найдена"));
        List<Comment> comments = commentRepository.findByItem(item, Sort.by(DESC, "created"));
        List<BookingAllDto> bookings = bookingService.getBookingsByItem(item.getId(), userId);
        LocalDateTime now = LocalDateTime.now();
        return ItemMapper.toItemAllFieldsDto(item,
                getLastItem(bookings, now),
                getNextItem(bookings, now),
                comments.stream().map(CommentMapper::toCommentDto).collect(toList()));
    }

    @Override
    @Transactional
    public ItemDto save(ItemDto itemDto, Long userId) {
        User owner = UserMapper.toUser(userService.get(userId));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(ItemDto itemDto, Long id, Long userId) {
        if (userId == null) {
            throw new IncorrectParameterException("Id пользователя не задан!");
        }
        Item item = itemRepository.getReferenceById(id);
        if (!item.getOwner().getId().equals(userId)) {
            throw new ObjectNotFoundException("Пользователь с id=" + userId + " не является владельцем вещи с id=" + id);
        }
        String patchName = itemDto.getName();
        if (Objects.nonNull(patchName) && !patchName.isBlank()) {
            item.setName(patchName);
        }
        String patchDescription = itemDto.getDescription();
        if (Objects.nonNull(patchDescription) && !patchDescription.isBlank()) {
            item.setDescription(patchDescription);
        }
        Boolean patchAvailable = itemDto.getAvailable();
        if (Objects.nonNull(patchAvailable)) {
            item.setAvailable(patchAvailable);
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemAllDto> getAll(Long id) {
        User owner = UserMapper.toUser(userService.get(id));
        if (owner != null) {
            List<Item> allItems = itemRepository.findAllByOwner_IdIs(id);
            List<Comment> comments = commentRepository.findByItemIn(allItems, Sort.by(DESC, "created"));
            Map<Long, List<BookingAllDto>> bookings = bookingService.getBookingsByOwner(id, null).stream()
                    .collect(groupingBy((BookingAllDto bookingExtendedDto) -> bookingExtendedDto.getItem().getId()));
            return allItems.stream()
                    .map(item -> getItemAllFieldsDto(comments, bookings, item))
                    .collect(toList());
        } else {
            throw new ObjectNotFoundException("Пользователь с id" + id + "не найден");
        }
    }

    @Override
    public List<ItemDto> search(String text) { //Поиск вещи по наличию текста в имени или описании
        if (text.isBlank()) return Collections.emptyList();
        return itemRepository.getAllText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(toList());
    }

    @Override
    @Transactional
    public CommentDto createComment(CommentDto commentDto,
                                    Long itemId,
                                    Long userId) {
        if (commentDto.getText() == null || commentDto.getText().isBlank())
            throw new IncorrectParameterException("Текст комментария не может быть пустым");
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new ObjectNotFoundException("Вещь с id = " + itemId + " не найдена"));
        User user = UserMapper.toUser(userService.get(userId));
        List<BookingAllDto> bookings = bookingService.getAll(userId, PAST.name());
        if (bookings.isEmpty()) throw new IncorrectParameterException("Нельзя оставить комментарий");
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        Comment save = commentRepository.save(comment);
        return CommentMapper.toCommentDto(save);
    }

    @Override
    public List<CommentDto> getAllComments() {
        return commentRepository.findAll()
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private ItemAllDto getItemAllFieldsDto(List<Comment> comments,
                                           Map<Long, List<BookingAllDto>> bookings,
                                           Item item) {
        LocalDateTime now = LocalDateTime.now();
        return ItemMapper.toItemAllFieldsDto(item,
                getLastItem(bookings.get(item.getId()), now),
                getNextItem(bookings.get(item.getId()), now),
                comments.stream().map(CommentMapper::toCommentDto).collect(toList()));
    }

    private BookingAllDto getNextItem(List<BookingAllDto> bookings, LocalDateTime now) {
        return bookings != null
                ? bookings.stream()
                .filter(booking -> booking.getStart().isAfter(now))
                .min(Comparator.comparing(BookingAllDto::getEnd)).orElse(null)
                : null;
    }

    private BookingAllDto getLastItem(List<BookingAllDto> bookings, LocalDateTime now) {
        return bookings != null
                ? bookings.stream()
                .filter(booking -> !booking.getStart().isAfter(now))
                .max(Comparator.comparing(BookingAllDto::getEnd)).orElse(null)
                : null;
    }
}
