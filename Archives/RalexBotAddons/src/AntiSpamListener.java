import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.events.EventType;
import com.lordralex.ralexbot.api.events.MessageEvent;
import com.lordralex.ralexbot.file.FileSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0
 * @author Joshua
 */
public class AntiSpamListener extends Listener {

    private final Map<String, Posts> logs = new HashMap<String, Posts>();
    private int MAX_MESSAGES;
    private int SPAM_RATE;
    private int DUPE_RATE;

    @Override
    public void setup() {
        MAX_MESSAGES = FileSystem.getInt("spam-message");
        SPAM_RATE = FileSystem.getInt("spam-time");
        DUPE_RATE = FileSystem.getInt("spam-dupe");
    }

    @Override
    public void onMessage(MessageEvent event) {
        synchronized (logs) {
            if (event.isCancelled()) {
                return;
            }
            String channel = event.getChannel();
            String sender = event.getSender();
            String message = event.getMessage();
            String hostname = event.getHostname();
            if (isOP(sender, channel) || isVoice(sender, channel) || sender.equalsIgnoreCase(getPircBot().getNick())) {
                return;
            }
            message = message.toString().toLowerCase();
            Posts posts = logs.remove(sender);
            if (posts == null) {
                posts = new Posts();
            }
            if (posts.addPost(message)) {
                if (isOP(this.getPircBot().getNick(), channel)) {
                    getPircBot().kick(getPircBot().getChannel(channel), getPircBot().getUser(sender), "Triggered Spam Guard (IP=" + hostname + ")");
                } else {
                    getPircBot().sendMessage("chanserv", "kick " + channel + " " + sender + " Triggered Spam guard (IP=" + hostname + ")");
                }
                event.setCancelled(true);
            } else {
                logs.put(sender, posts);
            }
        }
    }

    @Override
    public void declarePriorities() {
        priorities.put(EventType.Message, Priority.LOWEST);
    }

    private class Posts {

        List<Post> posts = new ArrayList<Post>();

        public boolean addPost(String lastPost) {
            posts.add(new Post(System.currentTimeMillis(), lastPost));
            if (posts.size() == MAX_MESSAGES) {
                posts.remove(0);
                boolean areSame = true;
                for (int i = 1; i < posts.size() && areSame; i++) {
                    if (!posts.get(i - 1).message.equalsIgnoreCase(posts.get(i).message)) {
                        areSame = false;
                    }
                }
                if (areSame) {
                    if (posts.get(posts.size() - 1).getTime() - posts.get(0).getTime() < DUPE_RATE) {
                        return true;
                    }
                }

                if (posts.get(posts.size() - 1).getTime() - posts.get(0).getTime() < SPAM_RATE) {
                    return true;
                }
            }
            return false;
        }
    }

    private class Post {

        long timePosted;
        String message;

        public Post(long Time, String Message) {
            timePosted = Time;
            message = Message;
        }

        public String getMessage() {
            return message;
        }

        public long getTime() {
            return timePosted;
        }
    }
}
