# RUoDeT (Reinforced Utils of Development Tools ._.)
A tool that auto uploads your newly compiled java projects into your destination (eg. Your VPS)

**NOTE: I CREATED THIS PROJECT IN A DAY TO MAKE THINGS EASIER FOR MYSELF, SO IT MAY NOT HAVE THE BEST AND STANDARD CODE**

Currently, It only supports IntelliJ -> Maven compiles, More customizabilities will be added soon. I will also be happy if you contribute on this project :)

# How to use
1. Run the program
2. Configure the program with the generated ``configuration.toml`` file as you wish.
3. Run the program on two points, one on your PC (act as ``Sender``), one on your server (act as ``Receiver``)

# How to configure
This is how the default configuration looks like:

```toml
function = "sender <or> receiver"

[sender]
  workspace_path = "Path to your IntelliJ workspace"
  [sender.socket]
    host = "localhost"
    port = 4401
    password = "somePassword"

[receiver]
  copy_to_paths = [
    "/home/server-1/plugins",
    "/home/server-2/plugins"
  ]
  [receiver.socket]
    port = 4401
    password = "somePassword"
```

Firstly, you need to set the function, ``Sender`` will listens to your ``workspace_path`` location and grabs jars that their modification date or their size changes.
NOTE: Currently it only looks in ``target`` folder (That Maven generates), It will ignore the jars with ``original`` and ``shaded`` tags.
Secondly, about sockets, If you're planning to run ``Receiver`` on a machine other than your PC, make sure to enter its ip address in ``host``.
Port can be anything, Just be sure that nothing is using that port, Keep in mind that ``Sender`` and ``Receiver`` should have the same port.
Password is just a simple protection layer to avoid other people to send jars to your machine, Both points should have the same password.

On ``Receiver`` options, you just need to put the port and password, ``copy_to_paths`` are your servers' plugins folders, If you have multiple servers, you can put as many as you want.

# Support
If you had any questions, you can contact me on discord: Mohamad82#1474
