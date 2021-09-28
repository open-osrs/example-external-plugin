package com.tha23rd.eventCollector.client;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class RsServiceClient
{
	private static RsServiceClient client;
	private final OkHttpClient httpClient;
	private final String BASE_URL;

	private RsServiceClient(String baseUrl) {
		this.httpClient = new OkHttpClient();
		this.BASE_URL = baseUrl;
	}

	public static RsServiceClient getClient(String baseUrl) {
		if (client == null)
			client = new RsServiceClient(baseUrl);

		return client;
	}

	public void postEvent(String eventBody) {
		RequestBody body = RequestBody.create(
			MediaType.parse("application/json"), eventBody);

		Request request = new Request.Builder()
			.url(BASE_URL + "/runelite")
			.post(body)
			.build();

		log.info("Sending event to: " + this.BASE_URL + " with contents: " + eventBody);

		httpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				e.printStackTrace();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				response.close();
			}
		});
	}

	public void heartbeat(String playerId) {
		Request request = new Request.Builder()
			.url(BASE_URL + "/heartbeat/" + playerId)
			.post(RequestBody.create(null, new byte[0]))
			.build();

		httpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				e.printStackTrace();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				response.close();
			}
		});
	}

	public void postEvent(String eventBody, boolean debugPrint) {
		if (debugPrint)
		{
			System.out.print(eventBody);
		}
		postEvent(eventBody);
	}
}
