public class MessageCommand {
    private String msg = null;


    public MessageCommand(String msg) {
        this.msg = msg;
    }

    public String getMessage(){
        if (msg == null){
            return "There is no message inside";
        }
        else{
            return msg;
        }
    }
}
