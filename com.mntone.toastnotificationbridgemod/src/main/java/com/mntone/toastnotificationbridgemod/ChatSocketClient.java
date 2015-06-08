package com.mntone.toastnotificationbridgemod;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;

public class ChatSocketClient
{
	public static final int DEFAULT_SERVER_PORT = 4123;

	private static final int RETRY_COUNT = 3;
	private static final Charset CONNECTION_CHARSET = Charset.forName("UTF-8");

	private boolean _enabled = false;
	private int _port = DEFAULT_SERVER_PORT;
	private Socket _socket = null;
	private OutputStreamWriter _writer = null;

	public ChatSocketClient()
	{
	}

	private void connect()
	{
		if (this._enabled)
		{
			return;
		}

		this.close();

		final InetAddress localHost = InetAddress.getLoopbackAddress();
		final InetSocketAddress endPointHost = new InetSocketAddress(localHost, this._port);
		try
		{
			this._socket = new Socket();
			this._socket.setTcpNoDelay(true);
			this._socket.setKeepAlive(true);
			this._socket.connect(endPointHost);
			this._enabled = true;
			this._writer = new OutputStreamWriter(this._socket.getOutputStream(), CONNECTION_CHARSET);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void close()
	{
		if (this._enabled)
		{
			try
			{
				this._socket.close();
				this._socket = null;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void send(@Nonnull final String jsonText)
	{
		this.sendPrivate(jsonText, RETRY_COUNT);
	}

	private void sendPrivate(@Nonnull final String jsonText, int retryCount)
	{
		if (this.checkConnection())
		{
			try
			{
				this._writer.write(jsonText);
				this._writer.flush();
			}
			catch (final SocketException e)
			{
				e.printStackTrace();

				this._enabled = false;
				if (--retryCount != 0)
				{
					this.sendPrivate(jsonText, retryCount);
				}
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private boolean checkConnection()
	{
		int count = 0;
		while (!this._enabled)
		{
			this.connect();
			++count;
			if (count == RETRY_COUNT)
			{
				return false;
			}
		}
		return true;
	}
}