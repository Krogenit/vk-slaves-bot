package ru.krogenit.slaves;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.HttpEntity;

@AllArgsConstructor
@Getter
public class GetResponse {
    private final String response;
    private final JsonObject jsonObject;
}
