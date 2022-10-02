package bezb_proj;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Arrays;

public class Client {

	public static void main(String[] args) throws Exception {
		
		BufferedReader tin = new BufferedReader(new InputStreamReader(System.in));
		Socket sock = new Socket("localhost",2021);
		BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		PrintWriter out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()),true);
		
		String hes = in.readLine();
		System.out.println("MD5: "+hes);

		final byte[] md5 = decodeUsingBigInteger(hes);
		
		in.close(); out.close(); 
		sock.close();
		
		System.out.println("Unesi duzinu za pogadjanje:");
		final int len = Integer.parseInt(tin.readLine());
		/*System.out.println("Unesi donju granicu:");
		final byte min = (byte) tin.readLine().charAt(0);
		System.out.println("Unesi gornju granicu:");
		final byte max = (byte) tin.readLine().charAt(0)+1;*/
		final byte min = 'a'; final byte max = 'z'+1;
		System.out.println("Unesi broj niti("+Runtime.getRuntime().availableProcessors()+"):");
		final int niti = Integer.parseInt(tin.readLine());
		
		tin.close();
		final int podeok = (max-min)/niti;
		final int mod = (max-min)%niti;
		
		for(int i = 0; i < niti; i++) {
			new Thread(new Runnable() {
				private int br;
				
				public Runnable init(int br) {
					this.br = br;
					return this;
				}
				
				@Override
				public void run() {
					final int brTreda = br;
					MessageDigest hash = null;
					FileWriter fw = null;
					try { hash = MessageDigest.getInstance("MD5"); fw = new FileWriter("thread"+brTreda+".log");} 
					catch (Exception e) {e.printStackTrace();}
					final byte tmin = (byte)(min+(podeok + ((mod>0 && brTreda < mod) ? 1 : 0)) * brTreda);
					final byte tmax =  (byte)(tmin+podeok);
					
					byte[] niz = new byte[1]; 
					niz[0] = (byte) tmin;
					boolean nasao = false;
					System.out.println("Thread["+brTreda+"] Running...");
					long t = System.nanoTime();
			    	for(int i = 0; i < len; ++niz[i]) {
			    		if(niz[i] == max || i == niz.length-1 && niz[i] == tmax) { 
			    			
			    			niz[i] = min;
			    			if(++i == niz.length) {
			    				niz = new byte[i+1];
			    				Arrays.fill(niz,min);
			    				niz[i] = tmin; --niz[i=0];
			    			}
			    			
			    		}
			    		else{ 
			    			try {
								fw.write('[');
								for(int j=0;j<niz.length;j++) {
									fw.write((char)niz[j]+", ");
								}
								fw.write("]\r\n");
							} catch (IOException e) {
								e.printStackTrace();
							}
			    			if(Arrays.equals(hash.digest(niz),md5)) {
			    				nasao=true; break;
			    			} 
			    			i = 0;
			    		}
			    	}
			    	t = System.nanoTime() - t;
			    	double ts = t / 1e9;
			    	System.out.println("Thread["+brTreda+"] Vreme: " + ts+"s");
			    	if(nasao) {
			    		System.out.println("Thread["+brTreda+"] Vrednost je: "+new String(niz));
			    		System.exit(0);
			    	}
			    	else System.out.println("Thread["+brTreda+"] nije nasao :(");
					
				}
			}.init(i)).start();
			
		}
	}
	
	private static byte[] decodeUsingBigInteger(String hexString) throws Exception {
		byte[] byteArray = new BigInteger(hexString, 16).toByteArray();
	    return (byteArray[0] == 0) ? Arrays.copyOfRange(byteArray, 1, byteArray.length) : byteArray;
	}

}
