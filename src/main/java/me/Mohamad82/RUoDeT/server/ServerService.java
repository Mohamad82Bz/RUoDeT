package me.Mohamad82.RUoDeT.server;

import me.Mohamad82.RUoDeT.Main;
import org.apache.commons.io.IOUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ServerService {

    private final ServerSocket serverSocket;

    private ServerService(int port, String password, List<String> copyPaths) throws Exception {
        System.out.println("Initialized ServerService.");
        this.serverSocket = new ServerSocket(port);

        while (true) {
            if (Main.statusCode.get() == 2) {
                Thread.sleep(1000);
                continue;
            }
            Socket socket = serverSocket.accept();
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            String incomingPassword = dataInputStream.readUTF();
            String incomingFileName = dataInputStream.readUTF();

            System.out.format("Client is sending '%s' to the server.\n", incomingFileName);

            if (password.equals(incomingPassword)) {
                File file = new File(incomingFileName);
                file.createNewFile();

                FileOutputStream fileOutputStream = new FileOutputStream(file);
                IOUtils.copy(dataInputStream, fileOutputStream);

                dataInputStream.close();
                dataOutputStream.close();
                fileOutputStream.close();

                System.out.format("Client sent '%s' to the server successfully.\n", incomingFileName);

                for (String path : copyPaths) {
                    File suitableFile = new File(path, file.getName());
                    if (suitableFile.exists())
                        Files.copy(
                                file.toPath(),
                                suitableFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING,
                                StandardCopyOption.COPY_ATTRIBUTES
                        );
                }

                file.delete();
            } else {
                System.out.format("%s tried to send data with a wrong password! (%s)\n", socket, incomingPassword);
            }
        }
    }

    public static ServerService serverService(int port, String password, List<String> copyPaths) throws Exception {
        return new ServerService(port, password, copyPaths);
    }

}
