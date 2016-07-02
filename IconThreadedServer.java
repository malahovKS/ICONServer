/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerPackage;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;



//для работы с dll
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
//import java.nio.channels.FileChannel;

// For Measure Execution Time with Guava Stopwatch
import com.google.common.base.Stopwatch;


/**
 *
 * @author kirilmalahov
 */
public class IconThreadedServer {

    public static void main(String[] args) {
    
                //Date Time
                //Calendar calendarKiev = Calendar.getInstance(TimeZone.getTimeZone("Europe/Kiev"));
                //Calendar calendarLocal = Calendar.getInstance(TimeZone.getDefault());
       
        

        ServerSocket serverSocketConnection = null;
        int cliConNumber = 5;

        try {
            int i = 1;

            // serverSocketConnection = new ServerSocket(8189, 5);
            // 23152 --> ssh
            // 24152 --> 6379
            // 25152 --> 8189
            // 8888  --> 8888
            // 548   --> 548

            if (args.length == 0) {
                serverSocketConnection = new ServerSocket(8189, 100);
                cliConNumber = 100;
            } else {

                if (args.length == 2) {
                    serverSocketConnection = new ServerSocket(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                    cliConNumber = Integer.parseInt(args[1]);
                }
            }


            while (true) {
                System.out.println("Only " + cliConNumber + " clients connection pissible");
                System.out.println("Waiting for connection on " + serverSocketConnection.getLocalPort() + " port");

                Socket incoming = serverSocketConnection.accept();
                System.out.println("Spawning connections: " + i);


                //Date Time
                //System.out.println("Europe/Kiev date/time ==>>  " + calendarKiev.getTime());
                //System.out.println("Local date/time ==>>  " + calendarLocal.getTime());
                System.out.println("Local date/time ==>>  " + new Date(System.currentTimeMillis()));
               
               

                System.out.println("Client Address: " + incoming.getInetAddress().getHostAddress());
                //System.out.println("Host Name: " + incoming.getInetAddress().getCanonicalHostName());
                System.out.println("Client Port: " + incoming.getPort());


                Runnable r = new ThreadedHandler(incoming, i);
                Thread t = new Thread(r);
                t.start();
                i++;
            }
        } catch (IOException ex) {
            Logger.getLogger(IconThreadedServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
class ThreadedHandler implements Runnable {

    Socket s = null;

    //Интерфейс и класс инициализации работы KonspektLib.dll
    public interface KonspektLib extends Library {

        public boolean exportTerms(String filename, String termsfname, int TermsWordLevel, int TermsWordGroupr, boolean TermsFromCompleteSent, int TermsFreq);
    }

    public void analyzeText(String in_filename, String out_filename) {


        //KonspektLib lib = (KonspektLib) Native.loadLibrary(System.getProperty("user.dir")+"/KonspektLib.dll", KonspektLib.class);
        KonspektLib lib = (KonspektLib) Native.loadLibrary("KonspektLib.dll", KonspektLib.class);
        lib.exportTerms(in_filename, out_filename, 99, 99, false, 0);

    }

    public void analyzeTextThreded(String inFilename, String outFilename, String pathToLibrary) {


        //KonspektLib lib = (KonspektLib) Native.loadLibrary(System.getProperty("user.dir")+"/KonspektLib.dll", KonspektLib.class);
        KonspektLib lib = (KonspektLib) Native.loadLibrary(pathToLibrary, KonspektLib.class);
        lib.exportTerms(inFilename, outFilename, 99, 99, false, 0);

    }

    
    public static void copyFolder(File srcFolderPath, File destFolderPath)
            throws IOException {

        if (!srcFolderPath.isDirectory()) {

            // If it is a File the Just copy It to the new Folder
            InputStream in = new FileInputStream(srcFolderPath);
            OutputStream out = new FileOutputStream(destFolderPath);

            byte[] buffer = new byte[1024];

            int length;

            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
            System.out.println("File copied from " + srcFolderPath + " to "
                    + destFolderPath + " successfully");

        } else {

            // if it is a directory create the directory inside the new destination directory and
            // list the contents...

            if (!destFolderPath.exists()) {
                destFolderPath.mkdir();
                System.out.println("Directory copied from " + srcFolderPath
                        + "  to " + destFolderPath + " successfully");
            }

            String folder_contents[] = srcFolderPath.list();

            for (String file : folder_contents) {

                File srcFile = new File(srcFolderPath, file);
                File destFile = new File(destFolderPath, file);

                copyFolder(srcFile, destFile);
            }

        }
    }

    public static void copyFile(String fromFileName, String toFileName)
            throws IOException {
        File fromFile = new File(fromFileName);
        File toFile = new File(toFileName);

        if (!fromFile.exists()) {
            throw new IOException("FileCopy: " + "no such source file: "
                    + fromFileName);
        }
        if (!fromFile.isFile()) {
            throw new IOException("FileCopy: " + "can't copy directory: "
                    + fromFileName);
        }
        if (!fromFile.canRead()) {
            throw new IOException("FileCopy: " + "source file is unreadable: "
                    + fromFileName);
        }

        if (toFile.isDirectory()) {
            toFile = new File(toFile, fromFile.getName());
        }

        if (toFile.exists()) {
            if (!toFile.canWrite()) {
                throw new IOException("FileCopy: "
                        + "destination file is unwriteable: " + toFileName);
            }
            System.out.print("Overwrite existing file " + toFile.getName()
                    + "? (Y/N): ");
            System.out.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    System.in));

            String response = "Y";   //!!!!!!!!!! позволяет не участвовать пользователю
            //String response = in.readLine();
            if (!response.equals("Y") && !response.equals("y")) {
                throw new IOException("FileCopy: "
                        + "existing file was not overwritten.");
            }
        } else {
            String parent = toFile.getParent();
            if (parent == null) {
                parent = System.getProperty("user.dir");
            }
            File dir = new File(parent);
            if (!dir.exists()) {
                throw new IOException("FileCopy: "
                        + "destination directory doesn't exist: " + parent);
            }
            if (dir.isFile()) {
                throw new IOException("FileCopy: "
                        + "destination is not a directory: " + parent);
            }
            if (!dir.canWrite()) {
                throw new IOException("FileCopy: "
                        + "destination directory is unwriteable: " + parent);
            }
        }

        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead); // write
            }
        } finally {
            if (from != null) {
                try {
                    from.close();
                } catch (IOException e) {
                    ;
                }
            }
            if (to != null) {
                try {
                    to.close();
                } catch (IOException e) {
                    ;
                }
            }
        }
    }

//Copy File    
    public ThreadedHandler(Socket Sckt, int Cntr) {

        incoming = Sckt;
        counter = Cntr;
    }

    // Get bytes from stream. Get file in byte array from stream
    public byte[] readBytes() throws IOException {
        // Again, probably better to store these objects references in the support class
        InputStream in = incoming.getInputStream();
        DataInputStream dis = new DataInputStream(in);

        int len = dis.readInt();
        //byte[] data = new byte[dis.available()];
        byte[] data = new byte[len];
        if (len > 0) {
            dis.readFully(data);
        }
        return data;
    }
    // Get bytes from stream. Get file in byte array from stream

    // Send file
    public void sendBytes(byte[] myByteArray, int start, int len) throws IOException {
        if (len < 0) {
            throw new IllegalArgumentException("Negative length not allowed");
        }
        if (start < 0 || start >= myByteArray.length) {
            throw new IndexOutOfBoundsException("Out of bounds: " + start);
        }
       

        OutputStream out = incoming.getOutputStream();
        DataOutputStream dos = new DataOutputStream(out);

        dos.writeInt(len);
        if (len > 0) {
            dos.write(myByteArray, start, len);
        }
    }

    public void sendBytes(byte[] myByteArray) throws IOException {
        sendBytes(myByteArray, 0, myByteArray.length);
    }
    // Send file

    public void run() {


        // turn off/on konspekt.dll
        //
        //
        boolean fake = false;
        boolean dll = false;
        boolean exe = true;



        // File mapping
        //
        //
        //
        //


        if (fake) {

            // fake mode
            //
            //
            //
            // 




            FileOutputStream fOut;
            try {
                // Open output file.
                fOut = new FileOutputStream("Recieve_" + counter + ".txt");
            } catch (FileNotFoundException exc) {
                System.out.println("Error Opening Output File");
                return;
            }
            try {
                fOut.write(readBytes());

            } catch (IOException ex) {
                Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                fOut.close();
            } catch (IOException ex) {
                Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
            }


            FileInputStream fIn;


            File f = new File("termsexport.tt");
            try {
                fIn = new FileInputStream(f);
            } catch (FileNotFoundException exc) {
                System.out.println("File Not Found");
                return;
            }

            byte fileContent[] = new byte[(int) f.length()];
            try {
                fIn.read(fileContent);
            } catch (IOException ex) {
                //  Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                fIn.close();
            } catch (IOException ex) {
                Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                sendBytes(fileContent);
            } catch (IOException ex) {
                Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.println("Sending  " + "Recieve_" + counter + ".tt" + " Done");

        }


        if (dll) {





            // True mode with dll
            //
            //
            //
            // 


            FileOutputStream fOut;
            try {
                // Open output file.
                fOut = new FileOutputStream("Recieve_" + counter + ".txt");
            } catch (FileNotFoundException exc) {
                System.out.println("Error Opening Output File");
                return;
            }
            try {
                fOut.write(readBytes());

            } catch (IOException ex) {
                Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                fOut.close();
            } catch (IOException ex) {
                Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
            }



            System.out.println("dll run");



            File dir = new File(System.getProperty("user.dir") + "/Recieve_" + counter);
            dir.mkdir();

            try {
                copyFile("KonspektLib.dll", System.getProperty("user.dir") + "/Recieve_" + counter + "/KonspektLib.dll");
            } catch (IOException ex) {
                Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
            }


            analyzeTextThreded("Recieve_" + counter + ".txt", "Recieve_" + counter + ".tt", System.getProperty("user.dir") + "/Recieve_" + counter + "/KonspektLib.dll");





            //TextAnalysis("Recieve_" + counter + ".txt", "Recieve_" + counter + ".tt");

            System.out.println("dll done");

            System.out.println("Sending  " + "Recieve_" + counter + ".tt");

            FileInputStream fIn;
            // First make sure that a file has been specified
            // on the command line.

            // Now, open the file.
            File f = new File("Recieve_" + counter + ".tt");
            try {
                fIn = new FileInputStream(f);
            } catch (FileNotFoundException exc) {
                System.out.println("File Not Found");
                return;
            }

            byte fileContent[] = new byte[(int) f.length()];
            try {
                fIn.read(fileContent);
            } catch (IOException ex) {
                //  Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                fIn.close();
            } catch (IOException ex) {
                Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
            }




            try {
                sendBytes(fileContent);
            } catch (IOException ex) {
                Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.println("Sending  " + "Recieve_" + counter + ".tt" + " Done");
        }



        if (exe) {


            Stopwatch stopwatch = Stopwatch.createStarted(); //Measure Execution Time with Guava Stopwatch            
            
            // True mode with exe
            //
            //
            //
            // 

            System.out.println("Prepare files and folders to start Konspektvv.exe");



            File dir = new File(System.getProperty("user.dir") + "/Recieve_" + counter);
            dir.mkdir();



            FileOutputStream fOut;
            try {
                // Open output file.
                fOut = new FileOutputStream(System.getProperty("user.dir") + "/Recieve_" + counter + "/Recieve_" + counter + ".txt");
            } catch (FileNotFoundException exc) {
                System.out.println("Error Opening Output File");
                return;
            }
            try {
                fOut.write(readBytes());

            } catch (IOException ex) {
                Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                fOut.close();
            } catch (IOException ex) {
                Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
            }







            File srcFolderPath = new File("Console");
            File destFolderPath = new File(System.getProperty("user.dir") + "/Recieve_" + counter);


            if (!srcFolderPath.exists()) {

                System.out.println("Directory does not exist. Will exit now");
                System.exit(0);

            } else {

                try {
                    copyFolder(srcFolderPath, destFolderPath);

                    System.out.println("Directory coping from " + srcFolderPath + "  to "
                            + destFolderPath + " was finished successfully");

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }



            
            
            //TextAnalysisThreded("Recieve_" + counter + ".txt", "Recieve_" + counter + ".tt", System.getProperty("user.dir") + "/Recieve_" + counter + "/KonspektLib.dll");

            System.out.println("Start Konspektvv.exe under WINE");
            Runtime r1 = Runtime.getRuntime();
            Process p1 = null;
            try {
                p1 = r1.exec("env LC_ALL=ru_RU.CP1251 wine " + System.getProperty("user.dir") + "/Recieve_" + counter + "/Konspektvv.exe " + System.getProperty("user.dir") + "/Recieve_" + counter + "/Recieve_" + counter + ".txt");
            } catch (IOException ex) {
            }
            try {
                p1.waitFor(); 
            } catch (InterruptedException ex) {
            }


            
            
            

            System.out.println("Konspektvv.exe done");

            System.out.println("Try to Send  " + "allterms.xml");

            FileInputStream fIn;
            
            // First make sure that a file has been specified
            // on the command line.

            
            
            // Now, open the file.
            File f = new File(System.getProperty("user.dir") + "/Recieve_" + counter + "/allterms.xml");
            try {
                fIn = new FileInputStream(f);
            } catch (FileNotFoundException exc) {
                
                System.out.println("File Not Found");
                

                ///////////
                ///Need send message to client
                ///////////
                
                System.out.println(" ");
                System.out.println("Sending report about fail to client");
                
                BufferedWriter writerLog = null;

                String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                File logFile = new File(System.getProperty("user.dir") + "/Recieve_" + counter + "/" + timeLog);
                
                try {
                    System.out.println(logFile.getCanonicalPath());
                } catch (IOException ex) {
                    Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    writerLog = new BufferedWriter(new FileWriter(logFile));
                } catch (IOException ex) {
                    Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    writerLog.write("File can't be analized!!!  ");
                    writerLog.write("Check file encoding. File encoding must be CP1251   ");
                    writerLog.write(" Or file too large");
                } catch (IOException ex) {
                    Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    writerLog.close();
                } catch (IOException ex) {
                    Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                byte fileContent[] = new byte[(int) logFile.length()];
                
                
                try {
                FileInputStream finLog = new FileInputStream(logFile);
                
                  
                finLog.read(fileContent);
           
                finLog.close();
            } catch (IOException ex) {
                Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
                
              try {
                sendBytes(fileContent);
            } catch (IOException ex) {
                Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
            }  
                
              
              System.out.println(" ");
              System.out.println("Report sent");
              
           ///////////
                ///Need send message to client
           ///////////    
              
                return;
            }

            System.out.println("Sending  " + "allterms.xml");

            byte fileContent[] = new byte[(int) f.length()];
            try {
                fIn.read(fileContent);
            } catch (IOException ex) {
                //  Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                fIn.close();
            } catch (IOException ex) {
                Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
            }



            try {
                sendBytes(fileContent);
            } catch (IOException ex) {
                Logger.getLogger(ThreadedHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.println("Sending  " + "Recieve_" + counter + ".tt" + " Successful");
           
            
            
            //Measure Execution Time with Guava Stopwatch
            stopwatch.stop();
            System.out.println(" Execution time: " + stopwatch);
        }





        System.out.println("Sending  Done");

    }
    private Socket incoming;
    private int counter;
}
