package net.minecraft.client.realms;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.realms.dto.WorldDownload;
import net.minecraft.client.realms.exception.RealmsDefaultUncaughtExceptionHandler;
import net.minecraft.client.realms.gui.screen.RealmsDownloadLatestWorldScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class FileDownload {
   private static final Logger LOGGER = LogManager.getLogger();
   private volatile boolean cancelled;
   private volatile boolean finished;
   private volatile boolean error;
   private volatile boolean extracting;
   private volatile File backupFile;
   private volatile File resourcePackPath;
   private volatile HttpGet httpRequest;
   private Thread currentThread;
   private final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(120000).setConnectTimeout(120000).build();
   private static final String[] INVALID_FILE_NAMES = new String[]{"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

   public long contentLength(String downloadLink) {
      CloseableHttpClient closeableHttpClient = null;
      HttpGet httpGet = null;

      long var5;
      try {
         httpGet = new HttpGet(downloadLink);
         closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
         CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
         var5 = Long.parseLong(closeableHttpResponse.getFirstHeader("Content-Length").getValue());
         return var5;
      } catch (Throwable var16) {
         LOGGER.error("Unable to get content length for download");
         var5 = 0L;
      } finally {
         if (httpGet != null) {
            httpGet.releaseConnection();
         }

         if (closeableHttpClient != null) {
            try {
               closeableHttpClient.close();
            } catch (IOException var15) {
               LOGGER.error((String)"Could not close http client", (Throwable)var15);
            }
         }

      }

      return var5;
   }

   public void downloadWorld(WorldDownload download, String message, RealmsDownloadLatestWorldScreen.DownloadStatus status, LevelStorage storage) {
      if (this.currentThread == null) {
         this.currentThread = new Thread(() -> {
            CloseableHttpClient closeableHttpClient = null;
            boolean var90 = false;

            label1407: {
               CloseableHttpResponse httpResponse4;
               FileOutputStream outputStream4;
               FileDownload.DownloadCountingOutputStream downloadCountingOutputStream4;
               FileDownload.ResourcePackProgressListener resourcePackProgressListener3;
               label1401: {
                  try {
                     var90 = true;
                     this.backupFile = File.createTempFile("backup", ".tar.gz");
                     this.httpRequest = new HttpGet(download.downloadLink);
                     closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
                     httpResponse4 = closeableHttpClient.execute(this.httpRequest);
                     status.totalBytes = Long.parseLong(httpResponse4.getFirstHeader("Content-Length").getValue());
                     if (httpResponse4.getStatusLine().getStatusCode() != 200) {
                        this.error = true;
                        this.httpRequest.abort();
                        var90 = false;
                        break label1407;
                     }

                     outputStream4 = new FileOutputStream(this.backupFile);
                     FileDownload.ProgressListener progressListener = new FileDownload.ProgressListener(message.trim(), this.backupFile, storage, status);
                     downloadCountingOutputStream4 = new FileDownload.DownloadCountingOutputStream(outputStream4);
                     downloadCountingOutputStream4.setListener(progressListener);
                     IOUtils.copy((InputStream)httpResponse4.getEntity().getContent(), (OutputStream)downloadCountingOutputStream4);
                     var90 = false;
                     break label1401;
                  } catch (Exception var103) {
                     LOGGER.error("Caught exception while downloading: " + var103.getMessage());
                     this.error = true;
                     var90 = false;
                  } finally {
                     if (var90) {
                        this.httpRequest.releaseConnection();
                        if (this.backupFile != null) {
                           this.backupFile.delete();
                        }

                        if (!this.error) {
                           if (!download.resourcePackUrl.isEmpty() && !download.resourcePackHash.isEmpty()) {
                              try {
                                 this.backupFile = File.createTempFile("resources", ".tar.gz");
                                 this.httpRequest = new HttpGet(download.resourcePackUrl);
                                 HttpResponse httpResponse5 = closeableHttpClient.execute(this.httpRequest);
                                 status.totalBytes = Long.parseLong(httpResponse5.getFirstHeader("Content-Length").getValue());
                                 if (httpResponse5.getStatusLine().getStatusCode() != 200) {
                                    this.error = true;
                                    this.httpRequest.abort();
                                    return;
                                 }

                                 OutputStream outputStream5 = new FileOutputStream(this.backupFile);
                                 FileDownload.ResourcePackProgressListener resourcePackProgressListener4 = new FileDownload.ResourcePackProgressListener(this.backupFile, status, download);
                                 FileDownload.DownloadCountingOutputStream downloadCountingOutputStream5 = new FileDownload.DownloadCountingOutputStream(outputStream5);
                                 downloadCountingOutputStream5.setListener(resourcePackProgressListener4);
                                 IOUtils.copy((InputStream)httpResponse5.getEntity().getContent(), (OutputStream)downloadCountingOutputStream5);
                              } catch (Exception var95) {
                                 LOGGER.error("Caught exception while downloading: " + var95.getMessage());
                                 this.error = true;
                              } finally {
                                 this.httpRequest.releaseConnection();
                                 if (this.backupFile != null) {
                                    this.backupFile.delete();
                                 }

                              }
                           } else {
                              this.finished = true;
                           }
                        }

                        if (closeableHttpClient != null) {
                           try {
                              closeableHttpClient.close();
                           } catch (IOException var91) {
                              LOGGER.error("Failed to close Realms download client");
                           }
                        }

                     }
                  }

                  this.httpRequest.releaseConnection();
                  if (this.backupFile != null) {
                     this.backupFile.delete();
                  }

                  if (!this.error) {
                     if (!download.resourcePackUrl.isEmpty() && !download.resourcePackHash.isEmpty()) {
                        try {
                           this.backupFile = File.createTempFile("resources", ".tar.gz");
                           this.httpRequest = new HttpGet(download.resourcePackUrl);
                           httpResponse4 = closeableHttpClient.execute(this.httpRequest);
                           status.totalBytes = Long.parseLong(httpResponse4.getFirstHeader("Content-Length").getValue());
                           if (httpResponse4.getStatusLine().getStatusCode() != 200) {
                              this.error = true;
                              this.httpRequest.abort();
                              return;
                           }

                           outputStream4 = new FileOutputStream(this.backupFile);
                           resourcePackProgressListener3 = new FileDownload.ResourcePackProgressListener(this.backupFile, status, download);
                           downloadCountingOutputStream4 = new FileDownload.DownloadCountingOutputStream(outputStream4);
                           downloadCountingOutputStream4.setListener(resourcePackProgressListener3);
                           IOUtils.copy((InputStream)httpResponse4.getEntity().getContent(), (OutputStream)downloadCountingOutputStream4);
                        } catch (Exception var99) {
                           LOGGER.error("Caught exception while downloading: " + var99.getMessage());
                           this.error = true;
                        } finally {
                           this.httpRequest.releaseConnection();
                           if (this.backupFile != null) {
                              this.backupFile.delete();
                           }

                        }
                     } else {
                        this.finished = true;
                     }
                  }

                  if (closeableHttpClient != null) {
                     try {
                        closeableHttpClient.close();
                     } catch (IOException var93) {
                        LOGGER.error("Failed to close Realms download client");
                     }

                     return;
                  }

                  return;
               }

               this.httpRequest.releaseConnection();
               if (this.backupFile != null) {
                  this.backupFile.delete();
               }

               if (!this.error) {
                  if (!download.resourcePackUrl.isEmpty() && !download.resourcePackHash.isEmpty()) {
                     try {
                        this.backupFile = File.createTempFile("resources", ".tar.gz");
                        this.httpRequest = new HttpGet(download.resourcePackUrl);
                        httpResponse4 = closeableHttpClient.execute(this.httpRequest);
                        status.totalBytes = Long.parseLong(httpResponse4.getFirstHeader("Content-Length").getValue());
                        if (httpResponse4.getStatusLine().getStatusCode() != 200) {
                           this.error = true;
                           this.httpRequest.abort();
                           return;
                        }

                        outputStream4 = new FileOutputStream(this.backupFile);
                        resourcePackProgressListener3 = new FileDownload.ResourcePackProgressListener(this.backupFile, status, download);
                        downloadCountingOutputStream4 = new FileDownload.DownloadCountingOutputStream(outputStream4);
                        downloadCountingOutputStream4.setListener(resourcePackProgressListener3);
                        IOUtils.copy((InputStream)httpResponse4.getEntity().getContent(), (OutputStream)downloadCountingOutputStream4);
                     } catch (Exception var101) {
                        LOGGER.error("Caught exception while downloading: " + var101.getMessage());
                        this.error = true;
                     } finally {
                        this.httpRequest.releaseConnection();
                        if (this.backupFile != null) {
                           this.backupFile.delete();
                        }

                     }
                  } else {
                     this.finished = true;
                  }
               }

               if (closeableHttpClient != null) {
                  try {
                     closeableHttpClient.close();
                  } catch (IOException var94) {
                     LOGGER.error("Failed to close Realms download client");
                  }
               }

               return;
            }

            this.httpRequest.releaseConnection();
            if (this.backupFile != null) {
               this.backupFile.delete();
            }

            if (!this.error) {
               if (!download.resourcePackUrl.isEmpty() && !download.resourcePackHash.isEmpty()) {
                  try {
                     this.backupFile = File.createTempFile("resources", ".tar.gz");
                     this.httpRequest = new HttpGet(download.resourcePackUrl);
                     HttpResponse httpResponse2 = closeableHttpClient.execute(this.httpRequest);
                     status.totalBytes = Long.parseLong(httpResponse2.getFirstHeader("Content-Length").getValue());
                     if (httpResponse2.getStatusLine().getStatusCode() != 200) {
                        this.error = true;
                        this.httpRequest.abort();
                        return;
                     }

                     OutputStream outputStream = new FileOutputStream(this.backupFile);
                     FileDownload.ResourcePackProgressListener resourcePackProgressListener = new FileDownload.ResourcePackProgressListener(this.backupFile, status, download);
                     FileDownload.DownloadCountingOutputStream downloadCountingOutputStream = new FileDownload.DownloadCountingOutputStream(outputStream);
                     downloadCountingOutputStream.setListener(resourcePackProgressListener);
                     IOUtils.copy((InputStream)httpResponse2.getEntity().getContent(), (OutputStream)downloadCountingOutputStream);
                  } catch (Exception var97) {
                     LOGGER.error("Caught exception while downloading: " + var97.getMessage());
                     this.error = true;
                  } finally {
                     this.httpRequest.releaseConnection();
                     if (this.backupFile != null) {
                        this.backupFile.delete();
                     }

                  }
               } else {
                  this.finished = true;
               }
            }

            if (closeableHttpClient != null) {
               try {
                  closeableHttpClient.close();
               } catch (IOException var92) {
                  LOGGER.error("Failed to close Realms download client");
               }
            }

         });
         this.currentThread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
         this.currentThread.start();
      }
   }

   public void cancel() {
      if (this.httpRequest != null) {
         this.httpRequest.abort();
      }

      if (this.backupFile != null) {
         this.backupFile.delete();
      }

      this.cancelled = true;
   }

   public boolean isFinished() {
      return this.finished;
   }

   public boolean isError() {
      return this.error;
   }

   public boolean isExtracting() {
      return this.extracting;
   }

   public static String findAvailableFolderName(String folder) {
      folder = folder.replaceAll("[\\./\"]", "_");
      String[] var1 = INVALID_FILE_NAMES;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         String string = var1[var3];
         if (folder.equalsIgnoreCase(string)) {
            folder = "_" + folder + "_";
         }
      }

      return folder;
   }

   private void untarGzipArchive(String name, File archive, LevelStorage storage) throws IOException {
      Pattern pattern = Pattern.compile(".*-([0-9]+)$");
      int i = 1;
      char[] var7 = SharedConstants.INVALID_CHARS_LEVEL_NAME;
      int var8 = var7.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         char c = var7[var9];
         name = name.replace(c, '_');
      }

      if (StringUtils.isEmpty(name)) {
         name = "Realm";
      }

      name = findAvailableFolderName(name);

      try {
         Iterator var146 = storage.getLevelList().iterator();

         while(var146.hasNext()) {
            LevelSummary levelSummary = (LevelSummary)var146.next();
            if (levelSummary.getName().toLowerCase(Locale.ROOT).startsWith(name.toLowerCase(Locale.ROOT))) {
               Matcher matcher = pattern.matcher(levelSummary.getName());
               if (matcher.matches()) {
                  if (Integer.valueOf(matcher.group(1)) > i) {
                     i = Integer.valueOf(matcher.group(1));
                  }
               } else {
                  ++i;
               }
            }
         }
      } catch (Exception var145) {
         LOGGER.error((String)"Error getting level list", (Throwable)var145);
         this.error = true;
         return;
      }

      String string2;
      if (storage.isLevelNameValid(name) && i <= 1) {
         string2 = name;
      } else {
         string2 = name + (i == 1 ? "" : "-" + i);
         if (!storage.isLevelNameValid(string2)) {
            boolean bl = false;

            while(!bl) {
               ++i;
               string2 = name + (i == 1 ? "" : "-" + i);
               if (storage.isLevelNameValid(string2)) {
                  bl = true;
               }
            }
         }
      }

      TarArchiveInputStream tarArchiveInputStream = null;
      File file = new File(MinecraftClient.getInstance().runDirectory.getAbsolutePath(), "saves");
      boolean var108 = false;

      LevelStorage.Session session2;
      Throwable var155;
      Path path2;
      label1421: {
         try {
            var108 = true;
            file.mkdir();
            tarArchiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(archive))));

            for(TarArchiveEntry tarArchiveEntry = tarArchiveInputStream.getNextTarEntry(); tarArchiveEntry != null; tarArchiveEntry = tarArchiveInputStream.getNextTarEntry()) {
               File file2 = new File(file, tarArchiveEntry.getName().replace("world", string2));
               if (tarArchiveEntry.isDirectory()) {
                  file2.mkdirs();
               } else {
                  file2.createNewFile();
                  FileOutputStream fileOutputStream = new FileOutputStream(file2);
                  Throwable var12 = null;

                  try {
                     IOUtils.copy((InputStream)tarArchiveInputStream, (OutputStream)fileOutputStream);
                  } catch (Throwable var135) {
                     var12 = var135;
                     throw var135;
                  } finally {
                     if (fileOutputStream != null) {
                        if (var12 != null) {
                           try {
                              fileOutputStream.close();
                           } catch (Throwable var132) {
                              var12.addSuppressed(var132);
                           }
                        } else {
                           fileOutputStream.close();
                        }
                     }

                  }
               }
            }

            var108 = false;
            break label1421;
         } catch (Exception var143) {
            LOGGER.error((String)"Error extracting world", (Throwable)var143);
            this.error = true;
            var108 = false;
         } finally {
            if (var108) {
               if (tarArchiveInputStream != null) {
                  tarArchiveInputStream.close();
               }

               if (archive != null) {
                  archive.delete();
               }

               try {
                  LevelStorage.Session session3 = storage.createSession(string2);
                  Throwable var22 = null;

                  try {
                     session3.save(string2.trim());
                     Path path3 = session3.getDirectory(WorldSavePath.LEVEL_DAT);
                     readNbtFile(path3.toFile());
                  } catch (Throwable var129) {
                     var22 = var129;
                     throw var129;
                  } finally {
                     if (session3 != null) {
                        if (var22 != null) {
                           try {
                              session3.close();
                           } catch (Throwable var128) {
                              var22.addSuppressed(var128);
                           }
                        } else {
                           session3.close();
                        }
                     }

                  }
               } catch (IOException var137) {
                  LOGGER.error((String)"Failed to rename unpacked realms level {}", (Object)string2, (Object)var137);
               }

               this.resourcePackPath = new File(file, string2 + File.separator + "resources.zip");
            }
         }

         if (tarArchiveInputStream != null) {
            tarArchiveInputStream.close();
         }

         if (archive != null) {
            archive.delete();
         }

         try {
            session2 = storage.createSession(string2);
            var155 = null;

            try {
               session2.save(string2.trim());
               path2 = session2.getDirectory(WorldSavePath.LEVEL_DAT);
               readNbtFile(path2.toFile());
            } catch (Throwable var131) {
               var155 = var131;
               throw var131;
            } finally {
               if (session2 != null) {
                  if (var155 != null) {
                     try {
                        session2.close();
                     } catch (Throwable var130) {
                        var155.addSuppressed(var130);
                     }
                  } else {
                     session2.close();
                  }
               }

            }
         } catch (IOException var139) {
            LOGGER.error((String)"Failed to rename unpacked realms level {}", (Object)string2, (Object)var139);
         }

         this.resourcePackPath = new File(file, string2 + File.separator + "resources.zip");
         return;
      }

      if (tarArchiveInputStream != null) {
         tarArchiveInputStream.close();
      }

      if (archive != null) {
         archive.delete();
      }

      try {
         session2 = storage.createSession(string2);
         var155 = null;

         try {
            session2.save(string2.trim());
            path2 = session2.getDirectory(WorldSavePath.LEVEL_DAT);
            readNbtFile(path2.toFile());
         } catch (Throwable var134) {
            var155 = var134;
            throw var134;
         } finally {
            if (session2 != null) {
               if (var155 != null) {
                  try {
                     session2.close();
                  } catch (Throwable var133) {
                     var155.addSuppressed(var133);
                  }
               } else {
                  session2.close();
               }
            }

         }
      } catch (IOException var142) {
         LOGGER.error((String)"Failed to rename unpacked realms level {}", (Object)string2, (Object)var142);
      }

      this.resourcePackPath = new File(file, string2 + File.separator + "resources.zip");
   }

   private static void readNbtFile(File file) {
      if (file.exists()) {
         try {
            CompoundTag compoundTag = NbtIo.readCompressed(file);
            CompoundTag compoundTag2 = compoundTag.getCompound("Data");
            compoundTag2.remove("Player");
            NbtIo.writeCompressed(compoundTag, file);
         } catch (Exception var3) {
            var3.printStackTrace();
         }
      }

   }

   @Environment(EnvType.CLIENT)
   class DownloadCountingOutputStream extends CountingOutputStream {
      private ActionListener listener;

      public DownloadCountingOutputStream(OutputStream out) {
         super(out);
      }

      public void setListener(ActionListener listener) {
         this.listener = listener;
      }

      protected void afterWrite(int n) throws IOException {
         super.afterWrite(n);
         if (this.listener != null) {
            this.listener.actionPerformed(new ActionEvent(this, 0, (String)null));
         }

      }
   }

   @Environment(EnvType.CLIENT)
   class ResourcePackProgressListener implements ActionListener {
      private final File tempFile;
      private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;
      private final WorldDownload worldDownload;

      private ResourcePackProgressListener(File tempFile, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus, WorldDownload worldDownload) {
         this.tempFile = tempFile;
         this.downloadStatus = downloadStatus;
         this.worldDownload = worldDownload;
      }

      public void actionPerformed(ActionEvent e) {
         this.downloadStatus.bytesWritten = ((FileDownload.DownloadCountingOutputStream)e.getSource()).getByteCount();
         if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled) {
            try {
               String string = Hashing.sha1().hashBytes(Files.toByteArray(this.tempFile)).toString();
               if (string.equals(this.worldDownload.resourcePackHash)) {
                  FileUtils.copyFile(this.tempFile, FileDownload.this.resourcePackPath);
                  FileDownload.this.finished = true;
               } else {
                  FileDownload.LOGGER.error("Resourcepack had wrong hash (expected " + this.worldDownload.resourcePackHash + ", found " + string + "). Deleting it.");
                  FileUtils.deleteQuietly(this.tempFile);
                  FileDownload.this.error = true;
               }
            } catch (IOException var3) {
               FileDownload.LOGGER.error((String)"Error copying resourcepack file", (Object)var3.getMessage());
               FileDownload.this.error = true;
            }
         }

      }
   }

   @Environment(EnvType.CLIENT)
   class ProgressListener implements ActionListener {
      private final String worldName;
      private final File tempFile;
      private final LevelStorage levelStorageSource;
      private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;

      private ProgressListener(String worldName, File tempFile, LevelStorage storage, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus) {
         this.worldName = worldName;
         this.tempFile = tempFile;
         this.levelStorageSource = storage;
         this.downloadStatus = downloadStatus;
      }

      public void actionPerformed(ActionEvent e) {
         this.downloadStatus.bytesWritten = ((FileDownload.DownloadCountingOutputStream)e.getSource()).getByteCount();
         if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled && !FileDownload.this.error) {
            try {
               FileDownload.this.extracting = true;
               FileDownload.this.untarGzipArchive(this.worldName, this.tempFile, this.levelStorageSource);
            } catch (IOException var3) {
               FileDownload.LOGGER.error((String)"Error extracting archive", (Throwable)var3);
               FileDownload.this.error = true;
            }
         }

      }
   }
}
