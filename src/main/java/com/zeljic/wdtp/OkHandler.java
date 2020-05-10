package com.zeljic.wdtp;

import com.google.gson.*;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class OkHandler extends WebSocketListener
{
	private WebSocket ws;
	private CountDownLatch counter = new CountDownLatch(1);
	private int waitingId;
	private JsonObject lastJsonObject = new JsonObject();

	private final Gson gson = new GsonBuilder()
			.disableHtmlEscaping()
			.registerTypeHierarchyAdapter(Number.class, (JsonSerializer<Number>) (src, typeOfSrc, context) -> new JsonPrimitive(src))
			.create();

	@Override
	public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response)
	{
		System.out.println("ws open");
		super.onOpen(webSocket, response);
	}

	@Override
	public void onMessage(@NotNull WebSocket webSocket, @NotNull String text)
	{

		System.out.println(String.format("<<< %s", text));

		this.lastJsonObject = gson.fromJson(text, JsonObject.class);

		if (this.lastJsonObject.has("id") && this.lastJsonObject.get("id").getAsInt() == this.waitingId)
		{
			this.counter.countDown();
		}

		super.onMessage(webSocket, text);
	}

	@Override
	public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes)
	{
		System.out.println("on binary message");
		super.onMessage(webSocket, bytes);
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
		this.ws.send(request);

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
}
