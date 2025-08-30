package com.dotnomi.fabricdependencyinjection.exception;

import java.util.ArrayList;
import java.util.List;

public final class CircularDependencyFormatter {
  /**
   * Formats a circular dependency path into a human-readable string representation.
   * The formatted string includes the names of the classes in the dependency path,
   * arranged in a graphical layout highlighting the circular dependency.
   *
   * @param path a list of classes representing the circular dependency path
   * @return a formatted string visually describing the circular dependency
   */
  public static String format(List<Class<?>> path) {
    if (path == null || path.isEmpty()) {
      return "Circular dependency detected! The classes depend on each other in an endless loop. Path: undefined";
    }

    var lastClass = path.getLast();
    var cycleStartIndex = path.indexOf(lastClass);
    if (cycleStartIndex == -1) cycleStartIndex = 0;

    var allNames = path.stream().map(Class::getSimpleName).toList();
    var preCycleNames = allNames.subList(0, cycleStartIndex);
    var cycleNames = allNames.subList(cycleStartIndex, allNames.size());

    var pathSummary = String.join(" -> ", allNames);
    var messageBuilder = new StringBuilder(String.format("Circular dependency detected! The classes depend on each other in an endless loop. Path: %s", pathSummary)).append("\n\n\t");

    var biggestNameLength = allNames.stream().mapToInt(String::length).max().orElse(0);
    var rowLength = Math.max((biggestNameLength / 3) + 4, 5);

    var topRow = "┌" + "─".repeat(rowLength) + "┐\n\t";
    var bottomRow = "└" + "─".repeat(rowLength) + "┘\n";
    var errorRow = "│" + " ".repeat(rowLength) + "X\n\t";
    var arrowRow = " " + " ".repeat(rowLength) + "↓\n\t";
    var circleArrowRow = "│" + " ".repeat(rowLength) + "↓\n\t";

    for (var className : preCycleNames) {
      var centerLength = (biggestNameLength - className.length()) / 2;
      messageBuilder.append(String.format("   " + " ".repeat(centerLength) + "%s\n\t", className));
      messageBuilder.append(arrowRow);
    }

    if (cycleNames.isEmpty()) {
      return messageBuilder.toString();
    }

    messageBuilder.append(topRow).append(errorRow);

    var cachedCycleNames = new ArrayList<>();
    for (var className : cycleNames) {
      if (cachedCycleNames.contains(className)) continue;
      var centerLength = (biggestNameLength - className.length()) / 2;
      messageBuilder.append(String.format("│  " + " ".repeat(centerLength) + "%s\n\t", className));
      messageBuilder.append(circleArrowRow);
      cachedCycleNames.add(className);
    }

    messageBuilder.append(bottomRow);
    return messageBuilder.toString();
  }
}
