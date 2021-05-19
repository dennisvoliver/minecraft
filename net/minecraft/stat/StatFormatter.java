package net.minecraft.stat;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Util;

public interface StatFormatter {
   DecimalFormat DECIMAL_FORMAT = (DecimalFormat)Util.make(new DecimalFormat("########0.00"), (decimalFormat) -> {
      decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
   });
   StatFormatter DEFAULT;
   StatFormatter DIVIDE_BY_TEN;
   StatFormatter DISTANCE;
   StatFormatter TIME;

   @Environment(EnvType.CLIENT)
   String format(int i);

   static {
      NumberFormat var10000 = NumberFormat.getIntegerInstance(Locale.US);
      DEFAULT = var10000::format;
      DIVIDE_BY_TEN = (i) -> {
         return DECIMAL_FORMAT.format((double)i * 0.1D);
      };
      DISTANCE = (i) -> {
         double d = (double)i / 100.0D;
         double e = d / 1000.0D;
         if (e > 0.5D) {
            return DECIMAL_FORMAT.format(e) + " km";
         } else {
            return d > 0.5D ? DECIMAL_FORMAT.format(d) + " m" : i + " cm";
         }
      };
      TIME = (i) -> {
         double d = (double)i / 20.0D;
         double e = d / 60.0D;
         double f = e / 60.0D;
         double g = f / 24.0D;
         double h = g / 365.0D;
         if (h > 0.5D) {
            return DECIMAL_FORMAT.format(h) + " y";
         } else if (g > 0.5D) {
            return DECIMAL_FORMAT.format(g) + " d";
         } else if (f > 0.5D) {
            return DECIMAL_FORMAT.format(f) + " h";
         } else {
            return e > 0.5D ? DECIMAL_FORMAT.format(e) + " m" : d + " s";
         }
      };
   }
}
