import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.lang.Math;
import java.util.concurrent.TimeUnit;

//import java.util.Scanner;

public class Sender {
    private static Socket socket = null;
    private static ServerSocket serverSocket = null;
    private static OutputStream  outputStream = null;
    private static FileInputStream fileRead = null;
    private static String fileName = "sendThisFile.txt";
 
    public static void main(String args[]) throws IOException {
        if (args.length != 1) {  
            System.err.println("Usage:  <port number>");
            System.exit(1);
        }
       
        int portNumber = Integer.parseInt(args[0]);
       
        //Connecting to the client
        try {
            //System.out.println("Connecting on port number :"+portNumber);
            serverSocket = new ServerSocket(portNumber);
           
            System.out.println("Server Started...... Waiting for the client");
            socket = serverSocket.accept();
            System.out.println("Connected to a client...");
            outputStream = socket.getOutputStream();
            fileRead = new FileInputStream(fileName);
           
        }
        catch (Exception e) {
            System.out.println("Some Error");
            System.out.println(e);
        }
       
        int bytesRemaining = fileRead.available();
        int seq_num = 2000;
        int packets = 0;
        int seqInc = 1;
       
        ArrayList<byte[]> p_list = new ArrayList<byte[]>();
       
        // Reading data from the file and storing it a ArrayList (p_list)
        while (bytesRemaining>0) {
            int data_length = (int) (Math.random()*1500);
            data_length = (bytesRemaining<data_length)? bytesRemaining : data_length;
           
            byte[] payload = new byte[data_length];
            byte[] writeData = new byte[data_length + 6];
                   
            fileRead.read(payload, 0, data_length);
           
            byte[] header = headerGenerator(seq_num, data_length);
           
            System.arraycopy(header, 0, writeData, 0, 6);
            System.arraycopy(payload, 0, writeData, 6, data_length);
           
            // outputStream.write(writeData);
            p_list.add(writeData);
           
            seq_num += seqInc;
            bytesRemaining -= data_length;
            packets++;
        }
       
        //sending packets randomly with random delay
        int temp_packet = packets;
        boolean[] packet_send = new boolean[packets];
        for(int i=0; i<packets; i++){
            packet_send[i] = false;
        }
       
        int delay = (int)(Math.random()*170);
        delay += 10;
        while(temp_packet>0) {
            int rand = (int)(Math.random()*packets);
           
            if (packet_send[rand]) {
              continue;
            }
            outputStream.write(p_list.get(rand));
            packet_send[rand] = true;
            temp_packet--;
            try {
                Thread.currentThread().sleep(delay);
            }
            catch(InterruptedException e) {
                System.out.println("Some error occured, \n"+e);
            }
        }
       
        System.out.println("Total Pckets sent = " + packets);    
    }  
   
   
    //Generating a header for the packet
    static byte[] headerGenerator(int seq_num, int data_len) {
        byte header[] = new byte[6];
        /*for(int i=0; i<4; i++) {
            int temp = seq_num%256;
            header[3-i] = (byte) temp;
            seq_num /= 256;
        }*/

        for(int i=3; i>=0; i--) {
            int temp = seq_num%256;
            header[i] = (byte) temp;
            seq_num /= 256;
     }

        for(int j=0; j<2; j++){
            int temp = data_len%256;
            header[5-j] = (byte) temp;
            data_len /= 256;
        }
        return header;
    }  
}

