package com.dotnomi.fabricdependencyinjection;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * A wrapper class for a list of beans, intended for injection.
 * This class is an immutable record that implements {@link Iterable} and provides
 * convenient delegate methods to access the underlying list.
 * Using this wrapper distinguishes a request for a list of beans from a request for a single bean of type List.
 *
 * @param beans The list of bean instances.
 * @param <T> The type of the beans in the list.
 */
public record BeanList<T>(@NotNull List<T> beans) implements Iterable<T> {
  /**
   * Returns an iterator over the elements in this list.
   *
   * @return an iterator over the elements in this list.
   */
  @Override
  public @NotNull Iterator<T> iterator() {
    return beans.iterator();
  }

  /**
   * Performs the given action for each element of the list until all elements
   * have been processed or the action throws an exception.
   *
   * @param action The action to be performed for each element.
   */
  @Override
  public void forEach(@NotNull Consumer<? super T> action) {
    this.beans.forEach(action);
  }

  /**
   * Creates a {@link Spliterator} over the elements in this list.
   *
   * @return a {@code Spliterator} over the elements in this list.
   */
  @Override
  public @NotNull Spliterator<T> spliterator() {
    return this.beans.spliterator();
  }

  /**
   * Returns the number of beans in this list.
   *
   * @return The size of the list.
   */
  public @NotNull Integer size() {
    return this.beans.size();
  }

  /**
   * Checks if the list of beans is empty.
   *
   * @return True if the list is empty, false otherwise.
   */
  public @NotNull Boolean isEmpty() {
    return this.beans.isEmpty();
  }

  /**
   * Returns the bean at the specified position in this list.
   *
   * @param index The index of the bean to return.
   * @return The bean at the specified index.
   */
  public @NotNull T get(int index) {
    return this.beans.get(index);
  }

  /**
   * Returns the first bean in this list.
   *
   * @return The first bean.
   */
  public @NotNull T getFirst() {
    return this.beans.getFirst();
  }

  /**
   * Returns the last bean in this list.
   *
   * @return The last bean.
   */
  public @NotNull T getLast() {
    return this.beans.getLast();
  }

  /**
   * Returns an unmodifiable view of the list of beans.
   *
   * @return An unmodifiable {@link List}.
   */
  public @NotNull List<T> asList() {
    return Collections.unmodifiableList(this.beans);
  }
}
