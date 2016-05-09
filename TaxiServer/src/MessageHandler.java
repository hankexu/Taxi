import bean.Role;
import com.google.gson.Gson;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.integration.beans.ArrayEditor;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by hanke on 2016-01-07.
 */
public class MessageHandler extends IoHandlerAdapter {

    private HashMap<String, IoSession> passengers = new HashMap<>();

    private ArrayList<String> passengerMsgs = new ArrayList<>();

    private ArrayList<String> driverMsgs = new ArrayList<>();

    private HashMap<String, IoSession> drivers = new HashMap<>();

    private Gson gson = new Gson();

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        super.sessionCreated(session);
        System.out.println("一个链接已创建");

    }

    public void messageReceived(IoSession session, Object message) throws Exception {
        super.messageReceived(session, message);
        System.out.println(message.toString());
        JSONObject msg = new JSONObject(message.toString());
        if (msg.getString("role").equals(Role.PASSENGER)) {
            String name = msg.getString("name");
            if (passengers.size() == 0)
                passengers.put(name, session);
            else {
                if (!passengers.containsKey(name)) {
                    passengers.put(name, session);
                }
            }

            if (passengerMsgs.size()>0){
                if(!passengerMsgs.get(passengerMsgs.size()-1).contains(name)){
                    passengerMsgs.add(message.toString());
                }
            }else {
                passengerMsgs.add(message.toString());
            }

            if (passengers != null && passengers.size() > 0) {
                for (IoSession ioSession : passengers.values()) {
                    ioSession.write(gson.toJson(driverMsgs));
                }
            }
        }

        else if (msg.getString("role").equals(Role.DRIVER)) {
            String name = msg.getString("name");
            if (drivers.size() == 0)
                drivers.put(name, session);
            else {
                if (!drivers.containsKey(name)) {
                    drivers.put(name, session);
                }
            }
            if (driverMsgs.size()>0){
                if(!driverMsgs.get(driverMsgs.size()-1).contains(name)){
                    driverMsgs.add(message.toString());
                }
            }else {
                driverMsgs.add(message.toString());
            }
            if (drivers != null && drivers.size() > 0) {
                for (IoSession ioSession : drivers.values()) {
                    ioSession.write(gson.toJson(passengerMsgs));
                }
            }
        }
        else if (msg.getString("role").equals(Role.ORDER)){
            for (IoSession ioSession:passengers.values()){
                ioSession.write(message);
            }
        }


    }


    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
        Set setP = passengers.entrySet();
        Iterator it = setP.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            if (entry.getValue().equals(session)) {
                passengers.remove(entry.getKey());
            }
        }

        Set setD = drivers.entrySet();
        it = setD.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            if (entry.getValue().equals(session)) {
                drivers.remove(entry.getKey());
                passengerMsgs.clear();
            }
        }
        System.out.println(passengerMsgs.size());
    }
}

