package gsk.onetcore;

import java.util.ArrayList;
import gsk.onetcore.CommandBase;
import gsk.onetcore.commands.Who;

public class Register {
    public ArrayList<CommandBase> cmds = new ArrayList();

    public Register() {
        this.cmds.add(new Who());
    }

    public ArrayList<CommandBase> getCmds() {
        return this.cmds;
    }
}
