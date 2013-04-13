import java.util.Enumeration;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.security.*;
import com.sun.comm.Win32Driver;
import gnu.io.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
/**
 *
 * @edited by Soufiane.T   12-01-2012
 * Feel free to contact for an example via github
 */
public class mifare {

	
	
	 byte KeyTypeA = (byte) 0xAA;
	 byte KeyTypeB = (byte) 0xBB;
	 byte KeyMifareDefault = (byte) 0xFF;
	 public static  byte[] E2promKeyA = new byte[] {0x10,0x11,0x12,0x13,0x14,0x15,0x16,0x17,0x18,0x19,0x1A,0x1B,0x1C,0x1D,0x1E,0x1F};
     public static  byte[] E2promKeyB = new byte[]{ 0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F };
     
   public enum ASource{
       KeyTypeA /*= 0xAA*/,
        KeyTypeB /* 0xBB*/,
        KeyMifareDefault/* = 0xFF*/}
    ;
        private static final int baudrate = 19200;
	private static final int databits = SerialPort.DATABITS_8;
	private static final int stopbit = SerialPort.STOPBITS_1;
	private static final int parity = SerialPort.PARITY_NONE;
	   Cipher ecipher;
	    Cipher dcipher;
        private CommPortIdentifier portId;
	private SerialPort serialPort;
        private DataInputStream in;
	private OutputStream out;
	
	public void DesEncrypter(SecretKey key){
		
        try {
            ecipher = Cipher.getInstance("DES");
            dcipher = Cipher.getInstance("DES");
            ecipher.init(Cipher.ENCRYPT_MODE, key);
            dcipher.init(Cipher.DECRYPT_MODE, key);

        } catch (javax.crypto.NoSuchPaddingException e) {
        } catch (java.security.NoSuchAlgorithmException e) {
        } catch (java.security.InvalidKeyException e) {
        }
	}
        public mifare(String serialID){
        	
        	

           Win32Driver w32Driver = new Win32Driver();
	   w32Driver.initialize();

           	try
		{

			System.out.println("Trying to open serial port '" + serialID + "'...");
			portId = CommPortIdentifier.getPortIdentifier(serialID);
			serialPort = (SerialPort) portId.open("send", 5000);
			System.out.println("Successfully opened serial port '" + serialPort.getName() + "'!");

			serialPort.setSerialPortParams(baudrate, databits, stopbit, parity);
                        serialPort.setDTR(true);

			in = new DataInputStream(serialPort.getInputStream());
                        out = serialPort.getOutputStream();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			System.exit(1);					
		}


        }

        
        @SuppressWarnings("restriction")
		public String encrypt(String str) throws IOException,IllegalBlockSizeException, BadPaddingException {
   
                byte[] utf8 = str.getBytes("UTF8");     
		byte[] enc = ecipher.doFinal(utf8);
                return new sun.misc.BASE64Encoder().encode(enc);
        }

        public String decrypt(String str) throws GeneralSecurityException, BadPaddingException, IOException {
            
                
                byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);
                byte[] utf8 = dcipher.doFinal(dec);
                return new String(utf8, "UTF8");
          
            
        }
        
        public boolean SelectTag(){
            
            return true;
        }
public void send_command(byte Command, byte[] DataBuffer, byte DataCount) throws IOException
        {
            byte[] TxFrame = new byte[24];
            int i;
            byte checksum;

            DataCount++; 
            checksum = 0;
            TxFrame[0] =(byte) 0xFF;
            TxFrame[1] = 0x00;
            TxFrame[2] = DataCount;
            TxFrame[3] = Command;

            checksum = TxFrame[1];
            checksum += TxFrame[2];
            checksum += TxFrame[3];


            for (i = 0; i < DataCount; i++)
            {
                TxFrame[i + 4] = DataBuffer[i];
                checksum += DataBuffer[i];
            }

            TxFrame[DataCount + 3] = checksum;
        try {
           
            out.write(TxFrame, 0, DataCount + 4);
        } catch (IOException ex) {
            Logger.getLogger(mifare.class.getName()).log(Level.SEVERE, null, ex);
        }
               out.close();

        }



        byte[] MyBuffer;
        byte Count;

     private boolean  get_response()
        {
            MyBuffer = new byte[24];
            int i;
            Count = 0;

           
            for (i = 0; i < 4; i++)
            {

                try
                {
                    MyBuffer[i] = (byte)in.readByte();
                }
                catch (IOException e) 
                {
                    return false;
                } 

            }

            if (MyBuffer[2] > 20) 
                return false;

           
            for (i = 0; i < MyBuffer[2]; i++)
            {
                try
                {
                    MyBuffer[i + 4] = (byte)in.readByte();
                }
                catch (IOException e) 
                {
                    return false;
                }
            }
            Count = MyBuffer[2];
            Count += 4;

            return true;

        }

         public void get_data( byte[] Response, byte Count)
	{
	
	int i;
	byte y;

	y = 0;

		if (Count >= 4) 
		{

			Count--; 

		for (i = 4; i < Count; i++)
		{
			Response[y] = Response[i];
			y++;
		}

	}

	Count = y;
        }

         		
         							public byte ReturnCode;
         							public byte TagType;
         							public byte[] TagSerial;
         							
         							
          public Boolean CMD_ResetDevice()
         					        {
         					            
          byte[] Response = new byte[24];
         	byte[] DataBuffer = new byte[20];
         	  int i;
         	byte Count = 0;
         	String Firmware = "";

         					                        

         	 byte a = 0x08;
         	 byte b = 0;
         	 try {
				send_command(a, DataBuffer, b);
			} catch (IOException e) {

				e.printStackTrace();
			}
         					   
         				

            if (get_response())
            {
                get_data(Response,Count); 

              
                for (i = 0; i < Count; i++)
                Firmware += Response[i];

                return true;
            }
            else
                return false;

        	}
         							
         							
      public byte[] CMD_SelectTag() throws IOException
          		{
    	  		
    	  byte[] c = new byte[24];
    	  		byte[] Response = new byte[24];
    	  		byte[] DataBuffer = new byte[20];
    	  		TagSerial = new byte[4];
    	  		byte Countm = 0;
    	  		TagType = 0;

    	  			ReturnCode = 0;  
            
            	send_command((byte) 0x83, DataBuffer,(byte) 0);
            		

            		if (get_response())
            			{
            				DataBuffer  = MyBuffer;
            					Countm=Count;
            						Response=MyBuffer;
         
            						
            						get_data(Response, Countm);
       
                if (Countm > 1)
                {

                    TagType = Response[0];                           
                    TagSerial[0] = Response[4];    
                    TagSerial[1] = Response[3];
                    TagSerial[2] = Response[2];
                    TagSerial[3] = Response[1];
                     System.out.println("ok! ");
                    return TagSerial; 
                }
                	else
                {
                	System.out.println("reponce "+ReturnCode);
                    ReturnCode = Response[0];
                    return c;

                		}


            			}
            		return c;
  }
          
          
      public boolean CMD_SeekForTag() throws IOException
      {

          byte[] Response = new byte[24];
          byte[] DataBuffer = new byte[20];
          byte Countm = 0;
          byte ReturnCode = 0;
          

         
          
          send_command((byte) 0x82, DataBuffer,(byte) 0); 


          if (get_response())
          {
        	  
        	  DataBuffer  = MyBuffer;
				Countm=Count;
					Response=MyBuffer;
              get_data(Response,Countm);         
              
                  if (Response[0]==0x4C)
                	 
                  return true;
                  else
                  {
                  ReturnCode = Response[0];
                  return false;
                  }


          }
          else
              return false; 
      }
      
      
      public boolean CMD_Authenticate(byte AuthSource, byte[] Key, byte BlockNo) throws IOException
      {
          byte[] Response = new byte[24];
          byte[] DataBuffer = new byte[20];
          byte i = 0;
          byte Countm = 0;
          byte ReturnCode = 0;

          
          

          DataBuffer[0] = BlockNo;
          DataBuffer[1] = AuthSource;
          
          if ((AuthSource == KeyTypeA) || (AuthSource == KeyTypeB))
          {
        	  
              for (i = 0; i < 6; i++) 
              DataBuffer[2 + i] = Key[i];
              send_command((byte)0x85, DataBuffer, (byte)8);
          }
          else
        	  
              send_command((byte)0x85, DataBuffer, (byte)2);

                      
          

          if (get_response())
          {
        	  DataBuffer  = MyBuffer;
				Countm=Count;
					Response=MyBuffer;
              get_data( Response, Countm);       
              
              if (Response[0] == 0x4C)
            	  
            	  
                  return true;
              else
              {
                  ReturnCode = Response[0];
                  return false;
              }


          }
          else
              return false;
      }
      
      public boolean CMD_Halt() throws IOException
      {

          byte[] Response = new byte[24];
          byte[] DataBuffer = new byte[20];
          byte Countm = 0;
          byte ReturnCode = 0;

          
          send_command((byte)0x93, DataBuffer,(byte) 0); 


          if (get_response())
          {
        	  DataBuffer  = MyBuffer;
				Countm=Count;
					Response=MyBuffer;
              get_data( Response, Countm);        

              if (Response[0] == 0x4C)
                  return true;
              else
              {
                  ReturnCode = Response[0];
                  return false;
              }


          }
          else
              return false;
      }

      
      public byte[] CMD_ReadBlock(byte BlockNo) throws IOException
      {
    	  byte[] c = new byte[16];
          byte[] Response = new byte[24];
          byte[] DataBuffer = new byte[20];
          byte[] BlockData = new byte[16];
          byte Countn = 0;
          byte i = 0;
          
          
          ReturnCode = 0;  

          DataBuffer[0] = BlockNo;
          

          send_command((byte)0x86, DataBuffer, (byte)1); 


          
          if (get_response())
          {
        	  DataBuffer  = MyBuffer;
				Countn=Count;
					Response=MyBuffer;
        	  
              get_data( Response, Countn);         
              if (Count <= 1) 
              {
            	  
                  ReturnCode = Response[0];
                  return c;

              }
              else
              {	/*String s = hexString(BlockData);
              System.out.print(s );*/
                  for (i = 0; i < 16; i++)
                  {
                	  
                      BlockData[i] = Response[i + 1];
                      byte[] a = new byte[16];
                      /*a[i]=BlockData[i];
                      System.out.print("b = " + BlockData[i] + " \n " );
                      System.out.print("a = " + a[i] );*/
                      ReturnCode = Response[0];
                     
                  }
                  return BlockData;
              }


          }
          else
              return c;
      }
      	
    
      @SuppressWarnings("unused")
	private boolean check_if_sector_trailer(byte BlockNo)
      {
          
          int remain = 0;
          int temp = 0;

          if (BlockNo <= 128)
          {
              temp = BlockNo;
              temp++;
              remain = temp % 4;

              if (remain == 0)
              {
                  return false;

              }
          }
          else
          {
              temp = BlockNo;
              temp++;
              remain = temp % 16;

              if (remain == 0)
              {
                  return false;

              }
          
          }

          return true;

      
      }

      
      public boolean CMD_WriteBlock(byte BlockNo,byte[] BlockData) throws IOException
      {

          byte[] Response = new byte[24];
          byte[] DataBuffer = new byte[20];
          byte Countm = 0;
          byte i = 0;


          ReturnCode = 0;  

          if (!check_if_sector_trailer(BlockNo))
          {
              ReturnCode = 1; 
              return false;

          }




          DataBuffer[0] = BlockNo;

          for(i=0;i<16;i++)
          DataBuffer[i+1] = BlockData[i];
          send_command((byte)0x89, DataBuffer, (byte)17); 

          if (get_response())
          {
        	  DataBuffer  = MyBuffer;
				Countm=Count;
					Response=MyBuffer;
              get_data(Response,Countm);        

              if (Count <= 1)
              {

                  ReturnCode = Response[0];
                  return false;

              }
              else
              {

                  return true;
              }


          }
          else
              return false;
      }

      public  byte[] GetBytes(long value)
      {
          ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder());
          buffer.putLong(value);
          return buffer.array();
      }
      
      public boolean CMD_ReadValue(byte BlockNo) throws IOException
      {

          byte[] Response = new byte[24];
          byte[] DataBuffer = new byte[20];
          byte Countm = 0;
          
          long Value = 0;
          byte ReturnCode = 0;  

          DataBuffer[0] = BlockNo;


          send_command((byte)0x87, DataBuffer, (byte)1); 



          if (get_response())
          {
        	  DataBuffer  = MyBuffer;
				Countm=Count;
					Response=MyBuffer;
              get_data(Response,Countm);      

              if (Count < 5) 
              {

                  ReturnCode = Response[0];
                  return false;

              }
              else
              {
                  Value = Long.parseLong(Response.toString());
                		  
                  return true;

              }


          }
          else
              return false;
      }

      
      public boolean CMD_WriteValue(byte BlockNo,long Value) throws IOException
      {

          byte[] Response = new byte[24];
          byte[] DataBuffer = new byte[20];
          byte[] ValueArray = new byte[8];
          byte Countm = 0;
          byte i=0;

         byte ReturnCode = 0;  

         if (!check_if_sector_trailer(BlockNo))
          {
              ReturnCode = 1; 
              return false;

          }

          DataBuffer[0] = BlockNo;
          ValueArray = GetBytes(Value);

          for (i = 0; i < 4; i++)
              DataBuffer[i + 1] = ValueArray[i];

          send_command((byte)0x8A, DataBuffer, (byte)5); 

          if (get_response())
          {
        	  
        	  DataBuffer  = MyBuffer;
				Countm=Count;
					Response=MyBuffer;
              get_data(Response, Count);  
       
              if (Count < 5) 
              {

                  ReturnCode = Response[0];
                  return false;
              }
              else
              {
                  return true;
              }


          }
          else
              return false;
      }

      public boolean CMD_IncrementValue(byte BlockNo, byte IncValue) throws IOException
      {

          byte[] Response = new byte[24];
          byte[] DataBuffer = new byte[20];
          byte[] ValueArray = new byte[8];
          byte Countm = 0;
          byte i = 0;

          long NewValue = 0;
          byte ReturnCode = 0;  
          DataBuffer[0] = BlockNo;
          ValueArray = GetBytes(IncValue);

          for (i = 0; i < 4; i++)
          DataBuffer[i + 1] = ValueArray[i];


          send_command((byte)0x8D, DataBuffer,(byte) 5); 


          if (get_response())
          {
        	  DataBuffer  = MyBuffer;
				Countm=Count;
					Response=MyBuffer;
              get_data( Response, Countm);         

              if (Count < 5) 
              {

                  ReturnCode = Response[0];
                  return false;

              }
              else
              {
                  NewValue = Long.parseLong(Response.toString());
                  return true;
              }


          }
          else
              return false;
      }    
      

      public boolean CMD_DecrementValue(byte BlockNo, long DecValue) throws IOException
      {

          byte[] Response = new byte[24];
          byte[] DataBuffer = new byte[20];
          byte[] ValueArray = new byte[8];
          byte Countm = 0;
          byte i = 0;

          long NewValue = 0;
          byte ReturnCode = 0;  
          DataBuffer[0] = BlockNo;
          ValueArray = GetBytes(DecValue);

          for (i = 0; i < 4; i++)
              DataBuffer[i + 1] = ValueArray[i];


          send_command((byte)0x8E, DataBuffer,(byte)5); 



          if (get_response())
          {
        	  DataBuffer  = MyBuffer;
				Countm=Count;
					Response=MyBuffer;
              get_data( Response,  Countm);    
    
              if (Count < 5) 
              {
                  ReturnCode = Response[0];
                  return false;
              }
              else
              {
                  NewValue = Long.parseLong(Response.toString());
                  return true;
              }


          }
          else
              return false;
      }



      public boolean CMD_SwitchRF(boolean OnOffState) throws IOException
      {

          byte[] Response = new byte[24];
          byte[] DataBuffer = new byte[20];
          byte Countm = 0;
          
          byte ReturnCode = 0;  

          if (OnOffState)
              DataBuffer[0] = (byte) 0xFF;
          else
              DataBuffer[0] = 0;
          

          send_command((byte)0x90, DataBuffer,(byte) 1); 



          if (get_response())
          {
        	  DataBuffer  = MyBuffer;
				Countm=Count;
					Response=MyBuffer;
              get_data( Response,  Countm);          

              if (Count == 1)
              {
                  if ((Response[0]==0) || (Response[0]==1))
                  return true;
                  else
                  {
                      ReturnCode = Response[0];
                      return false;
                  }


                                     
              }
              else
              {
                  ReturnCode = Response[0];
                  return false;

              }


          }
          else
              return false;
      }


      public  String hexString( byte[] buf ) {
          char[] TAB_BYTE_HEX = { '0', '1', '2', '3', '4', '5', '6','7',
                                  '8', '9', 'A', 'B', 'C', 'D', 'E','F' };
   
          StringBuffer sb = new StringBuffer( buf.length*2 );
   
          for ( int i=0; i<buf.length; i++ ) {
              sb.append( TAB_BYTE_HEX[(buf[i]>>>4) & 0xF] );
              sb.append( TAB_BYTE_HEX[ buf[i]      & 0x0F] );
          }
          return sb.toString();
     }
 

      private String encode2(String password)
      {
          byte[] uniqueKey = password.getBytes();
          byte[] hash      = null;

          try
          {
              hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
          }
          catch (NoSuchAlgorithmException e)
          {
              throw new Error("No MD5 support in this VM.");
          }

          StringBuilder hashString = new StringBuilder();
          for (int i = 0; i < hash.length; i++)
          {
              String hex = Integer.toHexString(hash[i]);
              if (hex.length() == 1)
              {
                  hashString.append('0');
                  hashString.append(hex.charAt(hex.length() - 1));
              }
              else
                  hashString.append(hex.substring(hex.length() - 2));
          }
          return hashString.toString();
      }

  
      public void writeHashKey(String fileName, String text)
		{

			String adressedufichier = System.getProperty("user.dir") + "/"+ nomFic;
		

			try
			{
				
				FileWriter fw = new FileWriter(adressedufichier, true);
				BufferedWriter output = new BufferedWriter(fw);
				output.write(texte);
				output.flush();
				output.close();
				System.out.println("file created");
			}
			catch(IOException ioe){
				System.out.print("Error : ");
				ioe.printStackTrace();
				}

		}
      
          public void ParseIncoming()
          {
              byte[] Response = new byte[24];
              byte Countm = 0;
              byte i = 0;
              
              TagSerial = new byte[4];
              String Firmware = "";
              byte Response_Type = 0;
              byte TagType = 0;



              if (get_response())
              {
            	  
		Countm=Count;
		Response=MyBuffer;
                  if (Response[3] == 0x81)  
                  {
                	
                      get_data(Response, Countm);        
                      for (i = 0; i < Countm; i++)
                    	  Firmware += Response[i];

                      Response_Type = 1;
                      
                  }
                  else if (Response[3] == 0x82)
                  {
                      get_data(Response,Count);      
                      TagType = Response[0];                       
                      TagSerial[0] = Response[4];    
                      TagSerial[1] = Response[3];
                      TagSerial[2] = Response[2];
                      TagSerial[3] = Response[1];

                      Response_Type = 2;
                     
                  }

              }
          



          }
       
          
          
          
          
