package ru.krogenit.slaves;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ru.krogenit.slaves.objects.Job;
import ru.krogenit.slaves.objects.Slave;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    public static final Parser INSTANCE = new Parser();

    public List<Slave> parseTopUsers(JsonObject jsonObject) {
        JsonArray jsonArray = jsonObject.get("list").getAsJsonArray();
        int size = jsonArray.size();
        List<Slave> topUsers = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            JsonObject topUserObject = jsonArray.get(i).getAsJsonObject();
            int slavesCount = topUserObject.get("slaves_count").getAsInt();
            topUsers.add(new Slave(topUserObject.get("id").getAsLong(), null, -1, -1, -1, -1, null, -1, -1, slavesCount, -1));
        }

        return topUsers;
    }

    public Job parseJob(JsonObject jsonObject) {
        if(jsonObject != null) {
            JsonObject jobObject = jsonObject.getAsJsonObject("job");
            String name = "безработный";
            if (jobObject != null) {
                name = jobObject.get("name").getAsString();
            }

            return new Job(name);
        }

        return null;
    }

    public List<Slave> parseSlaves(JsonObject jsonObject) {
        return parseSlaves(jsonObject, "slaves");
    }

    public List<Slave> parseSlaves(JsonObject jsonObject, String name) {
        List<Slave> slaves = new ArrayList<>();
        JsonArray jsonArray = jsonObject.getAsJsonArray(name);
        int size = jsonArray.size();
        for (int i = 0; i < size; i++) {
            JsonObject slaveObject = jsonArray.get(i).getAsJsonObject();
            Slave slave = parseSlave(slaveObject);
            slaves.add(slave);
        }

        return slaves;
    }

    public Slave parseSlave(JsonObject jsonObject) {
        Job job = parseJob(jsonObject);
        int slavesCount = jsonObject.get("slaves_count").getAsInt();
        if(slavesCount < 0) slavesCount = 0; //почемуто бывает отрицательное количество
        return new Slave(
                jsonObject.get("id").getAsLong(),
                job,
                jsonObject.get("master_id").getAsLong(),
                jsonObject.get("fetter_price").getAsInt(),
                jsonObject.get("sale_price").getAsInt(),
                jsonObject.get("price").getAsInt(),
                new ArrayList<>(slavesCount),
                jsonObject.get("balance").getAsInt(),
                jsonObject.get("profit_per_min").getAsInt(),
                slavesCount,
                jsonObject.get("fetter_to").getAsLong()
        );
    }
}
