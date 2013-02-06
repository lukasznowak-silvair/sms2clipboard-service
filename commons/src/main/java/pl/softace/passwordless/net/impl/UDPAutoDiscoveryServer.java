package pl.softace.passwordless.net.impl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.apache.log4j.Logger;

import pl.softace.passwordless.net.Command;
import pl.softace.passwordless.net.IAutoDiscoveryServer;

/**
 * 
 * {@link IAutoDiscoveryServer} implementation based on UDP multicasts.
 * 
 * @author lkawon@gmail.com
 *
 */
public class UDPAutoDiscoveryServer extends Thread implements IAutoDiscoveryServer {

	/**
	 * Log4j logger.
	 */
	private static final Logger LOG = Logger.getLogger(UDPAutoDiscoveryServer.class);
	
	/**
	 * Listening port.
	 */
	private int port = 1900;
	
	/**
	 * Multicast socket.
	 */
	private MulticastSocket socket;
	
	/**
	 * Multicast group.
	 */
	private String multicastGroup = "225.1.1.1";
	
	/**
	 * Flag indicating that the thread is running.
	 */
	private boolean isRunning; 
	
	
	/**
	 * Default constructor.
	 */
	public UDPAutoDiscoveryServer() {
		
	}
	
	public final int getPort() {
		return port;
	}

	public final void setPort(int port) {
		this.port = port;
	}

	public final String getMulticastGroup() {
		return multicastGroup;
	}

	public final void setMulticastGroup(String multicastGroup) {
		this.multicastGroup = multicastGroup;
	}

	/* (non-Javadoc)
	 * @see pl.softace.passwordless.net.IAutoDiscoveryServer#startServer()
	 */
	@Override
	public final void startServer() {
		try {
			socket = new MulticastSocket(port);
			socket.joinGroup(InetAddress.getByName(multicastGroup));
		} catch (IOException e) {
			LOG.error("Exception occured.", e);
		}
		
		isRunning = true;
		start();		
	}

	/* (non-Javadoc)
	 * @see pl.softace.passwordless.net.IAutoDiscoveryServer#stopServer()
	 */
	@Override
	public final void stopServer() {
		try {
			socket.leaveGroup(InetAddress.getByName(multicastGroup));
			socket.close();
		} catch (IOException e) {
			LOG.error("Exception occured.", e);
		}
		
		
		try {
			join();
		} catch (InterruptedException e) {
			LOG.error("Exception occured.", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public final void run() {
		LOG.debug("UDP Auto Discovery server started.");
		
		try {					
			while (isRunning) {
				// listening for datagram
				byte buffer[] = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				
				String receivedData = new String(packet.getData());								
				if (receivedData.contains(Command.SEARCH_COMMAND.getData())) {
					LOG.debug("Received search packet from " + packet.getAddress() + ".");
					
					String responseData = new String(Command.RESPONSE_COMMAND.getData());
					byte[] responseBuffer = responseData.getBytes();
					DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length, 
							InetAddress.getByName(multicastGroup), port);
					socket.send(responsePacket);
					
					LOG.debug("Response packet sent.");
				}			
				
				
				sleep(1);				
			}									
		} catch (IOException e) {
			LOG.error("Exception occured.", e);
		} catch (InterruptedException e) {
			LOG.error("Exception occured.", e);
		}
		
		LOG.debug("UDP Auto Discovery server stopped.");
	}
}
