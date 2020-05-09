package com.zeljic.wdtp;

import com.google.gson.*;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Handler implements WebSocket.Listener
{
	private WebSocket ws;
	private CountDownLatch counter = new CountDownLatch(1);
	private StringBuilder stringBuilder = new StringBuilder();
	private int waitingId;
	private JsonObject lastJsonObject = new JsonObject();

	private final Gson gson = new GsonBuilder()
			.disableHtmlEscaping()
			.registerTypeHierarchyAdapter(Number.class, (JsonSerializer<Number>) (src, typeOfSrc, context) -> new JsonPrimitive(src))
			.create();

	@Override
	public void onOpen(WebSocket webSocket)
	{
		System.out.println("ws open");
	}

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last)
	{
		this.stringBuilder.append(data);

		if (last)
		{
			String message = this.stringBuilder.toString();

			System.out.println(String.format("<<< %s", message));

			this.lastJsonObject = gson.fromJson(message, JsonObject.class);

			this.stringBuilder = new StringBuilder();


			if (this.lastJsonObject.has("id") && this.lastJsonObject.get("id").getAsInt() == this.waitingId)
			{
				this.counter.countDown();
			}
		}

		return WebSocket.Listener.super.onText(webSocket, data, last);
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error)
	{
		error.printStackTrace();
	}

	@Override
	public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last)
	{
		System.out.println("on binary");

		return WebSocket.Listener.super.onBinary(webSocket, data, last);
	}

	public void setWebSocket(WebSocket ws)
	{
		this.ws = ws;
	}

	private JsonObject execute(int id, String request)
	{
		this.waitingId = id;
		this.counter = new CountDownLatch(1);

		System.out.println(String.format(">>> %s", request));
		this.ws.sendText(request, true);

		try
		{
			this.counter.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		return this.lastJsonObject;
	}

	private JsonObject execute(int id, JsonObject request)
	{
		return this.execute(id, gson.toJson(request));
	}

	public JsonObject createTarget(int id, String url)
	{
		JsonObject params = new JsonObject();
		params.addProperty("url", url);

		JsonObject request = new JsonObject();
		request.addProperty("id", id);
		request.addProperty("method", "Target.createTarget");
		request.add("params", params);

		return this.execute(id, request);
	}

	public JsonObject attachToTarget(int id, String targetId)
	{
		JsonObject params = new JsonObject();
		params.addProperty("targetId", targetId);
		params.addProperty("flatten", true);

		JsonObject request = new JsonObject();
		request.addProperty("id", id);
		request.addProperty("method", "Target.attachToTarget");
		request.add("params", params);

		return this.execute(id, request);
	}

	public JsonObject evaluate(int id, String sessionId, String expression)
	{
		JsonObject params = new JsonObject();
		params.addProperty("expression", expression);
		params.addProperty("returnByValue", true);
		params.addProperty("awaitPromise", true);
		params.addProperty("userGesture", true);

		JsonObject request = new JsonObject();
		request.addProperty("id", id);
		request.addProperty("method", "Runtime.evaluate");
		request.addProperty("sessionId", sessionId);
		request.add("params", params);

		return this.execute(id, request);
	}

	public static Optional<JsonObject> getResult(JsonObject response)
	{
		if (response.has("result"))
		{
			var result = response.getAsJsonObject("result");

			return Optional.ofNullable(result);
		}

		return Optional.empty();
	}
}
