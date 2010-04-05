package org.intrace.client.cui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Command line agent client.
 */
public class TraceClient
{
  /**
   * Cmd line tool.
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception
  {
    BufferedReader readIn = new BufferedReader(new InputStreamReader(System.in));
    String inLine = "";
    while (!"quit".equals(inLine))
    {
      System.out.print("Enter command [connect/quit]: ");
      inLine = readIn.readLine();

      if ("connect".equals(inLine))
      {
        connectToAgent(readIn);
      }
    }
  }

  private static void connectToAgent(BufferedReader xiReadIn) throws Exception
  {
    System.out.print("Enter host address: ");
    String host = xiReadIn.readLine();
    System.out.print("Enter port: ");
    String port = xiReadIn.readLine();

    Socket socket = new Socket();
    socket.connect(new InetSocketAddress(host, Integer.valueOf(port)));

    System.out.println("Connected!");
    System.out.println("=> " + receiveMessage(socket));

    String inLine = "";
    while (!"disconnect".equals(inLine))
    {
      System.out.print("Enter command [help]: ");
      inLine = xiReadIn.readLine();
      if (!"".equals(inLine))
      {
        sendMessage(socket, inLine);
        System.out.println("=> " + receiveMessage(socket));
      }
    }
  }

  private static String receiveMessage(Socket xiConnectedClient) throws IOException
  {
    InputStream in = xiConnectedClient.getInputStream();
    ObjectInputStream objIn = new ObjectInputStream(in);
    try
    {
      return (String)objIn.readObject();
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
    return null;
  }

  private static void sendMessage(Socket xiConnectedClient, String xiString) throws IOException
  {
    OutputStream out = xiConnectedClient.getOutputStream();
    ObjectOutputStream objOut = new ObjectOutputStream(out);
    objOut.writeObject(xiString);
    objOut.flush();
  }
}
