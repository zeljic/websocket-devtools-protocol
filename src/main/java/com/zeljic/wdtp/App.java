package com.zeljic.wdtp;

import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Optional;

public class App
{
	public static void main(String[] args)
	{
		URI wsUri = URI.create("ws://127.0.0.1:9111/devtools/browser/07666ea7-dff6-4fe2-8e7f-50b49e953733");

		Handler handler = new Handler();

		WebSocket ws = HttpClient
				.newBuilder()
				.connectTimeout(Duration.ofSeconds(30))
				.build()
				.newWebSocketBuilder()
				.connectTimeout(Duration.ofSeconds(30))
				.buildAsync(wsUri, handler)
				.join();

		ws.request(1);

		handler.setWebSocket(ws);

		JsonObject createTarget = handler.createTarget(1, "about:blank");
		Optional<JsonObject> resultCreatTarget = Handler.getResult(createTarget);
		if (resultCreatTarget.isEmpty())
		{
			return;
		}

		String targetId = resultCreatTarget.get().get("targetId").getAsString();

		JsonObject attachToTarget = handler.attachToTarget(2, targetId);

		Optional<JsonObject> resultAttachToTarget = Handler.getResult(attachToTarget);
		if (resultAttachToTarget.isEmpty())
		{
			return;
		}

		String sessionId = resultAttachToTarget.get().get("sessionId").getAsString();

		handler.evaluate(3, sessionId, "document.body.style.background = 'red';");
		handler.evaluate(4, sessionId, "3.14 * 9.81");
		handler.evaluate(5, sessionId, "'" + ("1234567890".repeat(500)) + "';");
		handler.evaluate(6, sessionId, "'" + ("1234567890".repeat(2000)) + "';");
	}
}
