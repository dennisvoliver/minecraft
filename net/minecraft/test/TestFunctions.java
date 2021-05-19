package net.minecraft.test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public class TestFunctions {
   private static final Collection<TestFunction> TEST_FUNCTIONS = Lists.newArrayList();
   private static final Set<String> testClasses = Sets.newHashSet();
   private static final Map<String, Consumer<ServerWorld>> WORLD_SETTERS = Maps.newHashMap();
   private static final Collection<TestFunction> field_25302 = Sets.newHashSet();

   public static Collection<TestFunction> getTestFunctions(String testClass) {
      return (Collection)TEST_FUNCTIONS.stream().filter((testFunction) -> {
         return isInClass(testFunction, testClass);
      }).collect(Collectors.toList());
   }

   public static Collection<TestFunction> getTestFunctions() {
      return TEST_FUNCTIONS;
   }

   public static Collection<String> getTestClasses() {
      return testClasses;
   }

   public static boolean testClassExists(String testClass) {
      return testClasses.contains(testClass);
   }

   @Nullable
   public static Consumer<ServerWorld> getWorldSetter(String batchId) {
      return (Consumer)WORLD_SETTERS.get(batchId);
   }

   public static Optional<TestFunction> getTestFunction(String structurePath) {
      return getTestFunctions().stream().filter((testFunction) -> {
         return testFunction.getStructurePath().equalsIgnoreCase(structurePath);
      }).findFirst();
   }

   public static TestFunction getTestFunctionOrThrow(String structurePath) {
      Optional<TestFunction> optional = getTestFunction(structurePath);
      if (!optional.isPresent()) {
         throw new IllegalArgumentException("Can't find the test function for " + structurePath);
      } else {
         return (TestFunction)optional.get();
      }
   }

   private static boolean isInClass(TestFunction testFunction, String testClass) {
      return testFunction.getStructurePath().toLowerCase().startsWith(testClass.toLowerCase() + ".");
   }

   public static Collection<TestFunction> method_29405() {
      return field_25302;
   }

   public static void method_29404(TestFunction testFunction) {
      field_25302.add(testFunction);
   }

   public static void method_29406() {
      field_25302.clear();
   }
}
