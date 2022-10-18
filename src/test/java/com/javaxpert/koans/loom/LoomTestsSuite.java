package com.javaxpert.koans.loom;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Koans like tests suite
 */
class LoomTestsSuite {
    private MultipleRunnables runnableProvider;
    @BeforeEach
    void setUp() {
        runnableProvider= new MultipleRunnables();
    }

    @AfterEach
    void tearDown() {
        if(runnableProvider.getLongLifeDummyThread().getState()!= Thread.State.TERMINATED){
            runnableProvider.getLongLifeDummyThread().toggleState();
        }
    }

    @Test
    void startingLongRunningThreadShouldIncrementActiveThreadsCount(){
        // there 's a problem here , spot it!!!
        MultipleRunnables.LongRunningTask t = runnableProvider.getLongLifeDummyThread();
        int initialActiveThreads = MultipleRunnables.getActiveThreadsCount();
        t.start();
        int afterStartActiveThreads =MultipleRunnables.getActiveThreadsCount();
        assertEquals(true,t.isAlive());
        assertEquals(1,afterStartActiveThreads-initialActiveThreads);

    }

    @Test
    void startingVirtualThreadShouldNotIncreaseActiveThreadsCount(){
        int initialActiveThreads = MultipleRunnables.getActiveThreadsCount();
        Runnable r = runnableProvider.justFort1SecondTask;
        Thread t = Thread.ofVirtual().unstarted(r);
        t.start();

        int afterStartActiveThreads =MultipleRunnables.getActiveThreadsCount();
        assertEquals(true,t.isAlive());
        assertEquals(true,t.isVirtual());
        assertEquals(0,afterStartActiveThreads-initialActiveThreads);
    }


}