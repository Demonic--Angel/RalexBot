
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.EventType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version 1.0
 * @author Joshua
 */
public class PluginCommand extends Listener {

    @Override
    public void onCommand(CommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final String channel = event.getChannel();
        final String sender = event.getSender();
        final String[] args = event.getArgs();
        BufferedReader reader = null;
        String target = channel;
        if (target == null) {
            target = sender;
        }
        if (target == null) {
            return;
        }
        String search = buildArgs(args);
        try {
            String url = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=site:http://dev.bukkit.org%20" + search.replace(" ", "%20");
            URL path = new URL(url);
            reader = new BufferedReader(new InputStreamReader(path.openStream()));
            List<String> parts = new ArrayList<String>();
            String s;
            while ((s = reader.readLine()) != null) {
                parts.add(s);
            }
            List<String> b = new ArrayList<String>();
            for (String part : parts) {
                String[] c = part.split(",");
                b.addAll(Arrays.asList(c));
            }
            for (String string : b) {
                if (string.startsWith("\"url\":")) {
                    string = string.replace("\"", "");
                    string = string.replace("url:", "");
                    if (args.length > 1) {
                        sendMessage(target, string);
                    } else {
                        sendMessage(target, "There is this: http://dev.bukkit.org/server-mods/" + search + " or " + string);
                    }
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MCFCommand.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(MCFCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                    "plugin"
                };
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Command, Priority.NORMAL);
    }
}
