/* CS 656 Fall 2021 Rx V3.00
*
* Group Name: D11
* Members:    Ashray Kengunte Jayachandra(Ak379), Soumilee Ghosh(sg342), Sintu Boro(sb394), Noumala Hemanth Reddy(hn39), Sada Siva Tej Velalla(sv97)
*
* Follow all directions carefully. Your total file must 
* be less than 250 lines excluding comments.
*/
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.InetSocketAddress;

/* no additional imports permitted UNLESS you have
  instructor permission */

/*
  Do not change any method or field names or anything
  else marked DO_NOT. You may
  add your own private fields or local variables or
  methods. You cannot change main() except as indicated.
  String and friends are prohibited except where
  indicated below.
*/

public class Rx {
 private Socket       socket = null;
 private InputStream  inputStream = null;
 private List<Packet> pList = null;
 private byte[] buffer = new byte[1506];

// DO_NOT change signature, String OK here
public Rx(String host, int port) throws IOException {
  socket = new Socket();
  socket.connect( new InetSocketAddress(host, port) );
  // ADD print here if you want
  System.out.println("HostName: "+socket.getInetAddress());
  System.out.println("PortNumber: "+socket.getPort());
  }

public void close() throws IOException {
 // ADD your code here
    System.out.println();
    System.out.println("Connection is closed!");
	socket.close();
}

// DO_NOT not change this method; String OK here
public int run(String fname) {
  try {
    get_pkts();
    order_pkts();
    write_pkts(fname);
    close();
  } catch (Exception e) { e.printStackTrace(); }

  return 0;
}

/* get_pkts: pull all packets off the wire and store in pList
   this method also prints the packet info stats              */
public int get_pkts ( ) throws IOException {
  // ADD your variables here
	
	int packets = 0 ;  // how many packets read
    pList = new ArrayList<>();
    long receivedTime = System.currentTimeMillis();
    long endTime;
    double totalDelay = 0.0;
    double packetDelay = 0.0;
    int count = 1;
  /* loop: get all packets and capture stats
     must use getInputStream.read()          */
  while(socket.getInputStream().read(buffer,0,1506) != -1  ) 
  {
	  Packet packet = new Packet(buffer);
	  pList.add(packet);
	  packets = packets + 1;
      endTime = System.currentTimeMillis();
      packetDelay =  endTime - receivedTime;
      totalDelay = totalDelay + packetDelay;

      if(count <= 14) {
          System.out.println("Packet "+count+"  SEQNO= " + packet.getSeqNo() + " Length= " + packet.getLen() + " Delay= " + packetDelay+" ms");
          count++;
      }
  } // while (read packets)
//ADD print Total 14 packets line here
    
    double avg = totalDelay/packets;
    System.out.println("Total packets received: "+packets+" Total Delay= "+totalDelay+" ms");
    System.out.printf("Average: %.2f",avg);
return packets;
  
}

// DO_NOT change the signature, String OK here
public void write_pkts(String f) throws Exception {
	// this must call Packet.write() for each Packet
    FileOutputStream outputFile = new FileOutputStream(f);

    for(int x= 0; x < pList.size() -1; x++) {
       outputFile.write(pList.get(x).getPayload()) ;
    }
}

// put the pList in the correct order
public void order_pkts() {
    Packet sortPkt = null;
  for(int i = 0; i < pList.size() -1 ; i++) {
        for(int j = pList.size() -1; j > i ; j--){

            if(pList.get(i).getSeqNo() > pList.get(j).getSeqNo()){

                sortPkt = pList.get(i);
                pList.set(i,pList.get(j));
                pList.set(j,sortPkt);
            }
        }
    }
}

// DO_NOT change main at all! String OK here
public static void main(String[] args) {
  if(args.length != 3) {
    System.out.println("Usage: host  port filename");
    return;
  }

  try {
    Rx recv = new Rx(  args[0],
                         Integer.parseInt(args[1]));
		recv.run ( args[2] );
  } catch (Exception e) { e.printStackTrace(); }
} // main()

} // class Rx


/* Packet class */
class Packet {
/* DO_NOT change these private fields */
private byte[]      payload;
private int         seqNo;
private short       len;
private PrintStream tty;
    public  int getSeqNo(){
        return seqNo;
    }

    public int getLen(){
        return len;
    }

    public  byte[] getPayload(){
        return  payload;
    }

    byte[] sequence = new byte[4];
    byte[] length = new byte[2];

/* this CTOR is used to make a packet from
   a buffer that came off the wire         */
public Packet(byte[] buf) {

    for (int i = 0; i <= 3; i++) {
        sequence[i] = buf[i];
    }

    for(int j = 0; j <= 1 ; j++){
       length[j] = buf[j+4];
   }

 seqNo = get_seqno(sequence); // must use only this method
 len   = get_len(length);   // must use only this method


    payload = new byte[1500];
 // ADD code here for payload

    for(int x= 0; x < buf.length -6 ; x++) {
        payload[x] = buf[x+6];
    }
 // you may remove this debug line after you
 // get it working
 //System.out.println("DEBUG len " + len + " seq " + seqNo + " buflen " + buf.length);

} // Packet CTOR

/* ADD code in these 2 methods to parse the packet header
 * remember, no bitshift/bitmask/Math/libs: just basic math ops */
    private int  get_seqno(byte []b) {

        int num = byteToInt(b[0]) * pow(256, 3) + byteToInt(b[1]) * pow(256, 2) + byteToInt(b[2]) * pow(256, 1)
                + byteToInt(b[3]);
        return num;
    }

    private short get_len(byte []b) {
        return (short) (byteToInt(b[0]) * pow(256, 1) + byteToInt(b[1]));
    }

    private int byteToInt(byte b) {
        if (b < 0) {
            return b + 256;
        }
        return b;
    }
    private int pow(int base, int expo) {
        int res = base;
        for (int i = 1; i < expo; i++) {
            res = res * base;
        }
        return res;
    }

    // write this Packet to file: no need to change this
public void write(FileOutputStream f) throws IOException {
  f.write(payload);
}

// your methods etc here
}// class Packet
