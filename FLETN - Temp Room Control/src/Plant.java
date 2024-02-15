import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Plant {
    //initialize socket and input stream
    private Socket socket;
    private ServerSocket server;
    private DataInputStream in;

    public Plant(int port)
    {
        try
        {
            server = new ServerSocket(Constants.PORT);
            System.out.println("Server started on port: "+Constants.PORT);

            System.out.println("Waiting for client");

            socket = server.accept();
            System.out.println("Client connected");

            // take input from the client
            in = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));

            String line = "";

            // reads message from client until "Stop" is sent
            while (!line.equals("Stop"))
            {
                try
                {
                    line = in.readUTF();
                    System.out.println(line);

                }
                catch(IOException i)
                {
                    System.out.println(i);
                }
            }
            System.out.println("Closing connection");

            socket.close();
            in.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }

    public static void main(String args[])
    {

        Plant server = new Plant(Constants.PORT);
    }
}
