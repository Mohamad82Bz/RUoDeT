package me.Mohamad82.RUoDeT;

import com.moandjiezana.toml.Toml;
import me.Mohamad82.RUoDeT.client.ClientService;
import me.Mohamad82.RUoDeT.server.ServerService;
import me.Mohamad82.RUoDeT.utils.ResourceUtils;

import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public final static AtomicInteger statusCode = new AtomicInteger(1);

    private static String function;

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            File configurationFile = new File("configuration.toml");
            if (!configurationFile.exists()) {
                configurationFile = ResourceUtils.copyResource("configuration.toml", configurationFile);
            }

            Toml toml = new Toml().read(configurationFile);
            function = toml.getString("function");
            try {
                switch (function) {
                    case "sender": {
                        new Thread(() -> {
                            File excludesFile = new File("excludes.toml");
                            if (!excludesFile.exists()) {
                                ResourceUtils.copyResource("excludes.toml", excludesFile);
                            }
                            while (true) {
                                try {
                                    Thread.sleep(5 * 1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                File excludes = new File("excludes.toml");
                                Toml eToml = new Toml().read(excludes);
                                List<String> excludesList = eToml.getList("excludes");

                                if (!ClientService.revertExcludes.get() && eToml.getBoolean("revert-to-includes")) {
                                    ClientService.revertExcludes.set(!ClientService.revertExcludes.get());
                                }
                                if (!ClientService.excludes.containsAll(excludesList)) {
                                    ClientService.excludes.clear();
                                    ClientService.excludes.addAll(excludesList);
                                }
                            }
                        }).start();
                        ClientService.clientService(toml.getString("sender.socket.host"), Math.toIntExact(toml.getLong("sender.socket.port")), toml.getString("sender.socket.password"), toml.getString("sender.workspace_path"));
                        break;
                    }
                    case "receiver": {
                        ServerService.serverService(Math.toIntExact(toml.getLong("receiver.socket.port")), toml.getString("receiver.socket.password"), toml.getList("receiver.copy_to_paths"));
                        break;
                    }
                    default: {
                        System.out.println("Files has been generated, Please configure the program via configuration.toml file then rerun the program.");
                        System.exit(1);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }).start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            Thread.sleep(1000);
            switch (scanner.next().toLowerCase()) {
                case "shutdown":
                case "stop":
                case "exit": {
                    System.out.println("Shutting down...");
                    if (function.equalsIgnoreCase("sender")) {
                        statusCode.set(0);
                        while (true) {
                            if (statusCode.get() == -1)
                                System.exit(1);
                        }
                    } else {
                        System.exit(1);
                    }
                    break;
                }
                case "pause":
                case "p":
                case "2": {
                    statusCode.set(2);
                    System.out.println("\n- Paused -");
                    break;
                }
                case "resume":
                case "r":
                case "1": {
                    statusCode.set(1);
                    System.out.println("\n- Resumed -");
                    break;
                }
            }
        }
    }

}
