package ru.krogenit.slaves;

import com.google.gson.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.util.EntityUtils;
import ru.krogenit.slaves.objects.Me;
import ru.krogenit.slaves.objects.Slave;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RequestHandler {

    private final Random rand = new Random();
    private final int minSleepTime;
    private final int maxSleepTime;
    private final int timeout = 120;
    private final RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000).setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
    private final CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    private final String token;

    public RequestHandler(int minSleepTime, int maxSleepTime, String token) {
        this.minSleepTime = minSleepTime;
        this.maxSleepTime = maxSleepTime;
        this.token = token;
    }

    public Slave get(long id) {
        GetResponse response = getRequest("user?id=" + id);
        if(response.getResponse().equalsIgnoreCase("ok")) {
            return Parser.INSTANCE.parseSlave(response.getJsonObject());
        } else {
            System.out.println("get error " + response.getResponse());
        }

        return null;
    }

    public List<Slave> getFriends(long[] friends) {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < friends.length; i++) {
            jsonArray.add(friends[i]);
        }

        jsonObject.add("ids", jsonArray);
        String json = new Gson().toJson(jsonObject);
        PostResponse response = postRequest("user", json, false);
        HttpResponse httpResponse = response.getResponse();
        if(httpResponse.getStatusLine().getReasonPhrase().equalsIgnoreCase("ok")) {
            try {
                HttpEntity entity = httpResponse.getEntity();
                String result = EntityUtils.toString(entity);
                response.getPost().releaseConnection();
                JsonObject jsonObject1 = new Gson().fromJson(result, JsonObject.class);
//                System.out.println(jsonObject1);
                return Parser.INSTANCE.parseSlaves(jsonObject1, "users");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Не удалось получить список друзей");
            System.out.println(httpResponse.getStatusLine().toString());
            try {
                String result = EntityUtils.toString(httpResponse.getEntity());
                JsonObject errorObject = new Gson().fromJson(result, JsonObject.class);
                System.out.println(errorObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new ArrayList<>(0);
    }

    public boolean saleSlave(Slave slave) {
        JsonObject jsonObject = new JsonObject();
        JsonElement idElement = new JsonPrimitive(slave.getId());
        jsonObject.add("slave_id", idElement);
        String json = new Gson().toJson(jsonObject);
        HttpResponse httpResponse = postRequest("saleSlave", json);
        if(httpResponse.getStatusLine().getReasonPhrase().equalsIgnoreCase("ok")) {
//            System.out.println("Успешно продан раб " + slave.getId() + " за " + slave.getSalePrice());
            return true;
        } else {
            System.out.println("Не удалось продать раба " + slave.getId());
            System.out.println(httpResponse.getStatusLine().toString());
            try {
                String result = EntityUtils.toString(httpResponse.getEntity());
                JsonObject errorObject = new Gson().fromJson(result, JsonObject.class);
                System.out.println(errorObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public boolean setJob(long id, String name) {
        JsonObject jsonObject = new JsonObject();
        JsonElement idElement = new JsonPrimitive(id);
        JsonElement nameElement = new JsonPrimitive(name);
        jsonObject.add("slave_id", idElement);
        jsonObject.add("name", nameElement);
        String json = new Gson().toJson(jsonObject);
        HttpResponse httpResponse = postRequest("jobSlave", json);
        if(httpResponse != null && httpResponse.getStatusLine().getReasonPhrase().equalsIgnoreCase("ok")) {
//            System.out.println("Успешно назначена работа " + name + " рабу " + id);
            return true;
        } else {
            System.out.println("Не удалось назначить работу рабу " + id);
            if(httpResponse != null) {
                System.out.println(httpResponse.getStatusLine().toString());
                try {
                    String result = EntityUtils.toString(httpResponse.getEntity());
                    JsonObject errorObject = new Gson().fromJson(result, JsonObject.class);
                    System.out.println(errorObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    public List<Slave> getTop() {
        GetResponse response = getRequest("topUsers");
        if(response.getResponse().equalsIgnoreCase("ok")) {
            return Parser.INSTANCE.parseTopUsers(response.getJsonObject());
        } else {
            System.out.println("getTop error " + response.getResponse());
        }

        return new ArrayList<>(0);
    }

    public List<Slave> getSlaves(long id) {
        GetResponse response = getRequest("slaveList?id=" + id);
        if (response.getResponse().equalsIgnoreCase("ok")) {
            return Parser.INSTANCE.parseSlaves(response.getJsonObject());
        } else {
            System.out.println("getSlaves error " + response.getResponse());
        }

        return new ArrayList<>(0);
    }

    public boolean buySlave(Slave slave) {
        JsonObject jsonObject = new JsonObject();
        JsonElement jsonElement = new JsonPrimitive(slave.getId());
        jsonObject.add("slave_id", jsonElement);
        String json = new Gson().toJson(jsonObject);
        HttpResponse httpResponse = postRequest("buySlave", json);
        if(httpResponse != null && httpResponse.getStatusLine().getReasonPhrase().equalsIgnoreCase("ok")) {
//            System.out.println("Успешно куплен раб " + slave.getId() + " за " + slave.getPrice());
            return true;
        } else {
            System.out.println("Не удалось купить раба " + slave.getId());
            if(httpResponse != null) {
                System.out.println(httpResponse.getStatusLine().toString());
                try {
                    String result = EntityUtils.toString(httpResponse.getEntity());
                    JsonObject errorObject = new Gson().fromJson(result, JsonObject.class);
                    System.out.println(errorObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    public Me get() {
        GetResponse response = getRequest("start");
        if(response.getResponse().equalsIgnoreCase("ok")) {
            JsonObject jsonObject = response.getJsonObject();
            JsonObject meObject = jsonObject.getAsJsonObject("me");
            Slave slave = Parser.INSTANCE.parseSlave(meObject);
            List<Slave> slaves = Parser.INSTANCE.parseSlaves(jsonObject);
            slave.getSlaves().addAll(slaves);
            return new Me(slave);
        } else {
            return new Me(new Slave(-1, null, -1, -1, -1, -1, null, -1, -1, -1, -1));
        }
    }

    private HttpResponse postRequest(String method, String json) {
        return postRequest(method, json, true).getResponse();
    }

    private PostResponse postRequest(String method, String json, boolean closeConnection) {
        try {
            Thread.sleep(getRandomSleepTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        CloseableHttpResponse httpResponse = null;
        StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);

        HttpPost postMethod = new HttpPost("https://pixel.w84.vkforms.ru/HappySanta/slaves/1.0.0/" + method);
        populateHeaders(postMethod, method, "POST");
        postMethod.setEntity(requestEntity);

        try {
            httpResponse = httpClient.execute(postMethod);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(closeConnection) postMethod.releaseConnection();
        }

        return new PostResponse(postMethod, httpResponse);
    }

    private GetResponse getRequest(String method) {
        GetResponse response = new GetResponse("error", null);

        try{
            Thread.sleep(getRandomSleepTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            HttpGet request = new HttpGet("https://pixel.w84.vkforms.ru/HappySanta/slaves/1.0.0/" + method);
            populateHeaders(request, method, "GET");

            CloseableHttpResponse httpResponse = httpClient.execute(request);
//            System.out.println(httpResponse.getStatusLine().toString());

            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                JsonObject jsonObject = new Gson().fromJson(result, JsonObject.class);
//                System.out.println(jsonObject);
                response = new GetResponse(httpResponse.getStatusLine().getReasonPhrase(), jsonObject);
            }

            httpResponse.close();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    private void populateHeaders(AbstractHttpMessage message, String method, String methodType) {
        message.addHeader("accept", "application/json, text/plain, */*");
        message.addHeader(":authority", "pixel.w84.vkforms.ru");
        message.addHeader(":method", methodType);
        message.addHeader(":path", "/HappySanta/slaves/1.0.0/"+method);
        message.addHeader(":scheme", "https");
        message.addHeader("accept-encoding", "gzip, deflate, br");
        message.addHeader("accept-language", "en-US,en;q=0.9,ru;q=0.8");
        message.addHeader("authorization", token);
        message.addHeader("origin", "https://prod-app7794757-c1ffb3285f12.pages-ac.vk-apps.com");
        message.addHeader("referer", "https://prod-app7794757-c1ffb3285f12.pages-ac.vk-apps.com/");
        message.addHeader("sec-ch-ua", "\"Google Chrome\";v=\"89\", \"Chromium\";v=\"89\", \";Not A Brand\";v=\"99\"");
        message.addHeader("sec-ch-ua-mobile", "?0");
        message.addHeader("sec-fetch-dest", "empty");
        message.addHeader("sec-fetch-mode", "cors");
        message.addHeader("sec-fetch-site", "cross-site");
        message.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");
    }

    private long getRandomSleepTime() {
        return rand.nextInt(maxSleepTime - minSleepTime) + minSleepTime;
    }

    public void clear() {
        if(httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
