package me.Mohamad82.RUoDeT.client;

import me.Mohamad82.RUoDeT.Main;
import me.Mohamad82.RUoDeT.utils.FileInfo;
import org.apache.commons.io.IOUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientService {

    public final static List<String> excludes = new ArrayList<>();
    public final static AtomicBoolean revertExcludes = new AtomicBoolean(false);
    private final Set<File> cooldownFiles = new HashSet<>();

    private final File workspaceFolder;

    private ClientService(String host, int port, String password, String workspacePath) throws Exception {
        System.out.println("Initialized ClientService.");
        this.workspaceFolder = new File(workspacePath);

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        Runnable runnable = new Runnable() {
            private final Map<File, FileInfo> fileInfo = new HashMap<>();
            boolean firstLoop = true;
            public void run() {
                if (Main.statusCode.get() <= 0) {
                    Main.statusCode.set(-1);
                    return;
                }
                if (Main.statusCode.get() == 2) {
                    return;
                }
                for (File projectFolder : workspaceFolder.listFiles()) {

                    boolean excluded = false;
                    for (String exclude : excludes) {
                        if (projectFolder.getName().contains(exclude)) {
                            excluded = true;
                            break;
                        }
                    }
                    if (revertExcludes.get()) {
                        if (!excluded) continue;
                    } else {
                        if (excluded) continue;
                    }

                    File targetFolder = new File(projectFolder, "target");
                    File buildFolder = new File(projectFolder, "build");
                    if (buildFolder.exists()) {
                        File libsFolder = new File(buildFolder, "libs");
                        if (libsFolder.exists()) {
                            for (File jarFile : libsFolder.listFiles()) {
                                if (jarFile.isDirectory()) continue;
                                if (!jarFile.getName().endsWith(".jar")) continue;
                                boolean shouldBeSent = false;
                                if (fileInfo.containsKey(jarFile)) {
                                    FileInfo fileInfo = this.fileInfo.get(jarFile);
                                    if (fileInfo.getLastModified() != jarFile.lastModified() || fileInfo.getLenght() != jarFile.length()) {
                                        if (jarFile.length() > 1000)
                                            shouldBeSent = true;
                                    }
                                } else {
                                    shouldBeSent = true;
                                }
                                if (shouldBeSent) {
                                    send(jarFile, firstLoop, true, executorService, fileInfo, host, port, password);
                                }
                            }
                        }
                    }
                    if (targetFolder.exists()) {
                        for (File jarFile : targetFolder.listFiles()) {
                            if (jarFile.isDirectory()) continue;
                            if (!jarFile.getName().endsWith(".jar")) continue;
                            if (jarFile.getName().contains("original") || jarFile.getName().contains("shaded")) continue;
                            boolean shouldBeSent = false;
                            if (fileInfo.containsKey(jarFile)) {
                                FileInfo fileInfo = this.fileInfo.get(jarFile);
                                if (fileInfo.getLastModified() != jarFile.lastModified() || fileInfo.getLenght() != jarFile.length()) {
                                    shouldBeSent = true;
                                }
                            } else {
                                shouldBeSent = true;
                            }
                            if (shouldBeSent) {
                                send(jarFile, firstLoop, false, executorService, fileInfo, host, port, password);
                            }
                        }
                    }
                }
                firstLoop = false;
            }
        };

        executorService.scheduleAtFixedRate(runnable, 0, 2, TimeUnit.SECONDS);
    }

    private void send(File jarFile, boolean firstLoop, boolean gradleBuilt, ScheduledExecutorService executorService, Map<File, FileInfo> fileInfo, String host, int port, String password) {
        if (!cooldownFiles.contains(jarFile)) {
            if (firstLoop) {
                fileInfo.put(jarFile, FileInfo.fileInfo(jarFile));
            } else {
                cooldownFiles.add(jarFile);
                executorService.schedule(() -> {

                    fileInfo.put(jarFile, FileInfo.fileInfo(jarFile));

                    System.out.format("Sending '%s' to the server.\n", jarFile.getName());
                    try (Socket socket = new Socket(host, port)) {
                        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                        dataOutputStream.writeUTF(password);
                        dataOutputStream.writeUTF(jarFile.getName());
                        FileInputStream fileInputStream = new FileInputStream(jarFile);

                        IOUtils.copy(fileInputStream, dataOutputStream);

                        fileInputStream.close();
                        dataInputStream.close();
                        dataOutputStream.close();
                        socket.close();
                        System.out.format("'%s' has been sent to the server successfully.\n", jarFile.getName());
                    } catch (Exception e) {
                        System.out.format("Failed to send '%s': %s\n", jarFile.getName(), e.getMessage());
                    }
                    cooldownFiles.remove(jarFile);
                }, gradleBuilt ? 2 : 5, TimeUnit.SECONDS);
            }
        }
    }

    public static ClientService clientService(String host, int port, String password, String workspacePath) throws Exception {
        return new ClientService(host, port, password, workspacePath);
    }

}
