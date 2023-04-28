package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingAllDto;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemAllDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static ru.practicum.shareit.enums.States.PAST;


@Slf4j
@Service
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemStorage;
    private final UserService userService;
    private final CommentRepository commentStorage;
    private final BookingService bookingService;

    @Autowired
    public ItemServiceImpl(ItemRepository itemStorage,
                           UserService userService,
                           CommentRepository commentStorage,
                           BookingService bookingService) {
        this.itemStorage = itemStorage;
        this.userService = userService;
        this.commentStorage = commentStorage;
        this.bookingService = bookingService;
    }

    @Override
    public ItemAllDto get(Long id, Long userId) {
        Item item = itemStorage.findById(id).orElseThrow(
                () -> new ObjectNotFoundException("Вещь с id " + id + " не найдена"));
        List<Comment> comments = commentStorage.findByItem(item, Sort.by(DESC, "created"));
        List<BookingAllDto> bookings = bookingService.getBookingsByItem(item.getId(), userId);
        return ItemMapper.toItemAllFieldsDto(item,
               getLastItem(bookings),
               getNextItem(bookings),
               comments.stream().map(CommentMapper::toCommentDto).collect(toList()));
    }

    @Override
    @Transactional
    public ItemDto save(ItemDto itemDto, Long userId) {
        valid(itemDto);
        User owner = UserMapper.toUser(userService.get(userId));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemStorage.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(ItemDto itemDto, Long id, Long userId) {
        if (userId == null) {
            throw new IncorrectParameterException("Id пользователя не задан!");
        }

        Item item = itemStorage.getReferenceById(id);

        if (!item.getOwner().getId().equals(userId)) {
            throw new ObjectNotFoundException("Пользователь с id=" + userId + " не является владельцем вещи с id=" + id);
        }

        String patchName = itemDto.getName();
        if (Objects.nonNull(patchName) && !patchName.isEmpty()) {
            item.setName(patchName);
        }

        String patchDescription = itemDto.getDescription();
        if (Objects.nonNull(patchDescription) && !patchDescription.isEmpty()) {
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
            List<Item> allItems = itemStorage.findAllByOwner_IdIs(id);
            List<Comment> comments = commentStorage.findByItemIn(allItems, Sort.by(DESC, "created"));
            Map<Long, List<BookingAllDto>> bookings = bookingService.getBookingsByOwner(id, null).stream()
                    .collect(groupingBy((BookingAllDto bookingExtendedDto) -> bookingExtendedDto.getItem().getId()));
            return  allItems.stream()
                    .map(item -> getItemAllFieldsDto(comments, bookings, item))
                    .collect(toList());
        } else {

            throw new ObjectNotFoundException("Пользователь с id" + id + "не найден");
        }
    }

    @Override
    public List<ItemDto> getByText(String text) {
        if (text.isBlank()) return Collections.emptyList();
        return itemStorage.getAllText(text).stream()
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
        Item item = itemStorage.findById(itemId).orElseThrow(
                () -> new ObjectNotFoundException("Вещь с id = " + itemId + " не найдена"));
        User user = UserMapper.toUser(userService.get(userId));
        List<BookingAllDto> bookings = bookingService.getAll(userId, PAST.name());
        if (bookings.isEmpty()) throw new IncorrectParameterException("Нельзя оставить комментарий");
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        Comment save = commentStorage.save(comment);
        return CommentMapper.toCommentDto(save);
    }

    @Override
    public List<CommentDto> getAllComments() {
        return commentStorage.findAll()
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private ItemAllDto getItemAllFieldsDto(List<Comment> comments,
                                           Map<Long, List<BookingAllDto>> bookings,
                                           Item item) {
            return ItemMapper.toItemAllFieldsDto(item,
                    getLastItem(bookings.get(item.getId())),
                    getNextItem(bookings.get(item.getId())),
                    comments.stream().map(CommentMapper::toCommentDto).collect(toList()));
    }

    private void valid(ItemDto itemDto) {
        if (itemDto.getAvailable() == null) {
            throw new IncorrectParameterException("Не определена доступность инструмента");
        } else if (itemDto.getName() == null) {
            throw new IncorrectParameterException("Не задано название инструмента");
        } else if (itemDto.getDescription() == null) {
            throw new IncorrectParameterException("Не задано описание иснтрумента");
        } else if (itemDto.getName().isEmpty() || itemDto.getDescription().isEmpty()) {
            throw new IncorrectParameterException("Некорректно заданы поля в запросе");
        }
    }

    private BookingAllDto getNextItem(List<BookingAllDto> bookings) {
        return bookings != null
                ? bookings.stream()
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()) && !Objects.equals(booking.getStatus().toString(), "REJECTED"))
                .min(Comparator.comparing(BookingAllDto::getEnd)).orElse(null)
                : null;
    }

    private BookingAllDto getLastItem(List<BookingAllDto> bookings) {
        return bookings != null
                ? bookings.stream()
                .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                .max(Comparator.comparing(BookingAllDto::getEnd)).orElse(null)
                : null;
    }
}
