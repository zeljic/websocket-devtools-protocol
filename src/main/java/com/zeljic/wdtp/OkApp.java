package com.zeljic.wdtp;

import com.google.gson.JsonObject;
import okhttp3.*;

import java.util.Optional;

public class OkApp
{
	public static void main(String[] args)
	{
		var client = new OkHttpClient().newBuilder().build();

		Request request = new Request.Builder().url("ws://127.0.0.1:9111/devtools/browser/ed4da3c0-a53d-462c-8704-016f4aad428e").build();
		var handler = new OkHandler();

		WebSocket ws = client.newWebSocket(request, handler);

		handler.setWebSocket(ws);

		JsonObject createTarget = handler.createTarget(1, "about:blank");
		Optional<JsonObject> resultCreateTarget = Handler.getResult(createTarget);
		if (resultCreateTarget.isEmpty())
		{
			return;
		}

		String targetId = resultCreateTarget.get().get("targetId").getAsString();

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
		handler.evaluate(6, sessionId, "'" + ("1234567890".repeat(20000)) + "';");
	}
}
