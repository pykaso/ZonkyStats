package net.pykaso.zonkystats.zonkystats.utils;

import net.pykaso.zonkystats.zonkystats.AppExecutors;

import java.util.concurrent.Executor;

public class InstantAppExecutors extends AppExecutors {
    private static Executor instant = command -> command.run();

    public InstantAppExecutors() {
        super(instant, instant, instant);
    }
}