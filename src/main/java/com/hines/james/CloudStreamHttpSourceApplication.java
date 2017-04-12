package com.hines.james;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collections;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@SpringBootApplication
public class CloudStreamHttpSourceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudStreamHttpSourceApplication.class, args);
	}
}

@Configuration
@Controller
@EnableBinding(Source.class)
class HttpSourceConfiguration {

	@Autowired
	private Source channels;

	@RequestMapping(path = "${http.pathPattern:/}", method = POST, consumes = {"text/*", "application/json"})
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void handleRequest(@RequestBody String body, @RequestHeader(HttpHeaders.CONTENT_TYPE) Object contentType) throws MyCustomException {
        JSONObject obj = new JSONObject(body);

        if(obj.getString("foo").equals("bar")) {
            throw new MyCustomException();
        } else {
            System.out.println(obj.getString("foo"));
            sendMessage(body, contentType);
        }
	}

	@RequestMapping(path = "${http.pathPattern:/}", method = POST, consumes = "*/*")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void handleRequest(@RequestBody byte[] body, @RequestHeader(HttpHeaders.CONTENT_TYPE) Object contentType) {
		sendMessage(body, contentType);
	}

	private void sendMessage(Object body, Object contentType) {
		channels.output().send(MessageBuilder.createMessage(body,
				new MessageHeaders(Collections.singletonMap(MessageHeaders.CONTENT_TYPE, contentType))));
	}
}

@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Foo failed validation")
class MyCustomException extends Exception {
    private static final long serialVersionUID = 100L;
}
