package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOwner_IdIs(Long ownerId);

    @Query("select item from Item item " +
            "where item.available = TRUE " +
            "and (upper(item.name) " +
            "like upper(concat('%', ?1, '%')) " +
            "or upper(item.description) " +
            "like upper(concat('%', ?1, '%')))")
    List<Item> getAllText(String text);
}
