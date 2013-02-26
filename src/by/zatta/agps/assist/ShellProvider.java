package by.zatta.agps.assist;

/*ShellPrivider to get acces to the shell. Obvious, right?.
 *Copyright (C) 2012 insanity-toolbox project
 *
 *This program is free software: you can redistribute it and/or modify
 *it under the terms of the GNU General Public License as published by
 *the Free Software Foundation, either version 3 of the License, or
 *(at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import by.zatta.agps.BaseActivity;

import android.util.Log;

public enum ShellProvider {
	INSTANCE;

	/**
	 * 
	 */
	private static final String EOF_MARK = "s1UZA1BJt9rWWGF1tYFh";

	private class WorkerThread extends Thread {
		@Override
		protected void finalize() throws Throwable {
			process.destroy();
			this.interrupt();
			super.finalize();
		}

		static final String NO_MORE_WORK = "TERM";
		private static final String TAG = "ShellThread";
		BufferedReader output_reader;
		private Process process;
		private BlockingQueue<String> command_queue;
		private ArrayBlockingQueue<String> output_queue;
		private DataOutputStream stdin;
		private DataInputStream stdout;

		WorkerThread(BlockingQueue<String> command_queue,
				ArrayBlockingQueue<String> output_queue) {
			this.command_queue = command_queue;
			this.output_queue = output_queue;
		}

		private void init() {
			try {
				ProcessBuilder pb = new ProcessBuilder("su");
				pb.redirectErrorStream(true);
				process = pb.start();
				// process = Runtime.getRuntime().exec("su");
				stdin = new DataOutputStream(process.getOutputStream());
				stdout = new DataInputStream(process.getInputStream());
				output_reader = new BufferedReader(
						new InputStreamReader(stdout));

			} catch (Exception e) {
				Log.d("ShellProvider", "Couldnt start shell process" + e.toString());
				e.printStackTrace();

			}
		}

		@Override
		public void run() {

			try {
				while (true) {
					String command = command_queue.take();
					if (ensure_process()){
					if (command.equals(NO_MORE_WORK)) {
						command_queue = null;
						output_queue = null;
						stdin.writeBytes("exit \n");
						stdin.flush();
						stdin.close();
						stdout.close();
						output_reader.close();
						process.destroy();
						break;
					}
						stdin.writeBytes(command + "\n" + "echo \"" + EOF_MARK
								+ "\" \n");
						stdin.flush();

						StringBuilder sb = new StringBuilder();
						String line;
						while (((line = output_reader.readLine()) != null)
								&& !line.contains(EOF_MARK)) {
							if (!line.equals(""))
								sb.append(line + " ");
						}
						output_queue.put(sb.toString());
					
				}
					else output_queue.put("nosu");
				}
			}

			catch (IOException e) {
				Log.d("ShellProvider", "init?");
				e.printStackTrace();
			} catch (InterruptedException e) {
				Log.d("ShellProvider", "waited enough?");
				e.printStackTrace();
			}
		}

		public boolean ensure_process() {
			try {

				if (process == null) {
					if (BaseActivity.DEBUG)
						Log.d(TAG,
								"No active shell process, initializing...");
					init();
					
				}
				if (process != null)
					process.exitValue();
					init();
				if (process != null)
					process.exitValue();
				

			} catch (IllegalThreadStateException e) {
				if (BaseActivity.DEBUG)
					Log.d(TAG, "Shell process running...");
				return true;
			}
			return false;
		}

	}

	private BlockingQueue<String> command_queue;
	private transient WorkerThread worker;
	private ArrayBlockingQueue<String> output_queue;

	public synchronized void finishWork() {
		try {
			command_queue.put("TERM");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public synchronized String getCommandOutput(String command) {
		if (worker == null || !worker.isAlive()) {
			output_queue = new ArrayBlockingQueue<String>(10);
			command_queue = new ArrayBlockingQueue<String>(10);
			worker = new WorkerThread(command_queue, output_queue);
			worker.setName("ShellThread");
			worker.start();
		}
		String output = "";
		output_queue.clear();
		try {
			command_queue.put(command);

		} catch (InterruptedException e) {
			Log.d("ShellProvider", "Interrupted while command_queue.put(command): " + e.toString());
			e.printStackTrace();
			return null;
		}
		try {
			output = output_queue.take();
		} catch (InterruptedException e2) {
			Log.d("ShellProvider", "Interrupted while output_queue.take(): " + e2.toString());
		}
		if (BaseActivity.DEBUG)
			Log.d("ShellProvider", "get output: " + command + " -> \n" + output);
		return output;
	}

	public synchronized boolean isSuAvailable() {
		return getCommandOutput("id").contains("uid=0");
	}
	
	public synchronized void lockToMyBusybox(String storage_root){
		getCommandOutput("export BB=\"/data/data/by.zatta.agps/files/busybox\"");
		getCommandOutput("export SR=\""+storage_root+"\"");
	}
	
	public synchronized void mountRW(Boolean rw){
		if (rw)
			getCommandOutput("$BB mount | $BB grep \"/system\" | $BB awk '{system(\"$BB mount -o rw,remount -t \"$5\" \"$1\" \"$3\"\")}'");
		else
			getCommandOutput("$BB mount | $BB grep \"/system\" | $BB awk '{system(\"$BB mount -o ro,remount -t \"$5\" \"$1\" \"$3\"\")}'");
		
	}
	
	public synchronized void backup(){
		getCommandOutput("$BB test -d \"$SR/TopNTP\" || $BB mkdir \"$SR/TopNTP\"");
		getCommandOutput("$BB test -e \"$SR/TopNTP/gps.conf.bak\" || $BB cp /system/etc/gps.conf $SR/TopNTP/gps.conf.bak");
		getCommandOutput("$BB test -e \"$SR/TopNTP/gpsconfig.xml\" || $BB cp /system/etc/gps/gpsconfig.xml $SR/TopNTP/gpsconfig.xml.bak");
	}
	
	public synchronized void restore(){
		getCommandOutput("$BB test -e \"$SR/TopNTP/gps.conf.bak\" && $BB rm /system/etc/gps.conf && $BB cp $SR/TopNTP/gps.conf.bak /system/etc/gps.conf && $BB chmod 644 /system/etc/gps.conf");
		getCommandOutput("$BB test -e \"$SR/TopNTP/gpsconfig.xml.bak\" && $BB rm /system/etc/gps/gpsconfig.xml && $BB cp $SR/TopNTP/gpsconfig.xml.bak /system/etc/gps/gpsconfig.xml && $BB chmod 644 /system/etc/gps/gpsconfig.xml");
		
	}
	
	public synchronized void reboot(Boolean reboot){
		if (reboot) getCommandOutput("$BB reboot");
	}
	
	public synchronized void copyConf(){
		getCommandOutput("$BB rm /system/etc/gps.conf");
		getCommandOutput("$BB cat /data/data/by.zatta.agps/files/gps.conf > /system/etc/gps.conf");
		getCommandOutput("$BB chmod 644 /system/etc/gps.conf");
	}
	
	public synchronized void copySSL(Boolean mSSL){
		getCommandOutput("$BB rm /system/etc/SuplRootCert");
		if (mSSL){
			getCommandOutput("$BB cat /data/data/by.zatta.agps/files/SuplRootCert > /system/etc/SuplRootCert");
			getCommandOutput("chmod 644 /system/etc/SuplRootCert");
		}
	}
	
	public synchronized boolean isConfigPresent() {
		getCommandOutput("$BB test -e \"/system/etc/gps/gpsconfig.xml\" && $BB grep -n \"PeriodicTimeOutSec\" /system/etc/gps/gpsconfig.xml");	
		return getCommandOutput("$BB test -e \"/system/etc/gps/gpsconfig.xml\" && echo TRUE").contains("TRUE");

	}
	
	public synchronized void updateXML(String periodicTimeOut){
		if (isConfigPresent()){
			getCommandOutput("$BB sed -i 's/PeriodicTimeOutSec.*/PeriodicTimeOutSec=\"'"+periodicTimeOut+"'\"/' /system/etc/gps/gpsconfig.xml");
			getCommandOutput("$BB grep -n \"PeriodicTimeOutSec\" /system/etc/gps/gpsconfig.xml");		
		}
	}
}
