package mod.chiselsandbits.profiling.jvm.jfr.events;

import jdk.jfr.*;

@Name(CandBProfilingFinishedEvent.NAME)
@Label("C&B Profiling Finished")
@Category({"Minecraft", "C&B"})
public class CandBProfilingFinishedEvent extends Event
{
    public static final String NAME = "minecraft.candb.Finished";
    public static final EventType TYPE = EventType.getEventType(CandBProfilingFinishedEvent.class);


}
