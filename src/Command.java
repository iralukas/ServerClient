public class Command {
    private String command;

    public Command(String cmd) {
        this.command = cmd;
    }

    public MessageCommand execute(){
        return new MessageCommand(command);
    }
}
