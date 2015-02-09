package com.kowsercse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Coursera {
  private final File source = new File("/Users/kowser/CourseraTemp");
  private final File destination = new File("/Users/kowser/temp");
  private final Pattern pattern = Pattern.compile("([0-9]+)\\s*\\-\\s*([0-9]+)\\s*\\-\\s*(.*)\\.mp4");
  private final Function<File, Integer> classifier = lesson -> {
    final Matcher matcher = pattern.matcher(lesson.getName());
    matcher.matches();
    return Integer.valueOf(matcher.group(1));
  };

  public static void main(String ...args) {
    new Coursera().move();
  }
  public void move() {
    for (final File courseDirectory : source.listFiles()) {
      final File[] files = courseDirectory.listFiles((dir, name) -> pattern.matcher(name).matches());
      if(files != null) {
        final File destinationDir = new File(destination, courseDirectory.getName());
        if(destinationDir.mkdir()) {
          final Map<Integer, Set<File>> lectures = Arrays.asList(files).stream()
              .collect(Collectors.groupingBy(classifier, Collectors.mapping(x -> x, Collectors.toSet())));

          int i = 1;
          for (final Set<File> weeklyLessons : lectures.values()) {
            final int weekNumber = i;
            final File weekDirectory = new File(destinationDir, String.valueOf(weekNumber));
            if(weekDirectory.mkdirs()) {
              weeklyLessons.forEach(lesson -> {
                final Matcher matcher = pattern.matcher(lesson.getName());
                matcher.matches();
                final Integer lessonNumber = Integer.valueOf(matcher.group(2));
                final String title = matcher.group(3);

                final String destinationTitle = String.format("%s s%02de%02d %s.mp4", courseDirectory.getName(), weekNumber, lessonNumber, title);
                final File dest = new File(weekDirectory, destinationTitle);
                System.out.println(dest);

                try {
                  Files.createSymbolicLink(dest.toPath(), lesson.toPath());
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
//                lesson.renameTo(dest);
              });
            }
            i++;
          }
//          lectures.forEach((weekNumber, weeklyLessons) -> {
//            final File weekDirectory = new File(destinationDir, String.valueOf(weekNumber));
//            if(weekDirectory.mkdirs()) {
//              weeklyLessons.forEach(lesson -> {
//                final Matcher matcher = pattern.matcher(lesson.getName());
//                matcher.matches();
//                final Integer lessonNumber = Integer.valueOf(matcher.group(2));
//                final String title = matcher.group(3);
//
//                final String destinationTitle = String.format("%s s%02de%02d %s.mp4", courseDirectory.getName(), weekNumber, lessonNumber, title);
//                final File dest = new File(weekDirectory, destinationTitle);
//                System.out.println(dest);
////                lesson.renameTo(dest);
//              });
//            }
//          });
        }
      }
      else {
        System.out.println(courseDirectory.getName());
      }
    }

  }
}
