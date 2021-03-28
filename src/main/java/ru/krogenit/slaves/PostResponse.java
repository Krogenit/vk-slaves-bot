package ru.krogenit.slaves;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;

@AllArgsConstructor
@Getter
public class PostResponse {
    private final HttpPost post;
    private final HttpResponse response;
}
