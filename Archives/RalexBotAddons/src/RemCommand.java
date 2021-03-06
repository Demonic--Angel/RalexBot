
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;
import com.lordralex.ralexbot.file.FileSystem;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version 1.1
 * @author Lord_Ralex
 * @since 1.0
 */
public class RemCommand extends Listener {

    Map<String, String> remMap = new HashMap<String, String>();
    List<String> dontReply = new ArrayList<String>();

    @Override
    public void setup() {
        new File("data" + File.separator + "rem").mkdirs();
        for (File file : new File("data" + File.separator + "rem").listFiles()) {
            try {
                String name = file.getName().substring(0, file.getName().length() - 4).toLowerCase().trim();
                Scanner reader = new Scanner(file);
                String line = reader.nextLine().trim();
                remMap.put(name, line);

            } catch (FileNotFoundException ex) {
                Logger.getLogger(RemCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void onCommand(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        String command = event.getCommand().toLowerCase();
        String channel = event.getChannel();
        String sender = event.getSender();
        String[] args = event.getArgs();

        if (command.equalsIgnoreCase("remshutup")) {
            if (!isOP(sender, channel)) {
                return;
            }
            String target = channel;
            if (args.length != 0) {
                target = args[0];
            }
            target = target.toLowerCase();
            boolean wasThere = dontReply.remove(channel);
            if (wasThere) {
                if (channel != null) {
                    sendMessage(channel, "Returning to normal");
                } else {
                    sendMessage(sender, "Returning to normal");
                }
            } else {
                dontReply.add(target);
                if (channel != null) {
                    sendMessage(channel, "Shutting up");
                } else {
                    sendMessage(sender, "Shutting up");
                }
            }
            return;
        }

        if (dontReply.contains(channel)) {
            return;
        }

        if (isRem(command.toLowerCase())) {
            String reply = remMap.get(command);
            if (sender == null) {
                return;
            }
            reply = replacePlaceHolders(sender, channel, reply, args);
            String[] entire = reply.split("\n");
            if (channel == null) {
                sendMessage(sender, entire);
            } else {
                sendMessage(channel, entire);
            }
            return;
        }

        if (args.length == 0) {
            return;
        }

        String[] part = buildRem(args);
        String name = part[0];
        String reply = part[1];

        if (reply == null || reply.equalsIgnoreCase("null") || reply.equalsIgnoreCase("forget")) {
            remMap.remove(name);
            FileSystem.deleteRem(name);
            if (channel == null) {
                channel = sender;
            }
            if (sender != null) {
                sendMessage(channel, name + " is now forgotten");
            }
            return;
        }
        name = name.toLowerCase().trim();
        if (remMap.containsKey(name)) {
            remMap.remove(name);
            FileSystem.deleteRem(name);
        }
        remMap.put(name, reply);
        FileSystem.saveRem(name, reply);
        if (channel == null) {
            channel = sender;
        }
        sendMessage(channel, name + " is now saved");
    }

    private String[] buildRem(String[] args) {
        String[] complete = new String[2];
        complete[0] = args[0];
        complete[1] = "";
        for (int i = 1; i < args.length; i++) {
            complete[1] += args[i] + " ";
        }
        complete[1] = complete[1].trim();
        if (complete[1].equalsIgnoreCase("")) {
            complete[1] = null;
        }
        return complete;
    }

    private boolean isRem(String command) {
        return remMap.containsKey(command.toLowerCase().trim());
    }

    @Override
    public String[] getAliases() {

        List<String> list = new ArrayList<String>();
        list.add("rem");
        list.add("r");
        list.add("remember");
        list.add("remshutup");
        for (Object command : remMap.keySet()) {
            list.add((String) command);
        }
        return list.toArray(new String[0]);
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.FINAL);
    }
}
